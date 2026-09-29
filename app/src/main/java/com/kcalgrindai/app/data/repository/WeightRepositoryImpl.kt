package com.kcalgrindai.app.data.repository

import com.kcalgrindai.app.data.local.dao.WeightEntryDao
import com.kcalgrindai.app.data.mapper.toDomain
import com.kcalgrindai.app.data.mapper.toEntity
import com.kcalgrindai.app.domain.model.WeightEntry
import com.kcalgrindai.app.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepositoryImpl @Inject constructor(
    private val weightEntryDao: WeightEntryDao
) : WeightRepository {

    override suspend fun saveWeightEntry(entry: WeightEntry): Long {
        return weightEntryDao.upsertWeightEntry(entry.toEntity())
    }

    override suspend fun getWeightEntryById(id: Long): WeightEntry? {
        return weightEntryDao.getWeightEntryById(id)?.toDomain()
    }

    override suspend fun getLatestWeightEntry(): WeightEntry? {
        return weightEntryDao.getLatestWeightEntry()?.toDomain()
    }

    override fun observeLatestWeightEntry(): Flow<WeightEntry?> {
        return weightEntryDao.observeLatestWeightEntry().map { it?.toDomain() }
    }

    override fun observeAllWeightEntries(): Flow<List<WeightEntry>> {
        return weightEntryDao.observeAllWeightEntries().map { list -> list.map { it.toDomain() } }
    }

    override fun observeWeightEntriesInRange(
        startDate: String,
        endDate: String
    ): Flow<List<WeightEntry>> {
        return weightEntryDao.observeWeightEntriesInRange(startDate, endDate).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun deleteWeightEntry(id: Long) {
        weightEntryDao.deleteWeightEntryById(id)
    }
}
