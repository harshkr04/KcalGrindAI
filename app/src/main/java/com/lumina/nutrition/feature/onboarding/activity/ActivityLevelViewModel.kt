package com.lumina.nutrition.feature.onboarding.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.nutrition.domain.model.ActivityLevel
import com.lumina.nutrition.feature.onboarding.state.OnboardingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityLevelUiState(
    val selectedActivity: ActivityLevel = ActivityLevel.MODERATE,
    val availableActivities: List<ActivityLevel> = ActivityLevel.entries
)

@HiltViewModel
class ActivityLevelViewModel @Inject constructor(
    private val sessionManager: OnboardingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ActivityLevelUiState(selectedActivity = sessionManager.getSnapshot().activityLevel)
    )
    val uiState: StateFlow<ActivityLevelUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.draftState.collect { draft ->
                _uiState.update { it.copy(selectedActivity = draft.activityLevel) }
            }
        }
    }

    fun selectActivity(activity: ActivityLevel) {
        _uiState.update { it.copy(selectedActivity = activity) }
        sessionManager.updateActivityLevel(activity)
    }
}
