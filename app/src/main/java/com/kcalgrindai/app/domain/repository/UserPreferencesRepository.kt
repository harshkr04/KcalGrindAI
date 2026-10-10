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
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setDarkModeEnabled(enabled: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setSelectedAvatar(avatar: String?)
    suspend fun setWidgetPrivacyEnabled(enabled: Boolean)
    suspend fun setWidgetPromoDismissed(dismissed: Boolean)
}
