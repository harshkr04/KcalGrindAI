package com.kcalgrindai.app.domain.model

enum class WidgetStatusType {
    NORMAL,
    EMPTY,
    GOAL_REACHED,
    OVER_GOAL,
    ONBOARDING_INCOMPLETE,
    LOADING,
    PRIVACY
}

enum class SproutlingMood {
    RESTING,
    ACTIVE,
    ENERGIZED,
    CONTENT,
    WAVING,
    LOADING,
    SECURE
}

data class WidgetUiState(
    val targetCalories: Int = 2000,
    val consumedCalories: Int = 0,
    val remainingCalories: Int = 2000,
    val progressPercent: Int = 0,
    val energyBlocksFilled: Int = 0,
    val proteinGrams: Double = 0.0,
    val targetProteinGrams: Double = 150.0,
    val proteinFraction: Float = 0.0f,
    val carbsGrams: Double = 0.0,
    val targetCarbsGrams: Double = 200.0,
    val carbsFraction: Float = 0.0f,
    val fatGrams: Double = 0.0,
    val targetFatGrams: Double = 65.0,
    val fatFraction: Float = 0.0f,
    val waterMl: Int = 0,
    val waterGlasses: Int = 0,
    val streakDays: Int = 0,
    val statusType: WidgetStatusType = WidgetStatusType.EMPTY,
    val primaryHeadline: String = "2,000 kcal left",
    val subHeadline: String = "Ready to log today",
    val sproutlingMood: SproutlingMood = SproutlingMood.RESTING,
    val isPrivacyEnabled: Boolean = false,
    val lastUpdatedEpochMs: Long = 0L
) {
    companion object {
        val Loading = WidgetUiState(
            statusType = WidgetStatusType.LOADING,
            primaryHeadline = "Updating...",
            subHeadline = "Refreshing data",
            sproutlingMood = SproutlingMood.LOADING
        )

        val LoggedOut = WidgetUiState(
            statusType = WidgetStatusType.ONBOARDING_INCOMPLETE,
            primaryHeadline = "Sign in to begin",
            subHeadline = "Connect your account",
            sproutlingMood = SproutlingMood.WAVING
        )
    }
}
