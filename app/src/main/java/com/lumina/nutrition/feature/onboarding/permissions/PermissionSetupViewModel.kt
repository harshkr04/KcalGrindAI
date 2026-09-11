package com.lumina.nutrition.feature.onboarding.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.nutrition.domain.model.NutritionGoal
import com.lumina.nutrition.domain.model.TargetBasis
import com.lumina.nutrition.domain.model.UserProfile
import com.lumina.nutrition.domain.repository.UserProfileRepository
import com.lumina.nutrition.domain.usecase.NutritionCalculator
import com.lumina.nutrition.feature.onboarding.state.OnboardingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PermissionSetupUiState(
    val cameraGranted: Boolean = false,
    val micGranted: Boolean = false,
    val notificationGranted: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class PermissionSetupViewModel @Inject constructor(
    private val sessionManager: OnboardingSessionManager,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionSetupUiState())
    val uiState: StateFlow<PermissionSetupUiState> = _uiState.asStateFlow()

    fun updateCameraPermission(granted: Boolean) {
        _uiState.update { it.copy(cameraGranted = granted) }
        sessionManager.updatePermissions(
            camera = granted,
            mic = _uiState.value.micGranted,
            notification = _uiState.value.notificationGranted
        )
    }

    fun updateMicPermission(granted: Boolean) {
        _uiState.update { it.copy(micGranted = granted) }
        sessionManager.updatePermissions(
            camera = _uiState.value.cameraGranted,
            mic = granted,
            notification = _uiState.value.notificationGranted
        )
    }

    fun updateNotificationPermission(granted: Boolean) {
        _uiState.update { it.copy(notificationGranted = granted) }
        sessionManager.updatePermissions(
            camera = _uiState.value.cameraGranted,
            mic = _uiState.value.micGranted,
            notification = granted
        )
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val draft = sessionManager.getSnapshot()
            val now = System.currentTimeMillis()

            val profile = UserProfile(
                id = 0L,
                goal = draft.goal,
                units = draft.units,
                age = draft.age,
                heightCm = draft.heightCm,
                weightKg = draft.weightKg,
                goalWeightKg = draft.goalWeightKg,
                activityLevel = draft.activityLevel,
                dietTags = draft.dietTags,
                allergies = draft.allergies,
                targetsSource = if (draft.customCalories != null) TargetBasis.CUSTOM else TargetBasis.RECOMMENDED,
                createdAt = now,
                updatedAt = now
            )

            val water = NutritionCalculator.waterFor(draft.weightKg, draft.activityLevel)
            val baseBmr = NutritionCalculator.bmr(draft.weightKg, draft.heightCm, draft.age)
            val tdee = baseBmr * draft.activityLevel.factor

            val goal = NutritionGoal(
                id = 0L,
                userId = 0L,
                calories = draft.customCalories ?: 2000,
                proteinG = draft.customProteinG ?: 140.0,
                carbsG = draft.customCarbsG ?: 220.0,
                fatG = draft.customFatG ?: 60.0,
                waterLiters = water.waterLiters,
                waterGlasses = water.glasses,
                bmr = Math.round(baseBmr).toDouble(),
                tdee = Math.round(tdee).toDouble(),
                isCustom = draft.customCalories != null,
                updatedAt = now
            )

            userProfileRepository.saveProfileAndGoal(profile, goal)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
