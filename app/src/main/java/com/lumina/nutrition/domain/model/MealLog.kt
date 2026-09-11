package com.lumina.nutrition.domain.model

data class MealLog(
    val id: Long = 0L,
    val date: String,
    val mealType: MealType,
    val totalCalories: Double,
    val loggedAt: Long,
    val source: LogSource,
    val synced: Boolean
)
