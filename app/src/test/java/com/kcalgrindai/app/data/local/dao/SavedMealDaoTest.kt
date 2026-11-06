package com.kcalgrindai.app.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.entity.SavedMealEntity
import com.kcalgrindai.app.data.local.entity.SavedMealItemEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SavedMealDaoTest {

    private lateinit var db: KcalGrindDatabase
    private lateinit var savedMealDao: SavedMealDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KcalGrindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        savedMealDao = db.savedMealDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertSavedMeal_andGetById() = runTest {
        val meal = SavedMealEntity(
            id = 1L,
            name = "Post-Workout Smoothie",
            defaultMealType = "SNACK"
        )
        val id = savedMealDao.insertSavedMeal(meal)
        assertEquals(1L, id)

        val retrieved = savedMealDao.getSavedMealById(1L)
        assertNotNull(retrieved)
        assertEquals("Post-Workout Smoothie", retrieved?.name)
        assertEquals("SNACK", retrieved?.defaultMealType)
    }

    @Test
    fun insertItems_andObserveSavedMealsWithItems() = runTest {
        val meal = SavedMealEntity(
            id = 10L,
            name = "High Protein Breakfast",
            defaultMealType = "BREAKFAST"
        )
        savedMealDao.insertSavedMeal(meal)

        val item1 = SavedMealItemEntity(
            id = 101L,
            savedMealId = 10L,
            foodId = 1L,
            name = "Whole Eggs",
            brand = null,
            servingDescription = "2 large",
            servingGrams = 100.0,
            calories = 140.0,
            proteinG = 12.0,
            carbsG = 1.0,
            fatG = 10.0,
            fiberG = 0.0,
            sugarG = 0.5,
            sodiumMg = 140.0
        )
        val item2 = SavedMealItemEntity(
            id = 102L,
            savedMealId = 10L,
            foodId = 2L,
            name = "Sourdough Toast",
            brand = "Bakery",
            servingDescription = "1 slice",
            servingGrams = 50.0,
            calories = 120.0,
            proteinG = 4.0,
            carbsG = 24.0,
            fatG = 1.0,
            fiberG = 2.0,
            sugarG = 1.0,
            sodiumMg = 200.0
        )
        savedMealDao.insertSavedMealItems(listOf(item1, item2))

        val retrieved = savedMealDao.getSavedMealWithItemsById(10L)
        assertNotNull(retrieved)
        assertEquals("High Protein Breakfast", retrieved?.savedMeal?.name)
        assertEquals(2, retrieved?.items?.size)

        savedMealDao.observeSavedMealsWithItems().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("High Protein Breakfast", list[0].savedMeal.name)
            assertEquals(2, list[0].items.size)
            assertEquals("Whole Eggs", list[0].items[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun renameSavedMeal_updatesNameAndTimestamp() = runTest {
        val meal = SavedMealEntity(
            id = 5L,
            name = "Old Lunch Name",
            defaultMealType = "LUNCH",
            updatedAt = 1000L
        )
        savedMealDao.insertSavedMeal(meal)

        savedMealDao.renameSavedMeal(5L, "Power Protein Lunch", 2000L)

        val updated = savedMealDao.getSavedMealById(5L)
        assertNotNull(updated)
        assertEquals("Power Protein Lunch", updated?.name)
        assertEquals(2000L, updated?.updatedAt)
    }

    @Test
    fun deleteSavedMeal_cascadesAndDeletesItems() = runTest {
        val meal = SavedMealEntity(
            id = 20L,
            name = "To Delete",
            defaultMealType = "DINNER"
        )
        savedMealDao.insertSavedMeal(meal)

        val item = SavedMealItemEntity(
            id = 201L,
            savedMealId = 20L,
            name = "Chicken Breast",
            servingDescription = "150g",
            servingGrams = 150.0,
            calories = 240.0,
            proteinG = 46.0,
            carbsG = 0.0,
            fatG = 5.0
        )
        savedMealDao.insertSavedMealItems(listOf(item))

        assertEquals(1, savedMealDao.getItemsForSavedMeal(20L).size)

        savedMealDao.deleteSavedMealById(20L)

        assertNull(savedMealDao.getSavedMealById(20L))
        assertTrue(savedMealDao.getItemsForSavedMeal(20L).isEmpty())
    }
}
