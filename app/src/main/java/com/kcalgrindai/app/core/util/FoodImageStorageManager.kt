package com.kcalgrindai.app.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Manages local user food photos strictly within app-private internal storage:
 * - EXIF/GPS metadata is 100% stripped upon import (by re-encoding Bitmap).
 * - Resized to max 1024x1024 to save disk and memory.
 * - Stored in app-private `filesDir/food_images/`.
 * - Never synced or uploaded to cloud.
 * - Permanently purged on account deletion or local wipe.
 */
@Singleton
class FoodImageStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val imagesDir: File by lazy {
        File(context.filesDir, "food_images").apply {
            if (!exists()) mkdirs()
        }
    }

    suspend fun saveCustomFoodPhoto(foodId: Long, sourceUri: Uri): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val inputStream: InputStream = context.contentResolver.openInputStream(sourceUri)
                ?: throw IllegalArgumentException("Cannot open stream for URI: $sourceUri")

            // 1. Decode bounds to compute inSampleSize
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            inputStream.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            val maxDimension = 1024
            var sampleSize = 1
            val origWidth = options.outWidth
            val origHeight = options.outHeight

            if (origWidth > maxDimension || origHeight > maxDimension) {
                val halfWidth = origWidth / 2
                val halfHeight = origHeight / 2
                while ((halfWidth / sampleSize) >= maxDimension && (halfHeight / sampleSize) >= maxDimension) {
                    sampleSize *= 2
                }
            }

            // 2. Decode sampled bitmap (this discards EXIF/GPS tags entirely)
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // Energy & memory friendly
            }

            val secondStream = context.contentResolver.openInputStream(sourceUri)
                ?: throw IllegalArgumentException("Cannot re-open stream for URI: $sourceUri")

            val sampledBitmap = secondStream.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: throw IllegalStateException("Failed to decode bitmap from $sourceUri")

            // 3. Scale down if still slightly over maxDimension
            val longestEdge = max(sampledBitmap.width, sampledBitmap.height)
            val finalBitmap = if (longestEdge > maxDimension) {
                val scale = maxDimension.toFloat() / longestEdge
                val scaledWidth = (sampledBitmap.width * scale).toInt()
                val scaledHeight = (sampledBitmap.height * scale).toInt()
                Bitmap.createScaledBitmap(sampledBitmap, scaledWidth, scaledHeight, true)
            } else {
                sampledBitmap
            }

            // 4. Save clean JPEG into app-private internal directory
            val targetFile = File(imagesDir, "custom_food_${foodId}.jpg")
            FileOutputStream(targetFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                out.flush()
            }

            targetFile
        }
    }

    fun saveFoodImageFromBitmap(foodId: Long, bitmap: Bitmap): File {
        val targetFile = File(imagesDir, "custom_food_${foodId}.jpg")
        FileOutputStream(targetFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.flush()
        }
        return targetFile
    }

    fun getFoodImageFile(foodId: Long): File? {
        val file = File(imagesDir, "custom_food_${foodId}.jpg")
        return if (file.exists() && file.length() > 0) file else null
    }

    fun deleteFoodImage(foodId: Long): Boolean {
        val file = File(imagesDir, "custom_food_${foodId}.jpg")
        return if (file.exists()) file.delete() else false
    }

    fun deleteAllFoodImages(): Int {
        var count = 0
        imagesDir.listFiles()?.forEach { file ->
            if (file.isFile && file.delete()) {
                count++
            }
        }
        return count
    }
}
