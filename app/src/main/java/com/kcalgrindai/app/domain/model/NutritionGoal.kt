package com.kcalgrindai.app.domain.model

data class NutritionGoal(
    val id: Long = 0L,
    val userId: Long,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val waterLiters: Double,
    val waterGlasses: Int,
    val bmr: Double?,
    val tdee: Double?,
    val isCustom: Boolean,
    val updatedAt: Long
)
