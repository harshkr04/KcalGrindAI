package com.lumina.nutrition.feature.diary

import com.lumina.nutrition.domain.model.FoodLogItem
import com.lumina.nutrition.domain.model.MealWithFoodItems
import com.lumina.nutrition.feature.home.MealCategorySummary
import java.time.LocalDate

data class DiaryUiState(
    val isLoading: Boolean = true,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDateFormatted: String = "",
    val isToday: Boolean = true,
    val targetCalories: Int = 2000,
    val totalCalories: Int = 0,
    val totalProteinG: Double = 0.0,
    val totalCarbsG: Double = 0.0,
    val totalFatG: Double = 0.0,
    val mealsGrouped: List<MealCategorySummary> = emptyList(),
    val hasMeals: Boolean = false,
    val recentlyDeletedItem: FoodLogItem? = null,
    val undoMessage: String? = null
)
