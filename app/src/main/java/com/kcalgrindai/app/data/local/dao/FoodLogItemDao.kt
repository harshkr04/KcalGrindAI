package com.kcalgrindai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.kcalgrindai.app.data.local.entity.FoodLogItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: FoodLogItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<FoodLogItemEntity>): List<Long>

    @Upsert
    suspend fun upsertItem(item: FoodLogItemEntity): Long

    @Update
    suspend fun updateItem(item: FoodLogItemEntity)

    @Query("SELECT * FROM food_log_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): FoodLogItemEntity?

    @Query("SELECT * FROM food_log_items ORDER BY id ASC")
    suspend fun getAllFoodLogItems(): List<FoodLogItemEntity>

    @Query("SELECT * FROM food_log_items WHERE mealLogId = :mealLogId")
    suspend fun getItemsForMeal(mealLogId: Long): List<FoodLogItemEntity>

    @Query("SELECT * FROM food_log_items WHERE mealLogId = :mealLogId")
    fun observeItemsForMeal(mealLogId: Long): Flow<List<FoodLogItemEntity>>

    @Delete
    suspend fun deleteItem(item: FoodLogItemEntity)

    @Query("DELETE FROM food_log_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM food_log_items WHERE mealLogId = :mealLogId")
    suspend fun deleteItemsForMeal(mealLogId: Long)
}
