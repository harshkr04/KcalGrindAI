package com.lumina.nutrition.domain.model

data class FoodLogItem(
    val id: Long = 0L,
    val mealLogId: Long,
    val foodId: Long? = null,
    val name: String,
    val brand: String? = null,
    val servingDescription: String,
    val servingGrams: Double,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fiberG: Double,
    val source: ItemSource,
    val confidence: Float? = null,
    val confirmed: Boolean
)
