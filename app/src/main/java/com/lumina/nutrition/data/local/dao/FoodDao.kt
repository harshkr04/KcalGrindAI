package com.lumina.nutrition.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.lumina.nutrition.data.local.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>): List<Long>

    @Upsert
    suspend fun upsertFood(food: FoodEntity): Long

    @Update
    suspend fun updateFood(food: FoodEntity)

    @Query("SELECT * FROM foods WHERE id = :id LIMIT 1")
    suspend fun getFoodById(id: Long): FoodEntity?

    @Query("SELECT * FROM foods WHERE barcodeUpc = :barcode LIMIT 1")
    suspend fun getFoodByBarcode(barcode: String): FoodEntity?

    @Query("SELECT * FROM foods WHERE source = :source AND externalId = :externalId LIMIT 1")
    suspend fun getFoodByExternalId(source: String, externalId: String): FoodEntity?

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY createdAt DESC LIMIT :limit")
    suspend fun searchFoods(query: String, limit: Int = 30): List<FoodEntity>

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun observeFoodsByName(query: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE isUserCreated = 1 ORDER BY createdAt DESC")
    fun observeUserCreatedFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods ORDER BY createdAt DESC")
    fun observeAllFoods(): Flow<List<FoodEntity>>

    @Delete
    suspend fun deleteFood(food: FoodEntity)

    @Query("DELETE FROM foods WHERE id = :id")
    suspend fun deleteFoodById(id: Long)
}
