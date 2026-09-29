package com.kcalgrindai.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.kcalgrindai.app.domain.model.ThemeMode
import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("kcalgrind_user_prefs", Context.MODE_PRIVATE)
    }

    private val _notificationsEnabled: MutableStateFlow<Boolean> by lazy {
        MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true))
    }
    override val notificationsEnabled: Flow<Boolean>
        get() = _notificationsEnabled.asStateFlow()

    private val _themeMode: MutableStateFlow<ThemeMode> by lazy {
        val stored = prefs.getString(KEY_THEME_MODE, null)
        val mode = if (stored != null) {
            try {
                ThemeMode.valueOf(stored)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        } else if (prefs.contains(KEY_DARK_MODE_ENABLED)) {
            if (prefs.getBoolean(KEY_DARK_MODE_ENABLED, false)) ThemeMode.DARK else ThemeMode.SYSTEM
        } else {
            ThemeMode.SYSTEM
        }
        MutableStateFlow(mode)
    }
    override val themeMode: Flow<ThemeMode>
        get() = _themeMode.asStateFlow()

    private val _darkModeEnabled: MutableStateFlow<Boolean> by lazy {
        MutableStateFlow(_themeMode.value == ThemeMode.DARK)
    }
    override val darkModeEnabled: Flow<Boolean>
        get() = _darkModeEnabled.asStateFlow()

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    override suspend fun setDarkModeEnabled(enabled: Boolean) {
        setThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.SYSTEM)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        prefs.edit()
            .putString(KEY_THEME_MODE, mode.name)
            .putBoolean(KEY_DARK_MODE_ENABLED, mode == ThemeMode.DARK)
            .apply()
        _themeMode.value = mode
        _darkModeEnabled.value = (mode == ThemeMode.DARK)
    }

    private val _selectedAvatar: MutableStateFlow<String?> by lazy {
        MutableStateFlow(prefs.getString(KEY_SELECTED_AVATAR, null))
    }
    override val selectedAvatar: Flow<String?>
        get() = _selectedAvatar.asStateFlow()

    override suspend fun setSelectedAvatar(avatar: String?) {
        prefs.edit().putString(KEY_SELECTED_AVATAR, avatar).apply()
        _selectedAvatar.value = avatar
    }

    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DARK_MODE_ENABLED = "dark_mode_enabled"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SELECTED_AVATAR = "selected_avatar"
    }
}
