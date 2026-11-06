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

    private val _mealRemindersEnabled = MutableStateFlow(true)
    override val mealRemindersEnabled: Flow<Boolean> = _mealRemindersEnabled.asStateFlow()

    override suspend fun setMealRemindersEnabled(enabled: Boolean) {
        _mealRemindersEnabled.value = enabled
    }

    private val _hydrationRemindersEnabled = MutableStateFlow(true)
    override val hydrationRemindersEnabled: Flow<Boolean> = _hydrationRemindersEnabled.asStateFlow()

    override suspend fun setHydrationRemindersEnabled(enabled: Boolean) {
        _hydrationRemindersEnabled.value = enabled
    }

    private val _deductActiveCaloriesEnabled = MutableStateFlow(false)
    override val deductActiveCaloriesEnabled: Flow<Boolean> = _deductActiveCaloriesEnabled.asStateFlow()

    override suspend fun setDeductActiveCaloriesEnabled(enabled: Boolean) {
        _deductActiveCaloriesEnabled.value = enabled
    }

    var wasCleared: Boolean = false

    private val lastMealReminderDates = mutableMapOf<String, String>()

    override suspend fun getLastMealReminderDate(mealType: String): String? {
        return lastMealReminderDates[mealType.lowercase()]
    }

    override suspend fun setLastMealReminderDate(mealType: String, date: String) {
        lastMealReminderDates[mealType.lowercase()] = date
    }

    override suspend fun clearAllPreferences() {
        lastMealReminderDates.clear()
        wasCleared = true
        _notificationsEnabled.value = true
        _darkModeEnabled.value = false
        _themeMode.value = ThemeMode.SYSTEM
        _selectedAvatar.value = null
        _widgetPrivacyEnabled.value = false
        _widgetPromoDismissed.value = false
    }
}
