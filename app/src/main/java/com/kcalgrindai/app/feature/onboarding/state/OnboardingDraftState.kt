package com.kcalgrindai.app.feature.onboarding.state

import com.kcalgrindai.app.domain.model.ActivityLevel
import com.kcalgrindai.app.domain.model.GoalType
import com.kcalgrindai.app.domain.model.UnitSystem

data class OnboardingDraftState(
    val firstName: String = "",
    val goal: GoalType = GoalType.LOSE,
    val dietTags: List<String> = listOf("none"),
    val allergies: List<String> = emptyList(),
    val units: UnitSystem = UnitSystem.METRIC,
    val age: Int = 28,
    val heightCm: Double = 175.0,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val weightKg: Double = 70.0,
    val goalWeightKg: Double = 65.0,
    val customCalories: Int? = null,
    val customProteinG: Double? = null,
    val customCarbsG: Double? = null,
    val customFatG: Double? = null,
    val cameraPermissionGranted: Boolean = false,
    val micPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = false
)
