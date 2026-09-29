package com.kcalgrindai.app.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.entity.FoodEntity
import com.kcalgrindai.app.data.local.entity.FoodLogItemEntity
import com.kcalgrindai.app.data.local.entity.MealLogEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MealLogAndFoodLogItemDaoTest {

    private lateinit var db: KcalGrindDatabase
    private lateinit var mealLogDao: MealLogDao
    private lateinit var foodLogItemDao: FoodLogItemDao
    private lateinit var foodDao: FoodDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KcalGrindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        mealLogDao = db.mealLogDao()
        foodLogItemDao = db.foodLogItemDao()
        foodDao = db.foodDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertMealWithItemsAndObserveByDate() = runTest {
        // Insert Food reference
        val food = FoodEntity(
            id = 10L,
            source = "usda",
            name = "Eggs",
            servingDescription = "2 large",
            servingGrams = 100.0,
            calories = 140.0,
            proteinG = 12.0,
            carbsG = 1.0,
            fatG = 10.0,
            fiberG = 0.0,
            isUserCreated = false,
            createdAt = 1000L
        )
        foodDao.insertFood(food)

        // Insert Meal
        val meal = MealLogEntity(
            id = 1L,
            date = "2026-09-01",
            mealType = "breakfast",
            totalCalories = 350.0,
            loggedAt = 1000L,
            source = "ai_photo",
            synced = false
        )
        mealLogDao.insertMealLog(meal)

        // Insert FoodLogItems (one verified food reference, one ad-hoc AI item)
        val item1 = FoodLogItemEntity(
            id = 101L,
            mealLogId = 1L,
            foodId = 10L,
            name = "Scrambled Eggs",
            brand = null,
            servingDescription = "2 large eggs",
            servingGrams = 100.0,
            calories = 140.0,
            proteinG = 12.0,
            carbsG = 1.0,
            fatG = 10.0,
            fiberG = 0.0,
            source = "verified",
            confidence = 0.95f,
            confirmed = true
        )

        val item2 = FoodLogItemEntity(
            id = 102L,
            mealLogId = 1L,
            foodId = null, // ad-hoc AI detected item
            name = "Avocado Toast",
            brand = null,
            servingDescription = "1 slice",
            servingGrams = 120.0,
            calories = 210.0,
            proteinG = 5.0,
            carbsG = 22.0,
            fatG = 11.0,
            fiberG = 6.0,
            source = "ai",
            confidence = 0.82f,
            confirmed = true
        )
        foodLogItemDao.insertItems(listOf(item1, item2))

        // Observe meals with items for today
        mealLogDao.observeMealsWithItemsByDate("2026-09-01").test {
            val list = awaitItem()
            assertEquals(1, list.size)
            val mealWithItems = list[0]
            assertEquals("breakfast", mealWithItems.meal.mealType)
            assertEquals("ai_photo", mealWithItems.meal.source)
            assertEquals(2, mealWithItems.items.size)
            assertEquals("Scrambled Eggs", mealWithItems.items[0].name)
            assertEquals("verified", mealWithItems.items[0].source)
            assertEquals("Avocado Toast", mealWithItems.items[1].name)
            assertEquals("ai", mealWithItems.items[1].source)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun cascadeDeleteMealRemovesItems() = runTest {
        val meal = MealLogEntity(
            id = 2L,
            date = "2026-09-01",
            mealType = "lunch",
            totalCalories = 500.0,
            loggedAt = 2000L,
            source = "manual",
            synced = true
        )
        mealLogDao.insertMealLog(meal)

        val item = FoodLogItemEntity(
            id = 201L,
            mealLogId = 2L,
            foodId = null,
            name = "Turkey Sandwich",
            servingDescription = "1 whole",
            servingGrams = 200.0,
            calories = 500.0,
            proteinG = 30.0,
            carbsG = 45.0,
            fatG = 15.0,
            fiberG = 4.0,
            source = "manual",
            confidence = null,
            confirmed = true
        )
        foodLogItemDao.insertItem(item)

        // Delete meal
        mealLogDao.deleteMealLogById(2L)

        val loadedMeal = mealLogDao.getMealLogById(2L)
        assertNull(loadedMeal)

        val loadedItems = foodLogItemDao.getItemsForMeal(2L)
        assertEquals(0, loadedItems.size)
    }

    @Test
    fun observeMealsWithItemsInRange() = runTest {
        val meal1 = MealLogEntity(
            id = 1L,
            date = "2026-09-01",
            mealType = "dinner",
            totalCalories = 600.0,
            loggedAt = 1000L,
            source = "manual",
            synced = true
        )
        val meal2 = MealLogEntity(
            id = 2L,
            date = "2026-09-02",
            mealType = "dinner",
            totalCalories = 700.0,
            loggedAt = 2000L,
            source = "manual",
            synced = true
        )
        mealLogDao.insertMealLog(meal1)
        mealLogDao.insertMealLog(meal2)

        mealLogDao.observeMealsWithItemsInRange("2026-09-01", "2026-09-02").test {
            val list = awaitItem()
            assertEquals(2, list.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
