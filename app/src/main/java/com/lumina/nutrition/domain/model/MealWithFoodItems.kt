package com.lumina.nutrition.domain.model

data class MealWithFoodItems(
    val meal: MealLog,
    val items: List<FoodLogItem>
) {
    val totalProteinG: Double get() = items.sumOf { it.proteinG }
    val totalCarbsG: Double get() = items.sumOf { it.carbsG }
    val totalFatG: Double get() = items.sumOf { it.fatG }
    val totalFiberG: Double get() = items.sumOf { it.fiberG }
    val calculatedCalories: Double get() = items.sumOf { it.calories }
}
