package com.kcalgrindai.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.entity.RecipeEntity
import com.kcalgrindai.app.data.local.seeder.RecipeDatabaseSeeder
import com.kcalgrindai.app.domain.model.ItemSource
import com.kcalgrindai.app.domain.model.LogSource
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.model.Recipe
import com.kcalgrindai.app.domain.model.RecipeIngredient
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class RecipeRepositoryImplTest {

    private lateinit var db: KcalGrindDatabase
    private lateinit var recipeRepository: RecipeRepositoryImpl
    private lateinit var mealLogRepository: MealLogRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KcalGrindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val recipeDao = db.recipeDao()
        val mealLogDao = db.mealLogDao()
        val foodLogItemDao = db.foodLogItemDao()
        val seeder = RecipeDatabaseSeeder(recipeDao)

        mealLogRepository = MealLogRepositoryImpl(mealLogDao, foodLogItemDao)
        recipeRepository = RecipeRepositoryImpl(recipeDao, mealLogRepository, seeder)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun seedRecipesIfEmptySeedsAllPredefinedRecipes() = runTest {
        recipeRepository.seedRecipesIfEmpty()
        val count = db.recipeDao().getRecipeCount()
        assertTrue("Recipe count should be at least 70", count >= 70)
        assertEquals(78, count)
    }

    @Test
    fun logRecipeToDiaryWritesMealLogAndFoodItemsWithRecipeSource() = runTest {
        val recipe = Recipe(
            id = "test_recipe_1",
            name = "Protein Oatmeal",
            description = "High protein oats",
            emoji = "🥣",
            mealType = "breakfast",
            prepTimeMinutes = 5,
            totalCalories = 350.0,
            proteinG = 30.0,
            carbsG = 45.0,
            fatG = 5.0,
            fiberG = 6.0,
            dietTags = listOf("High-Protein", "Vegetarian"),
            ingredients = listOf(
                RecipeIngredient("Rolled Oats", 50.0, "g", 190.0, 6.5, 34.0, 3.2, 5.0),
                RecipeIngredient("Whey Protein", 30.0, "g", 120.0, 23.0, 1.0, 1.0, 0.0),
                RecipeIngredient("Almond Milk", 150.0, "ml", 40.0, 0.5, 10.0, 0.8, 1.0)
            ),
            instructions = listOf("Mix all ingredients"),
            popularityScore = 95.0,
            isFavorite = false
        )

        val targetDate = LocalDate.of(2026, 9, 13)
        val mealId = recipeRepository.logRecipeToDiary(recipe, targetDate)
        assertTrue(mealId > 0)

        // Verify MealLog
        val savedMeal = mealLogRepository.getMealById(mealId)
        assertNotNull(savedMeal)
        assertEquals("2026-09-13", savedMeal?.meal?.date)
        assertEquals(MealType.BREAKFAST, savedMeal?.meal?.mealType)
        assertEquals(LogSource.RECIPE, savedMeal?.meal?.source)
        assertEquals(350.0, savedMeal?.meal?.totalCalories ?: 0.0, 0.1)

        // Verify FoodLogItems
        val items = savedMeal?.items ?: emptyList()
        assertEquals(3, items.size)
        assertEquals("Rolled Oats", items[0].name)
        assertEquals(ItemSource.RECIPE, items[0].source)
        assertEquals("Whey Protein", items[1].name)
        assertEquals(ItemSource.RECIPE, items[1].source)
        assertEquals("Almond Milk", items[2].name)
        assertEquals(ItemSource.RECIPE, items[2].source)

        val ingredientCaloriesSum = items.sumOf { it.calories }
        assertEquals(savedMeal?.meal?.totalCalories ?: 0.0, ingredientCaloriesSum, 0.1)
    }

    @Test
    fun toggleFavoriteWorksCorrectly() = runTest {
        val recipeEntity = RecipeEntity(
            id = "fav_test",
            name = "Toast",
            description = "Simple toast",
            emoji = "🍞",
            mealType = "snack",
            prepTimeMinutes = 5,
            totalCalories = 100.0,
            proteinG = 3.0,
            carbsG = 20.0,
            fatG = 1.0,
            fiberG = 1.0,
            dietTags = emptyList(),
            ingredients = emptyList(),
            instructions = emptyList(),
            popularityScore = 80.0,
            isFavorite = false
        )
        db.recipeDao().insertRecipe(recipeEntity)

        recipeRepository.toggleFavorite("fav_test")
        val recipeAfter = recipeRepository.getRecipeById("fav_test")
        assertTrue(recipeAfter?.isFavorite == true)

        recipeRepository.toggleFavorite("fav_test")
        val recipeAfterSecond = recipeRepository.getRecipeById("fav_test")
        assertTrue(recipeAfterSecond?.isFavorite == false)
    }
}
