package com.kcalgrindai.app.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ImageCompressorTest {

    private lateinit var context: Context
    private lateinit var compressor: ImageCompressor
    private lateinit var testDir: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        compressor = ImageCompressor(context)
        testDir = File(context.cacheDir, "test_images").apply { mkdirs() }
    }

    private fun createTestImage(width: Int, height: Int, filename: String = "test.jpg"): File {
        val file = File(testDir, filename)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(120, 160, 200))
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }
        bitmap.recycle()
        return file
    }

    @Test
    fun calculateInSampleSize_computesCorrectPowerOfTwo() {
        // Dimensions smaller than maxDim * 2 -> inSampleSize = 1
        assertEquals(1, compressor.calculateInSampleSize(1024, 768, 1024))
        assertEquals(1, compressor.calculateInSampleSize(2048, 1536, 1024))

        // Dimensions >= maxDim * 2 -> inSampleSize = 2
        assertEquals(2, compressor.calculateInSampleSize(4000, 3000, 1024))

        // Dimensions >= maxDim * 4 -> inSampleSize = 4
        assertEquals(4, compressor.calculateInSampleSize(8192, 6144, 1024))
    }

    @Test
    fun compress_3000x2000_downscalesLongestSideTo1024Px() = runTest {
        val originalFile = createTestImage(3000, 2000, "large_3000x2000.jpg")

        val result = compressor.compress(originalFile.absolutePath)
        assertTrue(result.isSuccess)

        val draftFile = result.getOrThrow()
        assertTrue(draftFile.exists())

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(draftFile.absolutePath, options)

        assertEquals(1024, options.outWidth)
        assertEquals(682, options.outHeight)
    }

    @Test
    fun compress_640x480_stays640x480() = runTest {
        val originalFile = createTestImage(640, 480, "small_640x480.jpg")

        val result = compressor.compress(originalFile.absolutePath)
        assertTrue(result.isSuccess)

        val draftFile = result.getOrThrow()
        assertTrue(draftFile.exists())

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(draftFile.absolutePath, options)

        assertEquals(640, options.outWidth)
        assertEquals(480, options.outHeight)
    }

    @Test
    fun compress_correctsExifRotation() = runTest {
        val originalFile = createTestImage(800, 600, "exif_rotate.jpg")

        // Tag with 90-degree clockwise rotation
        val exif = ExifInterface(originalFile.absolutePath)
        exif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
        exif.saveAttributes()

        val result = compressor.compress(originalFile.absolutePath)
        assertTrue(result.isSuccess)

        val draftFile = result.getOrThrow()
        assertTrue(draftFile.exists())

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(draftFile.absolutePath, options)

        // After 90 degree rotation, width and height should be transposed: 600x800
        assertEquals(600, options.outWidth)
        assertEquals(800, options.outHeight)
    }

    @Test
    fun compress_corruptFile_returnsFailure() = runTest {
        val corruptFile = File(testDir, "corrupt.jpg").apply {
            writeBytes(byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04))
        }

        val result = compressor.compress(corruptFile.absolutePath)
        assertTrue("Corrupt image must return Result.failure", result.isFailure)
    }

    @Test
    fun compress_galleryUriCopy_handlesContentOrFileUri() = runTest {
        val originalFile = createTestImage(800, 600, "gallery_test.jpg")
        val fileUri = android.net.Uri.fromFile(originalFile).toString()

        val result = compressor.compress(fileUri)
        assertTrue("Expected success for file URI: ${result.exceptionOrNull()?.message}", result.isSuccess)

        val draftFile = result.getOrThrow()
        assertTrue(draftFile.exists())
        assertTrue(draftFile.length() > 0)
    }

    @Test
    fun deleteRawCapture_deletesOnlyFromPhotoCapturesDirectory() {
        val capturesDir = File(context.cacheDir, "photo_captures").apply { mkdirs() }
        val captureFile = File(capturesDir, "raw_capture.jpg").apply { writeText("raw") }
        assertTrue(captureFile.exists())

        compressor.deleteRawCapture(captureFile.absolutePath)
        assertFalse(captureFile.exists())

        // Ensure it does not delete files from other directories
        val otherFile = File(testDir, "safe.txt").apply { writeText("safe") }
        compressor.deleteRawCapture(otherFile.absolutePath)
        assertTrue(otherFile.exists())
    }

    @Test
    fun deleteDraft_deletesOnlyFromPhotoDraftsDirectory() {
        val draftsDir = File(context.cacheDir, "photo_drafts").apply { mkdirs() }
        val draftFile = File(draftsDir, "draft_photo.jpg").apply { writeText("draft") }
        assertTrue(draftFile.exists())

        compressor.deleteDraft(draftFile.absolutePath)
        assertFalse(draftFile.exists())

        // Ensure it does not delete files from other directories
        val otherFile = File(testDir, "safe_draft.txt").apply { writeText("safe") }
        compressor.deleteDraft(otherFile.absolutePath)
        assertTrue(otherFile.exists())
    }

    @Test
    fun purgeStaleTempFiles_removesOnlyFilesOlderThan24Hours() {
        val capturesDir = File(context.cacheDir, "photo_captures").apply { mkdirs() }
        val draftsDir = File(context.cacheDir, "photo_drafts").apply { mkdirs() }

        val oldCapture = File(capturesDir, "old_capture.jpg").apply {
            writeText("old")
            setLastModified(System.currentTimeMillis() - 25 * 60 * 60 * 1000L)
        }
        val newCapture = File(capturesDir, "new_capture.jpg").apply {
            writeText("new")
            setLastModified(System.currentTimeMillis())
        }
        val oldDraft = File(draftsDir, "old_draft.jpg").apply {
            writeText("old")
            setLastModified(System.currentTimeMillis() - 25 * 60 * 60 * 1000L)
        }
        val newDraft = File(draftsDir, "new_draft.jpg").apply {
            writeText("new")
            setLastModified(System.currentTimeMillis())
        }

        compressor.purgeStaleTempFiles()

        assertFalse(oldCapture.exists())
        assertTrue(newCapture.exists())
        assertFalse(oldDraft.exists())
        assertTrue(newDraft.exists())
    }
}
