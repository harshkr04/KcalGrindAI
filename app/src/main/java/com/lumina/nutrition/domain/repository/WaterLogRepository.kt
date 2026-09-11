package com.lumina.nutrition.domain.repository

import com.lumina.nutrition.domain.model.WaterLog
import kotlinx.coroutines.flow.Flow

interface WaterLogRepository {
    suspend fun saveWaterLog(entry: WaterLog): Long
    suspend fun logWater(amountMl: Int, date: String): Long
    fun observeWaterLogsByDate(date: String): Flow<List<WaterLog>>
    suspend fun getWaterLogsByDate(date: String): List<WaterLog>
    fun observeWaterLogsInRange(startDate: String, endDate: String): Flow<List<WaterLog>>
    suspend fun getWaterLogsInRange(startDate: String, endDate: String): List<WaterLog>
    fun observeTotalWaterByDate(date: String): Flow<Int>
    suspend fun deleteWaterLog(id: Long)
}
