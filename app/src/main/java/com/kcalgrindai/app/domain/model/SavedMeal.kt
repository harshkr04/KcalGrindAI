package com.kcalgrindai.app.domain.model

data class SavedMeal(
    val id: Long = 0L,
    val name: String,
    val defaultMealType: MealType = MealType.LUNCH,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class SavedMealItem(
    val id: Long = 0L,
    val savedMealId: Long = 0L,
    val foodId: Long? = null,
    val name: String,
    val brand: String? = null,
    val servingDescription: String,
    val servingGrams: Double,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fiberG: Double = 0.0,
    val sugarG: Double = 0.0,
    val sodiumMg: Double = 0.0,
    val source: ItemSource = ItemSource.USER_ENTERED
)

data class SavedMealWithItems(
    val savedMeal: SavedMeal,
    val items: List<SavedMealItem>
) {
    val totalCalories: Double get() = items.sumOf { it.calories }
    val totalProteinG: Double get() = items.sumOf { it.proteinG }
    val totalCarbsG: Double get() = items.sumOf { it.carbsG }
    val totalFatG: Double get() = items.sumOf { it.fatG }
    val totalFiberG: Double get() = items.sumOf { it.fiberG }
    val totalSugarG: Double get() = items.sumOf { it.sugarG }
    val totalSodiumMg: Double get() = items.sumOf { it.sodiumMg }
}
