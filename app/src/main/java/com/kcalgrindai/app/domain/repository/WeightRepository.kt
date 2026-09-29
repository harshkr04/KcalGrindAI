package com.kcalgrindai.app.domain.repository

import com.kcalgrindai.app.domain.model.WeightEntry
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    suspend fun saveWeightEntry(entry: WeightEntry): Long
    suspend fun getWeightEntryById(id: Long): WeightEntry?
    suspend fun getLatestWeightEntry(): WeightEntry?
    fun observeLatestWeightEntry(): Flow<WeightEntry?>
    fun observeAllWeightEntries(): Flow<List<WeightEntry>>
    fun observeWeightEntriesInRange(startDate: String, endDate: String): Flow<List<WeightEntry>>
    suspend fun deleteWeightEntry(id: Long)
}
