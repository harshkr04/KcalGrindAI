package com.kcalgrindai.app.domain.repository

import com.kcalgrindai.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val notificationsEnabled: Flow<Boolean>
    val darkModeEnabled: Flow<Boolean>
    val themeMode: Flow<ThemeMode>
    val selectedAvatar: Flow<String?>
    val widgetPrivacyEnabled: Flow<Boolean>
    val widgetPromoDismissed: Flow<Boolean>
    val mealRemindersEnabled: Flow<Boolean>
    val hydrationRemindersEnabled: Flow<Boolean>
    val deductActiveCaloriesEnabled: Flow<Boolean>

    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setDarkModeEnabled(enabled: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setSelectedAvatar(avatar: String?)
    suspend fun setWidgetPrivacyEnabled(enabled: Boolean)
    suspend fun setWidgetPromoDismissed(dismissed: Boolean)
    suspend fun setMealRemindersEnabled(enabled: Boolean)
    suspend fun setHydrationRemindersEnabled(enabled: Boolean)
    suspend fun setDeductActiveCaloriesEnabled(enabled: Boolean)
    suspend fun getLastMealReminderDate(mealType: String): String?
    suspend fun setLastMealReminderDate(mealType: String, date: String)
    suspend fun clearAllPreferences()
}
