package com.kcalgrindai.app.domain.repository

import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.model.SavedMealWithItems
import kotlinx.coroutines.flow.Flow

interface SavedMealRepository {
    suspend fun saveMealAsTemplate(
        name: String,
        mealType: MealType,
        items: List<FoodLogItem>
    ): Long

    suspend fun renameSavedMeal(id: Long, newName: String)

    suspend fun deleteSavedMeal(id: Long)

    fun observeSavedMeals(): Flow<List<SavedMealWithItems>>

    suspend fun getSavedMealById(id: Long): SavedMealWithItems?

    suspend fun logSavedMeal(
        savedMealId: Long,
        targetDate: String,
        targetMealType: MealType
    ): Long
}
