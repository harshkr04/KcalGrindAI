package com.kcalgrindai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kcalgrindai.app.data.local.entity.SavedMealEntity
import com.kcalgrindai.app.data.local.entity.SavedMealItemEntity
import com.kcalgrindai.app.data.local.model.SavedMealWithItemsRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedMealDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedMeal(savedMeal: SavedMealEntity): Long

    @Update
    suspend fun updateSavedMeal(savedMeal: SavedMealEntity)

    @Query("UPDATE saved_meals SET name = :newName, updatedAt = :updatedAt WHERE id = :id")
    suspend fun renameSavedMeal(id: Long, newName: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM saved_meals WHERE id = :id")
    suspend fun deleteSavedMealById(id: Long)

    @Delete
    suspend fun deleteSavedMeal(savedMeal: SavedMealEntity)

    @Query("SELECT * FROM saved_meals WHERE id = :id")
    suspend fun getSavedMealById(id: Long): SavedMealEntity?

    @Query("SELECT * FROM saved_meals ORDER BY updatedAt DESC")
    fun observeAllSavedMeals(): Flow<List<SavedMealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedMealItems(items: List<SavedMealItemEntity>)

    @Query("DELETE FROM saved_meal_items WHERE savedMealId = :savedMealId")
    suspend fun deleteItemsForSavedMeal(savedMealId: Long)

    @Query("SELECT * FROM saved_meal_items WHERE savedMealId = :savedMealId")
    suspend fun getItemsForSavedMeal(savedMealId: Long): List<SavedMealItemEntity>

    @Transaction
    @Query("SELECT * FROM saved_meals ORDER BY updatedAt DESC")
    fun observeSavedMealsWithItems(): Flow<List<SavedMealWithItemsRelation>>

    @Transaction
    @Query("SELECT * FROM saved_meals WHERE id = :id")
    suspend fun getSavedMealWithItemsById(id: Long): SavedMealWithItemsRelation?
}
