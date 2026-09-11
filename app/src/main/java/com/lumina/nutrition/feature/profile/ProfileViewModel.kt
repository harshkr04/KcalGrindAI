package com.lumina.nutrition.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.nutrition.domain.model.GoalType
import com.lumina.nutrition.domain.model.NutritionGoal
import com.lumina.nutrition.domain.model.UnitSystem
import com.lumina.nutrition.domain.model.UserProfile
import com.lumina.nutrition.domain.repository.NutritionGoalRepository
import com.lumina.nutrition.domain.repository.SyncRepository
import com.lumina.nutrition.domain.repository.SyncResult
import com.lumina.nutrition.domain.repository.UserProfileRepository
import com.lumina.nutrition.domain.usecase.NutritionCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Content(
        val profile: UserProfile,
        val goal: NutritionGoal?
    ) : ProfileUiState
    data object Empty : ProfileUiState
}

sealed interface SyncState {
    data object Idle : SyncState
    data object Syncing : SyncState
    data class Success(val result: SyncResult) : SyncState
    data class Error(val message: String) : SyncState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val nutritionGoalRepository: NutritionGoalRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    /** Epoch millis of the last successful sync, or 0 if never synced. */
    val lastSyncTimestamp: Long
        get() = syncRepository.getLastSyncTimestamp()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            userProfileRepository.observeProfileWithGoal().collectLatest { pair ->
                if (pair != null) {
                    _uiState.value = ProfileUiState.Content(
                        profile = pair.first,
                        goal = pair.second
                    )
                } else {
                    _uiState.value = ProfileUiState.Empty
                }
            }
        }
    }

    fun updateWeight(newWeightKg: Double) {
        viewModelScope.launch {
            val current = (_uiState.value as? ProfileUiState.Content)?.profile ?: return@launch
            val updated = current.copy(weightKg = newWeightKg, updatedAt = System.currentTimeMillis())
            userProfileRepository.saveProfile(updated)

            // Also recalculate goal targets
            val currentGoal = (_uiState.value as? ProfileUiState.Content)?.goal
            if (currentGoal != null && !currentGoal.isCustom) {
                val calc = NutritionCalculator.calculate(
                    weightKg = newWeightKg,
                    heightCm = updated.heightCm,
                    age = updated.age,
                    goal = updated.goal,
                    activity = updated.activityLevel,
                    goalWeightKg = updated.goalWeightKg,
                    dietTags = updated.dietTags
                )
                val updatedGoal = currentGoal.copy(
                    calories = calc.calories,
                    proteinG = calc.proteinG,
                    carbsG = calc.carbsG,
                    fatG = calc.fatG,
                    waterLiters = calc.waterLiters,
                    waterGlasses = calc.waterGlasses,
                    bmr = calc.bmr,
                    tdee = calc.tdee,
                    updatedAt = System.currentTimeMillis()
                )
                nutritionGoalRepository.saveGoal(updatedGoal)
            }
        }
    }

    fun toggleUnitSystem() {
        viewModelScope.launch {
            val current = (_uiState.value as? ProfileUiState.Content)?.profile ?: return@launch
            val newUnits = if (current.units == UnitSystem.METRIC) UnitSystem.IMPERIAL else UnitSystem.METRIC
            val updated = current.copy(units = newUnits, updatedAt = System.currentTimeMillis())
            userProfileRepository.saveProfile(updated)
        }
    }

    fun updateGoal(newGoal: GoalType) {
        viewModelScope.launch {
            val current = (_uiState.value as? ProfileUiState.Content)?.profile ?: return@launch
            val updated = current.copy(goal = newGoal, updatedAt = System.currentTimeMillis())
            userProfileRepository.saveProfile(updated)

            val currentGoal = (_uiState.value as? ProfileUiState.Content)?.goal
            if (currentGoal != null && !currentGoal.isCustom) {
                val calc = NutritionCalculator.calculate(
                    weightKg = updated.weightKg,
                    heightCm = updated.heightCm,
                    age = updated.age,
                    goal = newGoal,
                    activity = updated.activityLevel,
                    goalWeightKg = updated.goalWeightKg,
                    dietTags = updated.dietTags
                )
                val updatedGoal = currentGoal.copy(
                    calories = calc.calories,
                    proteinG = calc.proteinG,
                    carbsG = calc.carbsG,
                    fatG = calc.fatG,
                    waterLiters = calc.waterLiters,
                    waterGlasses = calc.waterGlasses,
                    bmr = calc.bmr,
                    tdee = calc.tdee,
                    updatedAt = System.currentTimeMillis()
                )
                nutritionGoalRepository.saveGoal(updatedGoal)
            }
        }
    }

    /**
     * Manual "Sync now" action (Phase 10, strategy a).
     * Pushes all local Room data to Supabase, then pulls remote changes.
     */
    fun onSyncNow() {
        if (_syncState.value is SyncState.Syncing) return // debounce
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            syncRepository.fullSync()
                .onSuccess { result ->
                    _syncState.value = SyncState.Success(result)
                }
                .onFailure { error ->
                    _syncState.value = SyncState.Error(
                        error.message ?: "Sync failed. Check your connection."
                    )
                }
        }
    }

    fun dismissSyncResult() {
        _syncState.value = SyncState.Idle
    }
}
