package com.kcalgrindai.app.data.repository

import com.kcalgrindai.app.data.local.dao.RecipeDao
import com.kcalgrindai.app.data.local.seeder.RecipeDatabaseSeeder
import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.ItemSource
import com.kcalgrindai.app.domain.model.LogSource
import com.kcalgrindai.app.domain.model.MealLog
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.model.Recipe
import com.kcalgrindai.app.domain.repository.MealLogRepository
import com.kcalgrindai.app.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val recipeDao: RecipeDao,
    private val mealLogRepository: MealLogRepository,
    private val seeder: RecipeDatabaseSeeder
) : RecipeRepository {

    override fun observeAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.observeAllRecipes().map { list -> list.map { it.toDomain() } }
    }

    override fun observeFavoriteRecipes(): Flow<List<Recipe>> {
        return recipeDao.observeFavoriteRecipes().map { list -> list.map { it.toDomain() } }
    }

    override fun observeRecipeById(recipeId: String): Flow<Recipe?> {
        return recipeDao.observeRecipeById(recipeId).map { it?.toDomain() }
    }

    override suspend fun getRecipeById(recipeId: String): Recipe? {
        return recipeDao.getRecipeById(recipeId)?.toDomain()
    }

    override suspend fun toggleFavorite(recipeId: String) {
        val current = recipeDao.getRecipeById(recipeId) ?: return
        recipeDao.setFavorite(recipeId, !current.isFavorite)
    }

    override suspend fun logRecipeToDiary(recipe: Recipe, targetDate: LocalDate): Long {
        val mealType = when (recipe.mealType.lowercase()) {
            "breakfast" -> MealType.BREAKFAST
            "lunch" -> MealType.LUNCH
            "dinner" -> MealType.DINNER
            else -> MealType.SNACK
        }
        val dateStr = targetDate.toString()
        val now = System.currentTimeMillis()

        val mealLog = MealLog(
            id = 0L,
            date = dateStr,
            mealType = mealType,
            totalCalories = recipe.totalCalories,
            loggedAt = now,
            source = LogSource.RECIPE,
            synced = false
        )

        val items = recipe.ingredients.map { ing ->
            FoodLogItem(
                id = 0L,
                mealLogId = 0L,
                foodId = null,
                name = ing.name,
                brand = "Curated Recipe",
                servingDescription = "${ing.amount} ${ing.unit}",
                servingGrams = ing.amount,
                calories = ing.calories,
                proteinG = ing.proteinG,
                carbsG = ing.carbsG,
                fatG = ing.fatG,
                fiberG = ing.fiberG,
                source = ItemSource.RECIPE,
                confidence = 1.0f,
                confirmed = true
            )
        }

        return mealLogRepository.saveMeal(mealLog, items)
    }

    override suspend fun seedRecipesIfEmpty() {
        seeder.seedRecipesIfEmpty()
    }
}
