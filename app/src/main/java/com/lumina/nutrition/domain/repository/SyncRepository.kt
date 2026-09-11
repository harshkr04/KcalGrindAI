package com.lumina.nutrition.domain.repository

/**
 * Cloud sync repository — pushes local Room data to Supabase
 * via the backend and pulls remote changes back.
 *
 * Room remains the local source of truth; Supabase is a synced copy.
 * The Android app never talks to Supabase directly.
 */
interface SyncRepository {
    /** Push all local data to the cloud (scoped to the current Firebase user). */
    suspend fun pushAll(): Result<SyncPushResult>

    /** Pull remote changes since the last sync and merge into Room. */
    suspend fun pullSince(sinceEpochMs: Long): Result<SyncPullResult>

    /**
     * Full sync: push first, then pull.
     * Returns a combined result with push + pull summaries.
     */
    suspend fun fullSync(): Result<SyncResult>

    /** Get the epoch-millis timestamp of the last successful sync. */
    fun getLastSyncTimestamp(): Long

    /** Store the last-sync timestamp after a successful sync. */
    fun setLastSyncTimestamp(epochMs: Long)
}

data class SyncPushResult(
    val pushed: Map<String, Int> = emptyMap(),
    val skipped: Map<String, Int> = emptyMap(),
    val errors: Map<String, String> = emptyMap()
)

data class SyncPullResult(
    val profileUpdated: Boolean = false,
    val goalUpdated: Boolean = false,
    val mealLogsInserted: Int = 0,
    val weightEntriesInserted: Int = 0,
    val waterLogsInserted: Int = 0,
    val conversationsInserted: Int = 0,
    val messagesInserted: Int = 0
)

data class SyncResult(
    val push: SyncPushResult,
    val pull: SyncPullResult,
    val syncedAt: Long
)
