package com.lumina.nutrition.feature.onboarding.allergies

import androidx.lifecycle.ViewModel
import com.lumina.nutrition.feature.onboarding.state.OnboardingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AllergenOption(val id: String, val label: String, val icon: String)

data class AllergiesUiState(
    val selectedAllergies: List<String> = emptyList(),
    val availableAllergens: List<AllergenOption> = listOf(
        AllergenOption("peanuts", "Peanuts", "🥜"),
        AllergenOption("tree_nuts", "Tree nuts", "🌰"),
        AllergenOption("milk", "Milk / Dairy", "🥛"),
        AllergenOption("eggs", "Eggs", "🥚"),
        AllergenOption("soy", "Soy", "🌱"),
        AllergenOption("wheat", "Wheat / Gluten", "🌾"),
        AllergenOption("fish", "Fish", "🐟"),
        AllergenOption("shellfish", "Shellfish", "🦐"),
        AllergenOption("sesame", "Sesame", "🌿")
    )
)

@HiltViewModel
class AllergiesViewModel @Inject constructor(
    private val sessionManager: OnboardingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AllergiesUiState(selectedAllergies = sessionManager.getSnapshot().allergies)
    )
    val uiState: StateFlow<AllergiesUiState> = _uiState.asStateFlow()

    fun toggleAllergen(allergenId: String) {
        _uiState.update { current ->
            val updated = current.selectedAllergies.toMutableList()
            if (updated.contains(allergenId)) {
                updated.remove(allergenId)
            } else {
                updated.add(allergenId)
            }
            sessionManager.updateAllergies(updated)
            current.copy(selectedAllergies = updated)
        }
    }
}
