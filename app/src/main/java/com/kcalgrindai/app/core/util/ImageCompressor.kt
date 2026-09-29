package com.kcalgrindai.app.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ImageCompressor"
private const val MAX_DIMENSION_PX = 1024
private const val TARGET_MAX_BYTES = 500 * 1024 // ~500 KB
private const val STALE_THRESHOLD_MS = 24 * 60 * 60 * 1000L // 24 hours

@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) : PhotoDraftStore {

    /**
     * Compresses a photo from a file or content URI:
     * 1. Bounds-decodes to determine dimensions without allocating full image memory.
     * 2. Computes inSampleSize (power of 2) before full decode.
     * 3. Scales the longest side to at most 1024 px (images <= 1024 px retain original dimensions).
     * 4. Corrects EXIF rotation and strips EXIF/GPS metadata during re-encoding for user privacy.
     * 5. Compresses at JPEG quality 80; if > 500 KB, re-compresses at quality 60.
     * 6. Writes result to cacheDir/photo_drafts/<uuid>.jpg.
     * 7. Logs original and compressed dimensions and bytes (never logs base64 content).
     */
    override suspend fun compress(inputPathOrUri: String): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val (bytes, orientation) = readInputBytesAndOrientation(inputPathOrUri)
            if (bytes.isEmpty()) {
                throw IllegalArgumentException("Input image data is empty")
            }

            val origBytes = bytes.size

            // 1. Decode bounds
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)
            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight

            if (origWidth <= 0 || origHeight <= 0) {
                throw IllegalArgumentException("Unable to decode image dimensions ($origWidth x $origHeight)")
            }

            // 2. Compute inSampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(origWidth, origHeight, MAX_DIMENSION_PX)
            }

            // 3. Decode bitmap with inSampleSize on Dispatchers.Default
            val decodedBitmap = withContext(Dispatchers.Default) {
                val rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
                    ?: throw IllegalStateException("Failed to decode bitmap from image bytes")

                // 4. Correct EXIF rotation
                val rotatedBitmap = rotateBitmapIfNeeded(rawBitmap, orientation)

                // 5. Downscale longest side to 1024 px (if larger than 1024)
                scaleBitmapToMaxDimension(rotatedBitmap, MAX_DIMENSION_PX)
            }

            val finalWidth = decodedBitmap.width
            val finalHeight = decodedBitmap.height

            // 6. Compress at quality 80, fallback to 60 if > 500 KB
            var quality = 80
            var outputStream = ByteArrayOutputStream()
            decodedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            if (outputStream.size() > TARGET_MAX_BYTES) {
                quality = 60
                outputStream = ByteArrayOutputStream()
                decodedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            val compressedBytes = outputStream.toByteArray()
            decodedBitmap.recycle()

            // 7. Save to cacheDir/photo_drafts/<uuid>.jpg
            val draftsDir = File(context.cacheDir, "photo_drafts").apply { mkdirs() }
            val draftFile = File(draftsDir, "${UUID.randomUUID()}.jpg")
            FileOutputStream(draftFile).use { fos ->
                fos.write(compressedBytes)
                fos.flush()
            }

            // 8. Safe logging (dims and bytes only, NEVER base64 content)
            Log.d(
                TAG,
                "Compressed photo: original=${origWidth}x${origHeight} ($origBytes bytes) -> " +
                    "compressed=${finalWidth}x${finalHeight} (${draftFile.length()} bytes, quality=$quality)"
            )

            draftFile
        }
    }

    private fun readInputBytesAndOrientation(inputPathOrUri: String): Pair<ByteArray, Int> {
        if (inputPathOrUri.startsWith("content://")) {
            val uri = Uri.parse(inputPathOrUri)
            val resolver = context.contentResolver
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalArgumentException("Cannot open stream for URI: $inputPathOrUri")
            val orientation = resolver.openInputStream(uri)?.use { stream ->
                try {
                    val exif = ExifInterface(stream)
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                } catch (e: Exception) {
                    ExifInterface.ORIENTATION_NORMAL
                }
            } ?: ExifInterface.ORIENTATION_NORMAL
            return Pair(bytes, orientation)
        }

        val resolvedPath = if (inputPathOrUri.startsWith("file://")) {
            val uri = Uri.parse(inputPathOrUri)
            val rawPath = uri.path ?: inputPathOrUri.removePrefix("file://")
            val decoded = Uri.decode(rawPath)
            val withoutLeadingSlash = if (decoded.length > 2 && (decoded.startsWith("/") || decoded.startsWith("\\")) && decoded[1].isLetter() && decoded[2] == ':') {
                decoded.substring(1)
            } else {
                decoded
            }
            withoutLeadingSlash.replace('/', File.separatorChar)
        } else {
            inputPathOrUri
        }

        val file = File(resolvedPath)
        if (!file.exists()) {
            throw IllegalArgumentException("File does not exist: $inputPathOrUri (resolved: $resolvedPath)")
        }
        val bytes = file.readBytes()
        val orientation = try {
            val exif = ExifInterface(file.absolutePath)
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
        return Pair(bytes, orientation)
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
    }

    private fun scaleBitmapToMaxDimension(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val longestSide = maxOf(width, height)

        if (longestSide <= maxDim) {
            return bitmap
        }

        val scaleFactor = maxDim.toFloat() / longestSide.toFloat()
        val targetWidth = (width * scaleFactor).toInt().coerceAtLeast(1)
        val targetHeight = (height * scaleFactor).toInt().coerceAtLeast(1)

        val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        if (scaled != bitmap) {
            bitmap.recycle()
        }
        return scaled
    }

    fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var inSampleSize = 1
        val longest = maxOf(width, height)
        while ((longest / inSampleSize) > maxDim * 2) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    /**
     * Deletes the raw camera capture file after compression.
     */
    override fun deleteRawCapture(filePath: String?) {
        if (filePath.isNullOrBlank()) return
        try {
            val file = File(filePath)
            if (file.exists() && file.parentFile?.name == "photo_captures") {
                file.delete()
                Log.d(TAG, "Deleted raw capture file: $filePath")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete raw capture file: $filePath", e)
        }
    }

    /**
     * Deletes a draft photo upon successful meal log or cancellation.
     */
    override fun deleteDraft(filePath: String?) {
        if (filePath.isNullOrBlank()) return
        try {
            val file = File(filePath)
            if (file.exists() && file.parentFile?.name == "photo_drafts") {
                file.delete()
                Log.d(TAG, "Deleted draft photo: $filePath")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete draft photo: $filePath", e)
        }
    }

    /**
     * Purges temporary capture and draft files older than 24 hours.
     */
    override fun purgeStaleTempFiles() {
        val cache = context.cacheDir
        val dirs = listOf(
            File(cache, "photo_captures"),
            File(cache, "photo_drafts")
        )
        val cutoff = System.currentTimeMillis() - STALE_THRESHOLD_MS

        dirs.forEach { dir ->
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    if (file.isFile && file.lastModified() < cutoff) {
                        file.delete()
                        Log.d(TAG, "Purged stale temp file: ${file.absolutePath}")
                    }
                }
            }
        }
    }
}
