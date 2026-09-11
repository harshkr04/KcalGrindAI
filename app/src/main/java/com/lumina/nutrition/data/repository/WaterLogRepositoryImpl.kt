package com.lumina.nutrition.data.repository

import com.lumina.nutrition.data.local.dao.WaterLogDao
import com.lumina.nutrition.data.mapper.toDomain
import com.lumina.nutrition.data.mapper.toEntity
import com.lumina.nutrition.domain.model.WaterLog
import com.lumina.nutrition.domain.repository.WaterLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaterLogRepositoryImpl @Inject constructor(
    private val waterLogDao: WaterLogDao
) : WaterLogRepository {

    override suspend fun saveWaterLog(entry: WaterLog): Long {
        return waterLogDao.insertWaterLog(entry.toEntity())
    }

    override suspend fun logWater(amountMl: Int, date: String): Long {
        val entry = WaterLog(
            id = 0L,
            date = date,
            amountMl = amountMl,
            loggedAt = System.currentTimeMillis()
        )
        return waterLogDao.insertWaterLog(entry.toEntity())
    }

    override fun observeWaterLogsByDate(date: String): Flow<List<WaterLog>> {
        return waterLogDao.observeWaterLogsByDate(date).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getWaterLogsByDate(date: String): List<WaterLog> {
        return waterLogDao.getWaterLogsByDate(date).map { it.toDomain() }
    }

    override fun observeWaterLogsInRange(startDate: String, endDate: String): Flow<List<WaterLog>> {
        return waterLogDao.observeWaterLogsInRange(startDate, endDate).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getWaterLogsInRange(startDate: String, endDate: String): List<WaterLog> {
        return waterLogDao.getWaterLogsInRange(startDate, endDate).map { it.toDomain() }
    }

    override fun observeTotalWaterByDate(date: String): Flow<Int> {
        return waterLogDao.observeTotalWaterByDate(date)
    }

    override suspend fun deleteWaterLog(id: Long) {
        waterLogDao.deleteWaterLogById(id)
    }
}
