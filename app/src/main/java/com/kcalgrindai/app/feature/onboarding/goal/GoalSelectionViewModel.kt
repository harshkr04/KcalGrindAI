package com.kcalgrindai.app.feature.onboarding.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.domain.model.GoalType
import com.kcalgrindai.app.feature.onboarding.state.OnboardingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalSelectionUiState(
    val selectedGoal: GoalType = GoalType.LOSE,
    val availableGoals: List<GoalType> = GoalType.entries
)

@HiltViewModel
class GoalSelectionViewModel @Inject constructor(
    private val sessionManager: OnboardingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GoalSelectionUiState(selectedGoal = sessionManager.getSnapshot().goal)
    )
    val uiState: StateFlow<GoalSelectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.draftState.collect { draft ->
                _uiState.update { it.copy(selectedGoal = draft.goal) }
            }
        }
    }

    fun selectGoal(goal: GoalType) {
        _uiState.update { it.copy(selectedGoal = goal) }
        sessionManager.updateGoal(goal)
    }
}
