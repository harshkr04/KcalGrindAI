package com.kcalgrindai.app.feature.settings.widget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.core.util.WidgetPinningHelper
import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WidgetSizeTab {
    DETAILED_4X2,
    QUICK_2X2
}

data class WidgetSettingsUiState(
    val isWidgetAdded: Boolean = false,
    val installedWidgetCount: Int = 0,
    val isPrivacyEnabled: Boolean = false,
    val selectedTab: WidgetSizeTab = WidgetSizeTab.DETAILED_4X2,
    val isPinningSupported: Boolean = true
)

@HiltViewModel
class WidgetSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(WidgetSizeTab.DETAILED_4X2)
    private val _refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<WidgetSettingsUiState> = combine(
        userPreferencesRepository.widgetPrivacyEnabled,
        _selectedTab,
        _refreshTrigger
    ) { isPrivacy, selectedTab, _ ->
        val count = WidgetPinningHelper.getInstalledWidgetCount(context)
        val isAdded = count > 0
        val isSupported = WidgetPinningHelper.isPinningSupported(context)
        WidgetSettingsUiState(
            isWidgetAdded = isAdded,
            installedWidgetCount = count,
            isPrivacyEnabled = isPrivacy,
            selectedTab = selectedTab,
            isPinningSupported = isSupported
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WidgetSettingsUiState(
            isWidgetAdded = WidgetPinningHelper.isWidgetInstalled(context),
            installedWidgetCount = WidgetPinningHelper.getInstalledWidgetCount(context),
            isPinningSupported = WidgetPinningHelper.isPinningSupported(context)
        )
    )

    fun refreshStatus() {
        _refreshTrigger.value += 1
    }

    fun selectTab(tab: WidgetSizeTab) {
        _selectedTab.value = tab
    }

    fun togglePrivacy(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setWidgetPrivacyEnabled(enabled)
        }
    }

    fun requestPinWidget(): Boolean {
        val result = WidgetPinningHelper.requestPinWidget(context)
        refreshStatus()
        return result
    }
}
