package com.kcalgrindai.app.feature.onboarding.diet

import androidx.lifecycle.ViewModel
import com.kcalgrindai.app.feature.onboarding.state.OnboardingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class DietOption(val id: String, val label: String, val exclusive: Boolean = false)

data class DietPreferencesUiState(
    val selectedDiets: List<String> = listOf("none"),
    val availableDiets: List<DietOption> = listOf(
        DietOption("none", "No preference", exclusive = true),
        DietOption("vegetarian", "Vegetarian"),
        DietOption("vegan", "Vegan"),
        DietOption("high_protein", "High protein"),
        DietOption("low_carb", "Low carb"),
        DietOption("balanced", "Balanced"),
        DietOption("mediterranean", "Mediterranean"),
        DietOption("keto", "Keto"),
        DietOption("pescatarian", "Pescatarian")
    )
)

@HiltViewModel
class DietPreferencesViewModel @Inject constructor(
    private val sessionManager: OnboardingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DietPreferencesUiState(selectedDiets = sessionManager.getSnapshot().dietTags)
    )
    val uiState: StateFlow<DietPreferencesUiState> = _uiState.asStateFlow()

    fun toggleDiet(dietId: String) {
        _uiState.update { current ->
            val updated = if (dietId == "none") {
                listOf("none")
            } else {
                val currentWithoutNone = current.selectedDiets.filter { it != "none" }.toMutableList()
                if (currentWithoutNone.contains(dietId)) {
                    currentWithoutNone.remove(dietId)
                    if (currentWithoutNone.isEmpty()) listOf("none") else currentWithoutNone
                } else {
                    currentWithoutNone.add(dietId)
                    currentWithoutNone
                }
            }
            sessionManager.updateDietTags(updated)
            current.copy(selectedDiets = updated)
        }
    }
}
