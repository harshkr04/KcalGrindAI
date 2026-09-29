package com.kcalgrindai.app.domain.repository

import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.MealLog
import com.kcalgrindai.app.domain.model.MealWithFoodItems
import kotlinx.coroutines.flow.Flow

interface MealLogRepository {
    suspend fun saveMeal(meal: MealLog, items: List<FoodLogItem>): Long
    suspend fun updateMeal(meal: MealLog)
    suspend fun deleteMeal(mealId: Long)
    suspend fun getMealById(mealId: Long): MealWithFoodItems?
    fun observeMealsWithItemsByDate(date: String): Flow<List<MealWithFoodItems>>
    fun observeMealsWithItemsInRange(startDate: String, endDate: String): Flow<List<MealWithFoodItems>>
    suspend fun saveFoodLogItem(item: FoodLogItem): Long
    suspend fun updateFoodLogItem(item: FoodLogItem)
    suspend fun deleteFoodLogItem(itemId: Long)
    fun observeItemsForMeal(mealId: Long): Flow<List<FoodLogItem>>
    suspend fun getUnsyncedMeals(): List<MealLog>
    fun observeStreakDays(): Flow<Int>
    suspend fun getStreakDays(): Int
}
