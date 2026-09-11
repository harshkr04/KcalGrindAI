package com.lumina.nutrition.feature.diary.detail

import com.lumina.nutrition.domain.model.MealWithFoodItems

data class MealDetailUiState(
    val isLoading: Boolean = true,
    val mealWithItems: MealWithFoodItems? = null,
    val totalCalories: Double = 0.0,
    val totalProteinG: Double = 0.0,
    val totalCarbsG: Double = 0.0,
    val totalFatG: Double = 0.0,
    val formattedDate: String = "",
    val sourceLabel: String = "Manual",
    val isDuplicated: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)
