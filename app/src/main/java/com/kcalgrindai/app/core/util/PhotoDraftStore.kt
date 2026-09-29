package com.kcalgrindai.app.core.util

import java.io.File

/**
 * Abstraction over photo draft lifecycle: compression, deletion, and stale-file cleanup.
 * Production implementation is [ImageCompressor]; tests use a fake.
 */
interface PhotoDraftStore {

    /**
     * Compresses the image at [inputPathOrUri] (file path, file:// URI, or content:// URI)
     * and writes the result to a draft file in the cache directory.
     *
     * @return [Result.success] with the draft [File], or [Result.failure] on error.
     */
    suspend fun compress(inputPathOrUri: String): Result<File>

    /**
     * Deletes the raw camera capture file after compression.
     */
    fun deleteRawCapture(filePath: String?)

    /**
     * Deletes a draft photo upon successful meal log or cancellation.
     */
    fun deleteDraft(filePath: String?)

    /**
     * Purges temporary capture and draft files older than 24 hours.
     */
    fun purgeStaleTempFiles()
}
