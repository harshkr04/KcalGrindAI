package com.lumina.nutrition.feature.onboarding.state

import com.lumina.nutrition.domain.model.ActivityLevel
import com.lumina.nutrition.domain.model.GoalType
import com.lumina.nutrition.domain.model.UnitSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingSessionManager @Inject constructor() {

    private val _draftState = MutableStateFlow(OnboardingDraftState())
    val draftState: StateFlow<OnboardingDraftState> = _draftState.asStateFlow()

    fun getSnapshot(): OnboardingDraftState = _draftState.value

    fun updateGoal(goal: GoalType) {
        _draftState.update { it.copy(goal = goal) }
    }

    fun updateDietTags(tags: List<String>) {
        _draftState.update { it.copy(dietTags = tags) }
    }

    fun updateAllergies(allergies: List<String>) {
        _draftState.update { it.copy(allergies = allergies) }
    }

    fun updatePersonalDetails(age: Int, heightCm: Double, units: UnitSystem) {
        _draftState.update { it.copy(age = age, heightCm = heightCm, units = units) }
    }

    fun updateActivityLevel(activity: ActivityLevel) {
        _draftState.update { it.copy(activityLevel = activity) }
    }

    fun updateBodyMetrics(weightKg: Double, goalWeightKg: Double) {
        _draftState.update { it.copy(weightKg = weightKg, goalWeightKg = goalWeightKg) }
    }

    fun updateCustomMacros(calories: Int, proteinG: Double, carbsG: Double, fatG: Double) {
        _draftState.update {
            it.copy(
                customCalories = calories,
                customProteinG = proteinG,
                customCarbsG = carbsG,
                customFatG = fatG
            )
        }
    }

    fun updatePermissions(camera: Boolean, mic: Boolean, notification: Boolean) {
        _draftState.update {
            it.copy(
                cameraPermissionGranted = camera,
                micPermissionGranted = mic,
                notificationPermissionGranted = notification
            )
        }
    }

    fun resetDraft() {
        _draftState.value = OnboardingDraftState()
    }
}
