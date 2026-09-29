package com.kcalgrindai.app.domain.repository

import com.kcalgrindai.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val notificationsEnabled: Flow<Boolean>
    val darkModeEnabled: Flow<Boolean>
    val themeMode: Flow<ThemeMode>
    val selectedAvatar: Flow<String?>
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setDarkModeEnabled(enabled: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setSelectedAvatar(avatar: String?)
}
