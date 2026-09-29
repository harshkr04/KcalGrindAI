package com.kcalgrindai.app.domain.repository

import com.kcalgrindai.app.domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface RecipeRepository {
    fun observeAllRecipes(): Flow<List<Recipe>>
    fun observeFavoriteRecipes(): Flow<List<Recipe>>
    fun observeRecipeById(recipeId: String): Flow<Recipe?>
    suspend fun getRecipeById(recipeId: String): Recipe?
    suspend fun toggleFavorite(recipeId: String)
    suspend fun logRecipeToDiary(recipe: Recipe, targetDate: LocalDate = LocalDate.now()): Long
    suspend fun seedRecipesIfEmpty()
}
