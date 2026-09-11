package com.lumina.nutrition.domain.repository

import com.lumina.nutrition.domain.model.FoodItem
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    suspend fun saveFood(food: FoodItem): Long
    suspend fun saveFoods(foods: List<FoodItem>): List<Long>
    suspend fun getFoodById(id: Long): FoodItem?
    suspend fun getFoodByBarcode(barcode: String): FoodItem?
    suspend fun searchFoods(query: String, limit: Int = 30): List<FoodItem>
    suspend fun searchFoodsOnline(query: String): Result<List<FoodItem>>
    suspend fun lookupBarcode(barcode: String): Result<FoodItem?>
    fun observeFoodsByName(query: String): Flow<List<FoodItem>>
    fun observeUserCreatedFoods(): Flow<List<FoodItem>>
    fun observeAllFoods(): Flow<List<FoodItem>>
    suspend fun deleteFood(food: FoodItem)
    suspend fun deleteFoodById(id: Long)
}
