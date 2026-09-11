package com.lumina.nutrition.feature.home

import com.lumina.nutrition.domain.model.MealType
import com.lumina.nutrition.domain.model.MealWithFoodItems

data class MacroProgress(
    val currentGrams: Double = 0.0,
    val targetGrams: Double = 0.0,
    val progressFraction: Float = 0.0f
)

data class MealCategorySummary(
    val mealType: MealType,
    val totalCalories: Double = 0.0,
    val meals: List<MealWithFoodItems> = emptyList(),
    val itemCount: Int = 0
)

data class WaterProgress(
    val consumedMl: Int = 0,
    val targetMl: Int = 2400,
    val targetGlasses: Int = 8,
    val progressFraction: Float = 0.0f,
    val latestLogId: Long? = null,
    val logsCountToday: Int = 0
)

data class ActivityProgress(
    val stepsToday: Long = 0L,
    val isAvailable: Boolean = false,
    val statusMessage: String = "Health Connect not available"
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "Lumina User",
    val currentDateFormatted: String = "",
    val targetCalories: Int = 2000,
    val consumedCalories: Int = 0,
    val remainingCalories: Int = 2000,
    val calorieProgressFraction: Float = 0.0f,
    val protein: MacroProgress = MacroProgress(),
    val carbs: MacroProgress = MacroProgress(),
    val fat: MacroProgress = MacroProgress(),
    val water: WaterProgress = WaterProgress(),
    val activity: ActivityProgress = ActivityProgress(),
    val mealsGrouped: List<MealCategorySummary> = emptyList(),
    val hasLoggedMealsToday: Boolean = false
)
