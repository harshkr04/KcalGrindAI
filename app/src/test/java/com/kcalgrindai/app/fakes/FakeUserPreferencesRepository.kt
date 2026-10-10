package com.kcalgrindai.app.fakes

import com.kcalgrindai.app.domain.model.ThemeMode
import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeUserPreferencesRepository(
    initialPrivacy: Boolean = false,
    initialPromoDismissed: Boolean = false
) : UserPreferencesRepository {

    private val _notificationsEnabled = MutableStateFlow(true)
    override val notificationsEnabled: Flow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _darkModeEnabled = MutableStateFlow(false)
    override val darkModeEnabled: Flow<Boolean> = _darkModeEnabled.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    override val themeMode: Flow<ThemeMode> = _themeMode.asStateFlow()

    private val _selectedAvatar = MutableStateFlow<String?>(null)
    override val selectedAvatar: Flow<String?> = _selectedAvatar.asStateFlow()

    private val _widgetPrivacyEnabled = MutableStateFlow(initialPrivacy)
    override val widgetPrivacyEnabled: Flow<Boolean> = _widgetPrivacyEnabled.asStateFlow()

    private val _widgetPromoDismissed = MutableStateFlow(initialPromoDismissed)
    override val widgetPromoDismissed: Flow<Boolean> = _widgetPromoDismissed.asStateFlow()

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    override suspend fun setDarkModeEnabled(enabled: Boolean) {
        _darkModeEnabled.value = enabled
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    override suspend fun setSelectedAvatar(avatar: String?) {
        _selectedAvatar.value = avatar
    }

    override suspend fun setWidgetPrivacyEnabled(enabled: Boolean) {
        _widgetPrivacyEnabled.value = enabled
    }

    override suspend fun setWidgetPromoDismissed(dismissed: Boolean) {
        _widgetPromoDismissed.value = dismissed
    }
}
