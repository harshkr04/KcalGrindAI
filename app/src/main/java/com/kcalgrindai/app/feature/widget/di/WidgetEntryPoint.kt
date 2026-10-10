package com.kcalgrindai.app.feature.widget.di

import com.kcalgrindai.app.domain.mapper.WidgetStateMapper
import com.kcalgrindai.app.domain.repository.AuthRepository
import com.kcalgrindai.app.domain.repository.MealLogRepository
import com.kcalgrindai.app.domain.repository.NutritionGoalRepository
import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import com.kcalgrindai.app.domain.repository.UserProfileRepository
import com.kcalgrindai.app.domain.repository.WaterLogRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun mealLogRepository(): MealLogRepository
    fun waterLogRepository(): WaterLogRepository
    fun nutritionGoalRepository(): NutritionGoalRepository
    fun userProfileRepository(): UserProfileRepository
    fun userPreferencesRepository(): UserPreferencesRepository
    fun authRepository(): AuthRepository
    fun widgetStateMapper(): WidgetStateMapper
}
