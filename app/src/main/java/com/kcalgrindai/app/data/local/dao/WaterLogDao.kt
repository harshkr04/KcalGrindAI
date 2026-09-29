package com.kcalgrindai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kcalgrindai.app.data.local.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(entry: WaterLogEntity): Long

    @Query("SELECT * FROM water_logs WHERE date = :date ORDER BY loggedAt ASC")
    fun observeWaterLogsByDate(date: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE date = :date ORDER BY loggedAt ASC")
    suspend fun getWaterLogsByDate(date: String): List<WaterLogEntity>

    @Query("SELECT * FROM water_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, loggedAt ASC")
    fun observeWaterLogsInRange(startDate: String, endDate: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, loggedAt ASC")
    suspend fun getWaterLogsInRange(startDate: String, endDate: String): List<WaterLogEntity>

    @Query("SELECT * FROM water_logs ORDER BY date ASC, loggedAt ASC")
    suspend fun getAllWaterLogs(): List<WaterLogEntity>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_logs WHERE date = :date")
    fun observeTotalWaterByDate(date: String): Flow<Int>

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteWaterLogById(id: Long)

    @Query("DELETE FROM water_logs WHERE date = :date")
    suspend fun deleteWaterLogsByDate(date: String)
}
