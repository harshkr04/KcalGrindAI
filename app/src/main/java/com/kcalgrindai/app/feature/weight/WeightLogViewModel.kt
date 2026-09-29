package com.kcalgrindai.app.feature.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.domain.model.UnitSystem
import com.kcalgrindai.app.domain.model.WeightEntry
import com.kcalgrindai.app.domain.repository.NutritionGoalRepository
import com.kcalgrindai.app.domain.repository.UserProfileRepository
import com.kcalgrindai.app.domain.repository.WeightRepository
import com.kcalgrindai.app.domain.usecase.CalculateNutritionGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

data class WeightLogUiState(
    val isLoading: Boolean = true,
    val weightInput: String = "",
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val unitLabel: String = "kg",
    val dateIso: String = LocalDate.now().toString(),
    val dateFormatted: String = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
    val note: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSavedSuccess: Boolean = false
)

@HiltViewModel
class WeightLogViewModel @Inject constructor(
    private val weightRepository: WeightRepository,
    private val userProfileRepository: UserProfileRepository,
    private val nutritionGoalRepository: NutritionGoalRepository,
    private val calculateNutritionGoalUseCase: CalculateNutritionGoalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightLogUiState())
    val uiState: StateFlow<WeightLogUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val profile = userProfileRepository.observeProfile().firstOrNull()
            val unit = profile?.units ?: UnitSystem.METRIC
            val isMetric = unit == UnitSystem.METRIC
            val unitLabel = if (isMetric) "kg" else "lbs"

            val latestWeight = weightRepository.getLatestWeightEntry()
            val prefillWeight = if (latestWeight != null) {
                if (isMetric) {
                    String.format(Locale.US, "%.1f", latestWeight.weightKg)
                } else {
                    String.format(Locale.US, "%.1f", latestWeight.weightKg * 2.20462)
                }
            } else if (profile != null) {
                if (isMetric) {
                    String.format(Locale.US, "%.1f", profile.weightKg)
                } else {
                    String.format(Locale.US, "%.1f", profile.weightKg * 2.20462)
                }
            } else {
                ""
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    weightInput = prefillWeight,
                    unitSystem = unit,
                    unitLabel = unitLabel
                )
            }
        }
    }

    fun onWeightChange(newWeight: String) {
        // Allow digits and decimal point
        val sanitized = newWeight.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(weightInput = sanitized, errorMessage = null) }
    }

    fun onNoteChange(newNote: String) {
        _uiState.update { it.copy(note = newNote) }
    }

    fun saveWeight(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val weightVal = currentState.weightInput.toDoubleOrNull()
        if (weightVal == null || weightVal <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid weight") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val weightKg = if (currentState.unitSystem == UnitSystem.METRIC) {
                    weightVal
                } else {
                    weightVal / 2.20462
                }

                val roundedKg = (weightKg * 10.0).roundToInt() / 10.0
                val entry = WeightEntry(
                    weightKg = roundedKg,
                    date = currentState.dateIso,
                    note = currentState.note.trim().ifEmpty { null },
                    loggedAt = System.currentTimeMillis()
                )

                weightRepository.saveWeightEntry(entry)

                // Sync new weight to UserProfile and recalculate nutrition target if not custom
                val currentProfile = userProfileRepository.observeProfile().firstOrNull()
                if (currentProfile != null) {
                    val updatedProfile = currentProfile.copy(
                        weightKg = roundedKg,
                        updatedAt = System.currentTimeMillis()
                    )
                    userProfileRepository.saveProfile(updatedProfile)

                    val currentGoal = nutritionGoalRepository.getLatestGoal()
                    if (currentGoal == null || !currentGoal.isCustom) {
                        val newGoal = calculateNutritionGoalUseCase(updatedProfile)
                        nutritionGoalRepository.saveGoal(newGoal.copy(id = currentGoal?.id ?: 0L))
                    }
                }

                _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.message ?: "Failed to save weight")
                }
            }
        }
    }
}
