package com.kcalgrindai.app.feature.onboarding.goalsetting

import androidx.lifecycle.ViewModel
import com.kcalgrindai.app.domain.usecase.NutritionCalculator
import com.kcalgrindai.app.feature.onboarding.state.OnboardingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class GoalSettingUiState(
    val calories: Int = 2000,
    val proteinG: Double = 140.0,
    val carbsG: Double = 220.0,
    val fatG: Double = 60.0,
    val sumCalories: Int = 2000,
    val diffCalories: Int = 0,
    val withinTolerance: Boolean = true,
    val proteinPct: Int = 28,
    val carbsPct: Int = 44,
    val fatPct: Int = 28
)

@HiltViewModel
class GoalSettingViewModel @Inject constructor(
    private val sessionManager: OnboardingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalSettingUiState())
    val uiState: StateFlow<GoalSettingUiState> = _uiState.asStateFlow()

    init {
        val draft = sessionManager.getSnapshot()
        val cals = draft.customCalories ?: 2000
        val p = draft.customProteinG ?: 140.0
        val c = draft.customCarbsG ?: 220.0
        val f = draft.customFatG ?: 60.0

        val check = NutritionCalculator.checkMacros(cals, p, c, f)
        _uiState.value = GoalSettingUiState(
            calories = cals,
            proteinG = p,
            carbsG = c,
            fatG = f,
            sumCalories = check.sumCalories,
            diffCalories = check.diffCalories,
            withinTolerance = check.withinTolerance,
            proteinPct = check.proteinPct,
            carbsPct = check.carbsPct,
            fatPct = check.fatPct
        )
    }

    fun onProteinChanged(grams: Double) {
        _uiState.update { current ->
            recalculate(current.copy(proteinG = grams))
        }
    }

    fun onCarbsChanged(grams: Double) {
        _uiState.update { current ->
            recalculate(current.copy(carbsG = grams))
        }
    }

    fun onFatChanged(grams: Double) {
        _uiState.update { current ->
            recalculate(current.copy(fatG = grams))
        }
    }

    fun onConfirm() {
        val current = _uiState.value
        sessionManager.updateCustomMacros(
            calories = current.calories,
            proteinG = current.proteinG,
            carbsG = current.carbsG,
            fatG = current.fatG
        )
    }

    private fun recalculate(state: GoalSettingUiState): GoalSettingUiState {
        val check = NutritionCalculator.checkMacros(
            state.calories,
            state.proteinG,
            state.carbsG,
            state.fatG
        )
        return state.copy(
            sumCalories = check.sumCalories,
            diffCalories = check.diffCalories,
            withinTolerance = check.withinTolerance,
            proteinPct = check.proteinPct,
            carbsPct = check.carbsPct,
            fatPct = check.fatPct
        )
    }
}
