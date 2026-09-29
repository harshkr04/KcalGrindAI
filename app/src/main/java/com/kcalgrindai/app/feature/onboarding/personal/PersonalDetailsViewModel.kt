package com.kcalgrindai.app.feature.onboarding.personal

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

data class PersonalDetailsUiState(
    val nameInput: String = "",
    val units: UnitSystem = UnitSystem.METRIC,
    val ageInput: String = "28",
    val heightCmInput: String = "175",
    val heightFtInput: String = "5",
    val heightInInput: String = "9",
    val ageError: String? = null,
    val heightError: String? = null,
    val isValid: Boolean = true
)

@HiltViewModel
class PersonalDetailsViewModel @Inject constructor(
    private val sessionManager: OnboardingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalDetailsUiState())
    val uiState: StateFlow<PersonalDetailsUiState> = _uiState.asStateFlow()

    init {
        val draft = sessionManager.getSnapshot()
        val (ft, inch) = NutritionCalculator.cmToFtIn(draft.heightCm)
        _uiState.value = PersonalDetailsUiState(
            nameInput = draft.firstName,
            units = draft.units,
            ageInput = draft.age.toString(),
            heightCmInput = draft.heightCm.toInt().toString(),
            heightFtInput = ft.toString(),
            heightInInput = inch.toString()
        )
        validate()
    }

    fun onNameChanged(input: String) {
        _uiState.update { it.copy(nameInput = input) }
        validate()
    }

    fun setUnits(units: UnitSystem) {
        _uiState.update { current ->
            if (units == UnitSystem.IMPERIAL && current.units == UnitSystem.METRIC) {
                val cm = current.heightCmInput.toDoubleOrNull() ?: 175.0
                val (ft, inch) = NutritionCalculator.cmToFtIn(cm)
                current.copy(units = units, heightFtInput = ft.toString(), heightInInput = inch.toString())
            } else if (units == UnitSystem.METRIC && current.units == UnitSystem.IMPERIAL) {
                val ft = current.heightFtInput.toIntOrNull() ?: 5
                val inch = current.heightInInput.toIntOrNull() ?: 9
                val cm = NutritionCalculator.ftInToCm(ft, inch)
                current.copy(units = units, heightCmInput = cm.toInt().toString())
            } else {
                current.copy(units = units)
            }
        }
        validate()
    }

    fun onAgeChanged(input: String) {
        _uiState.update { it.copy(ageInput = input.filter { c -> c.isDigit() }) }
        validate()
    }

    fun onHeightCmChanged(input: String) {
        _uiState.update { it.copy(heightCmInput = input.filter { c -> c.isDigit() || c == '.' }) }
        validate()
    }

    fun onHeightFtChanged(input: String) {
        _uiState.update { it.copy(heightFtInput = input.filter { c -> c.isDigit() }) }
        validate()
    }

    fun onHeightInChanged(input: String) {
        _uiState.update { it.copy(heightInInput = input.filter { c -> c.isDigit() }) }
        validate()
    }

    private fun validate() {
        _uiState.update { current ->
            var ageErr: String? = null
            val age = current.ageInput.toIntOrNull()
            if (age == null) {
                ageErr = "Age is required."
            } else if (age < NutritionCalculator.Limits.MIN_AGE) {
                ageErr = "Kcal Grind AI is for ages ${NutritionCalculator.Limits.MIN_AGE} and up."
            } else if (age > NutritionCalculator.Limits.MAX_AGE) {
                ageErr = "Enter an age of ${NutritionCalculator.Limits.MAX_AGE} or below."
            }

            var heightErr: String? = null
            val heightCm = if (current.units == UnitSystem.METRIC) {
                current.heightCmInput.toDoubleOrNull()
            } else {
                val ft = current.heightFtInput.toIntOrNull() ?: 0
                val inch = current.heightInInput.toIntOrNull() ?: 0
                NutritionCalculator.ftInToCm(ft, inch)
            }

            if (heightCm == null || heightCm == 0.0) {
                heightErr = "Height is required."
            } else if (heightCm < NutritionCalculator.Limits.MIN_HEIGHT_CM || heightCm > NutritionCalculator.Limits.MAX_HEIGHT_CM) {
                heightErr = if (current.units == UnitSystem.IMPERIAL) {
                    "Enter a height between 3′3″ and 8′2″."
                } else {
                    "Enter a height between ${NutritionCalculator.Limits.MIN_HEIGHT_CM.toInt()} and ${NutritionCalculator.Limits.MAX_HEIGHT_CM.toInt()} cm."
                }
            }

            val valid = (ageErr == null && heightErr == null && age != null && heightCm != null)
            if (valid) {
                sessionManager.updatePersonalDetails(age, heightCm, current.units, current.nameInput.trim())
            }

            current.copy(
                ageError = ageErr,
                heightError = heightErr,
                isValid = valid
            )
        }
    }
}
