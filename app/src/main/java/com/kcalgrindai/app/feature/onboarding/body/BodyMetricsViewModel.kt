package com.kcalgrindai.app.feature.onboarding.body

import androidx.lifecycle.ViewModel
import com.kcalgrindai.app.domain.model.UnitSystem
import com.kcalgrindai.app.domain.usecase.NutritionCalculator
import com.kcalgrindai.app.feature.onboarding.state.OnboardingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class BodyMetricsUiState(
    val units: UnitSystem = UnitSystem.METRIC,
    val weightInput: String = "70",
    val goalWeightInput: String = "65",
    val weightError: String? = null,
    val goalWeightError: String? = null,
    val note: String? = null,
    val isValid: Boolean = true
)

@HiltViewModel
class BodyMetricsViewModel @Inject constructor(
    private val sessionManager: OnboardingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BodyMetricsUiState())
    val uiState: StateFlow<BodyMetricsUiState> = _uiState.asStateFlow()

    init {
        val draft = sessionManager.getSnapshot()
        val wStr = if (draft.units == UnitSystem.METRIC) {
            draft.weightKg.toInt().toString()
        } else {
            NutritionCalculator.kgToLb(draft.weightKg).toString()
        }
        val gwStr = if (draft.units == UnitSystem.METRIC) {
            draft.goalWeightKg.toInt().toString()
        } else {
            NutritionCalculator.kgToLb(draft.goalWeightKg).toString()
        }
        _uiState.value = BodyMetricsUiState(
            units = draft.units,
            weightInput = wStr,
            goalWeightInput = gwStr
        )
        validate()
    }

    fun onWeightChanged(input: String) {
        _uiState.update { it.copy(weightInput = input.filter { c -> c.isDigit() || c == '.' }) }
        validate()
    }

    fun onGoalWeightChanged(input: String) {
        _uiState.update { it.copy(goalWeightInput = input.filter { c -> c.isDigit() || c == '.' }) }
        validate()
    }

    private fun validate() {
        _uiState.update { current ->
            val draft = sessionManager.getSnapshot()
            val wRaw = current.weightInput.toDoubleOrNull()
            val gwRaw = current.goalWeightInput.toDoubleOrNull()

            val weightKg = if (wRaw != null) {
                if (current.units == UnitSystem.METRIC) wRaw else NutritionCalculator.lbToKg(wRaw)
            } else null

            val goalWeightKg = if (gwRaw != null) {
                if (current.units == UnitSystem.METRIC) gwRaw else NutritionCalculator.lbToKg(gwRaw)
            } else null

            var wErr: String? = null
            if (weightKg == null) {
                wErr = "Current weight is required."
            } else if (weightKg < NutritionCalculator.Limits.MIN_WEIGHT_KG || weightKg > NutritionCalculator.Limits.MAX_WEIGHT_KG) {
                wErr = if (current.units == UnitSystem.IMPERIAL) {
                    "Enter a weight between ${NutritionCalculator.kgToLb(NutritionCalculator.Limits.MIN_WEIGHT_KG)} and ${NutritionCalculator.kgToLb(NutritionCalculator.Limits.MAX_WEIGHT_KG)} lb."
                } else {
                    "Enter a weight between ${NutritionCalculator.Limits.MIN_WEIGHT_KG.toInt()} and ${NutritionCalculator.Limits.MAX_WEIGHT_KG.toInt()} kg."
                }
            }

            var gwErr: String? = null
            if (goalWeightKg == null) {
                gwErr = "Goal weight is required."
            } else if (goalWeightKg < NutritionCalculator.Limits.MIN_WEIGHT_KG || goalWeightKg > NutritionCalculator.Limits.MAX_WEIGHT_KG) {
                gwErr = if (current.units == UnitSystem.IMPERIAL) {
                    "Enter a target weight between ${NutritionCalculator.kgToLb(NutritionCalculator.Limits.MIN_WEIGHT_KG)} and ${NutritionCalculator.kgToLb(NutritionCalculator.Limits.MAX_WEIGHT_KG)} lb."
                } else {
                    "Enter a target weight between ${NutritionCalculator.Limits.MIN_WEIGHT_KG.toInt()} and ${NutritionCalculator.Limits.MAX_WEIGHT_KG.toInt()} kg."
                }
            }

            val note = if (wErr == null && gwErr == null && weightKg != null && goalWeightKg != null) {
                NutritionCalculator.goalWeightNote(draft.goal, weightKg, goalWeightKg)
            } else null

            val valid = (wErr == null && gwErr == null && weightKg != null && goalWeightKg != null)
            if (valid) {
                sessionManager.updateBodyMetrics(weightKg, goalWeightKg)
            }

            current.copy(
                weightError = wErr,
                goalWeightError = gwErr,
                note = note,
                isValid = valid
            )
        }
    }
}
