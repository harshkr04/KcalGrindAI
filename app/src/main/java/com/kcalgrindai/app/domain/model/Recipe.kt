package com.kcalgrindai.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RecipeIngredient(
    val name: String,
    val amount: Double,
    val unit: String,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fiberG: Double = 0.0
)

data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val mealType: String,
    val prepTimeMinutes: Int,
    val totalCalories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fiberG: Double = 0.0,
    val dietTags: List<String> = emptyList(),
    val ingredients: List<RecipeIngredient> = emptyList(),
    val instructions: List<String> = emptyList(),
    val popularityScore: Double = 0.0,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
