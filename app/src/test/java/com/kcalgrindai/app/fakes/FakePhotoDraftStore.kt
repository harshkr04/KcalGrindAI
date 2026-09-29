package com.kcalgrindai.app.fakes

import com.kcalgrindai.app.core.util.PhotoDraftStore
import java.io.File

/**
 * In-memory fake for [PhotoDraftStore]. No real file I/O.
 * Tests can inspect [compressCalls], [deleteDraftCalls], [deleteRawCaptureCalls], and
 * [purgeCalled] to assert interactions.
 */
class FakePhotoDraftStore : PhotoDraftStore {

    /** Paths passed to [compress]. */
    val compressCalls = mutableListOf<String>()
    /** Paths passed to [deleteDraft]. */
    val deleteDraftCalls = mutableListOf<String?>()
    /** Paths passed to [deleteRawCapture]. */
    val deleteRawCaptureCalls = mutableListOf<String?>()
    /** Whether [purgeStaleTempFiles] was called. */
    var purgeCalled = false
        private set

    /** When non-null, [compress] returns this result. Defaults to a dummy success. */
    var compressResult: Result<File> = Result.success(File("/fake/draft.jpg"))

    override suspend fun compress(inputPathOrUri: String): Result<File> {
        compressCalls.add(inputPathOrUri)
        return compressResult
    }

    override fun deleteRawCapture(filePath: String?) {
        deleteRawCaptureCalls.add(filePath)
    }

    override fun deleteDraft(filePath: String?) {
        deleteDraftCalls.add(filePath)
    }

    override fun purgeStaleTempFiles() {
        purgeCalled = true
    }
}
