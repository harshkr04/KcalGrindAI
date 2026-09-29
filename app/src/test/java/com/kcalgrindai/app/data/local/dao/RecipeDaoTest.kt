package com.kcalgrindai.app.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.entity.RecipeEntity
import com.kcalgrindai.app.domain.model.RecipeIngredient
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecipeDaoTest {

    private lateinit var db: KcalGrindDatabase
    private lateinit var recipeDao: RecipeDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KcalGrindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        recipeDao = db.recipeDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndQueryAllRecipes() = runTest {
        val ingredients = listOf(
            RecipeIngredient("Salmon", 150.0, "g", 280.0, 34.0, 0.0, 15.5, 0.0),
            RecipeIngredient("Asparagus", 100.0, "g", 20.0, 2.2, 3.8, 0.2, 2.0)
        )
        val recipe1 = RecipeEntity(
            id = "r1",
            name = "Pan Seared Salmon",
            description = "Crispy salmon",
            emoji = "🐟",
            mealType = "dinner",
            prepTimeMinutes = 18,
            totalCalories = 300.0,
            proteinG = 36.2,
            carbsG = 3.8,
            fatG = 15.7,
            fiberG = 2.0,
            dietTags = listOf("High-Protein", "Low-Carb"),
            ingredients = ingredients,
            instructions = listOf("Sear salmon", "Roast asparagus"),
            popularityScore = 95.0,
            isFavorite = false
        )
        val recipe2 = recipe1.copy(id = "r2", name = "Berry Yogurt", popularityScore = 98.0)

        recipeDao.insertRecipes(listOf(recipe1, recipe2))

        assertEquals(2, recipeDao.getRecipeCount())

        val byId = recipeDao.getRecipeById("r1")
        assertNotNull(byId)
        assertEquals("Pan Seared Salmon", byId?.name)
        assertEquals(2, byId?.ingredients?.size)
        assertEquals("Salmon", byId?.ingredients?.first()?.name)

        recipeDao.observeAllRecipes().test {
            val list = awaitItem()
            assertEquals(2, list.size)
            assertEquals("r2", list[0].id) // 98.0 popularity before 95.0
            assertEquals("r1", list[1].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun toggleFavoriteAndObserveFavorites() = runTest {
        val recipe = RecipeEntity(
            id = "r1",
            name = "Avocado Toast",
            description = "Crisp sourdough toast",
            emoji = "🥑",
            mealType = "breakfast",
            prepTimeMinutes = 10,
            totalCalories = 300.0,
            proteinG = 12.0,
            carbsG = 25.0,
            fatG = 16.0,
            fiberG = 8.0,
            dietTags = listOf("Vegetarian"),
            ingredients = emptyList(),
            instructions = emptyList(),
            popularityScore = 90.0,
            isFavorite = false
        )

        recipeDao.insertRecipe(recipe)
        assertFalse(recipeDao.getRecipeById("r1")!!.isFavorite)

        recipeDao.setFavorite("r1", true)
        assertTrue(recipeDao.getRecipeById("r1")!!.isFavorite)

        recipeDao.observeFavoriteRecipes().test {
            val favs = awaitItem()
            assertEquals(1, favs.size)
            assertEquals("r1", favs[0].id)
            cancelAndIgnoreRemainingEvents()
        }

        recipeDao.setFavorite("r1", false)
        assertFalse(recipeDao.getRecipeById("r1")!!.isFavorite)
    }

    @Test
    fun insertWithIgnorePreservesFavoriteState() = runTest {
        val recipe = RecipeEntity(
            id = "r1",
            name = "Avocado Toast",
            description = "Crisp sourdough toast",
            emoji = "🥑",
            mealType = "breakfast",
            prepTimeMinutes = 10,
            totalCalories = 300.0,
            proteinG = 12.0,
            carbsG = 25.0,
            fatG = 16.0,
            fiberG = 8.0,
            dietTags = listOf("Vegetarian"),
            ingredients = emptyList(),
            instructions = emptyList(),
            popularityScore = 90.0,
            isFavorite = false
        )
        recipeDao.insertRecipe(recipe)
        recipeDao.setFavorite("r1", true)
        assertTrue(recipeDao.getRecipeById("r1")!!.isFavorite)

        // Re-insert with isFavorite = false via insertRecipes (OnConflictStrategy.IGNORE)
        recipeDao.insertRecipes(listOf(recipe))
        assertTrue(recipeDao.getRecipeById("r1")!!.isFavorite) // Should still be true!
    }
}
