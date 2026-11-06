package com.kcalgrindai.app.core.notification

import com.kcalgrindai.app.data.local.dao.MealLogDao
import com.kcalgrindai.app.data.local.dao.WaterLogDao
import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderWorkerEntryPoint {
    fun waterLogDao(): WaterLogDao
    fun mealLogDao(): MealLogDao
    fun userPreferencesRepository(): UserPreferencesRepository
}
