package com.lumina.nutrition.feature.onboarding.target

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.nutrition.domain.usecase.CalculateNutritionGoalUseCase
import com.lumina.nutrition.domain.usecase.NutritionCalculator
import com.lumina.nutrition.feature.onboarding.state.OnboardingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CalorieTargetUiState {
    data object Loading : CalorieTargetUiState
    data class Success(
        val calories: Int,
        val proteinG: Double,
        val carbsG: Double,
        val fatG: Double,
        val waterLiters: Double,
        val waterGlasses: Int,
        val bmr: Double?,
        val tdee: Double?,
        val explanation: String
    ) : CalorieTargetUiState
}

@HiltViewModel
class CalorieTargetViewModel @Inject constructor(
    private val calculateUseCase: CalculateNutritionGoalUseCase,
    private val sessionManager: OnboardingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalorieTargetUiState>(CalorieTargetUiState.Loading)
    val uiState: StateFlow<CalorieTargetUiState> = _uiState.asStateFlow()

    init {
        computeTarget()
    }

    fun computeTarget() {
        viewModelScope.launch {
            _uiState.value = CalorieTargetUiState.Loading
            // Brief animation pause for smooth UX
            delay(300)

            val draft = sessionManager.getSnapshot()
            val result = calculateUseCase.calculateRaw(
                weightKg = draft.weightKg,
                heightCm = draft.heightCm,
                age = draft.age,
                goal = draft.goal,
                activity = draft.activityLevel,
                goalWeightKg = draft.goalWeightKg,
                dietTags = draft.dietTags
            )

            sessionManager.updateCustomMacros(
                calories = result.calories,
                proteinG = result.proteinG,
                carbsG = result.carbsG,
                fatG = result.fatG
            )

            _uiState.value = CalorieTargetUiState.Success(
                calories = result.calories,
                proteinG = result.proteinG,
                carbsG = result.carbsG,
                fatG = result.fatG,
                waterLiters = result.waterLiters,
                waterGlasses = result.waterGlasses,
                bmr = result.bmr,
                tdee = result.tdee,
                explanation = NutritionCalculator.explain(result)
            )
        }
    }
}
