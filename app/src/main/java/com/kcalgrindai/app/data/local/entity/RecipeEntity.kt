package com.kcalgrindai.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kcalgrindai.app.domain.model.Recipe
import com.kcalgrindai.app.domain.model.RecipeIngredient

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey
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
    val dietTags: List<String>,
    val ingredients: List<RecipeIngredient>,
    val instructions: List<String>,
    val popularityScore: Double = 0.0,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Recipe = Recipe(
        id = id,
        name = name,
        description = description,
        emoji = emoji,
        mealType = mealType,
        prepTimeMinutes = prepTimeMinutes,
        totalCalories = totalCalories,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        fiberG = fiberG,
        dietTags = dietTags,
        ingredients = ingredients,
        instructions = instructions,
        popularityScore = popularityScore,
        isFavorite = isFavorite,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(recipe: Recipe): RecipeEntity = RecipeEntity(
            id = recipe.id,
            name = recipe.name,
            description = recipe.description,
            emoji = recipe.emoji,
            mealType = recipe.mealType,
            prepTimeMinutes = recipe.prepTimeMinutes,
            totalCalories = recipe.totalCalories,
            proteinG = recipe.proteinG,
            carbsG = recipe.carbsG,
            fatG = recipe.fatG,
            fiberG = recipe.fiberG,
            dietTags = recipe.dietTags,
            ingredients = recipe.ingredients,
            instructions = recipe.instructions,
            popularityScore = recipe.popularityScore,
            isFavorite = recipe.isFavorite,
            createdAt = recipe.createdAt
        )
    }
}
