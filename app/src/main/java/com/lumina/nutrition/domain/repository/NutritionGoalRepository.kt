package com.lumina.nutrition.domain.repository

import com.lumina.nutrition.domain.model.NutritionGoal
import kotlinx.coroutines.flow.Flow

interface NutritionGoalRepository {
    suspend fun saveGoal(goal: NutritionGoal): Long
    suspend fun getGoalByUserId(userId: Long): NutritionGoal?
    suspend fun getLatestGoal(): NutritionGoal?
    fun observeGoalByUserId(userId: Long): Flow<NutritionGoal?>
    fun observeLatestGoal(): Flow<NutritionGoal?>
    suspend fun deleteGoalByUserId(userId: Long)
}
