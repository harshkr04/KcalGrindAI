package com.lumina.nutrition.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.lumina.nutrition.data.local.entity.MealLogEntity
import com.lumina.nutrition.data.local.model.MealWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(mealLog: MealLogEntity): Long

    @Upsert
    suspend fun upsertMealLog(mealLog: MealLogEntity): Long

    @Update
    suspend fun updateMealLog(mealLog: MealLogEntity)

    @Query("SELECT * FROM meal_logs WHERE id = :id LIMIT 1")
    suspend fun getMealLogById(id: Long): MealLogEntity?

    @Transaction
    @Query("SELECT * FROM meal_logs WHERE id = :id LIMIT 1")
    suspend fun getMealWithItemsById(id: Long): MealWithItems?

    @Query("SELECT * FROM meal_logs WHERE date = :date ORDER BY loggedAt ASC")
    fun observeMealsByDate(date: String): Flow<List<MealLogEntity>>

    @Transaction
    @Query("SELECT * FROM meal_logs WHERE date = :date ORDER BY loggedAt ASC")
    fun observeMealsWithItemsByDate(date: String): Flow<List<MealWithItems>>

    @Transaction
    @Query("SELECT * FROM meal_logs WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, loggedAt ASC")
    fun observeMealsWithItemsInRange(startDate: String, endDate: String): Flow<List<MealWithItems>>

    @Query("SELECT * FROM meal_logs ORDER BY loggedAt ASC")
    suspend fun getAllMealLogs(): List<MealLogEntity>

    @Query("SELECT * FROM meal_logs WHERE synced = 0")
    suspend fun getUnsyncedMealLogs(): List<MealLogEntity>

    @Delete
    suspend fun deleteMealLog(mealLog: MealLogEntity)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMealLogById(id: Long)
}
