package com.kcalgrindai.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.ItemSource
import com.kcalgrindai.app.domain.model.LogSource
import com.kcalgrindai.app.domain.model.MealLog
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.repository.SavedMealRepository
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
class SavedMealRepositoryImplTest {

    private lateinit var db: KcalGrindDatabase
    private lateinit var mealLogRepository: MealLogRepositoryImpl
    private lateinit var savedMealRepository: SavedMealRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KcalGrindDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        mealLogRepository = MealLogRepositoryImpl(
            mealLogDao = db.mealLogDao(),
            foodLogItemDao = db.foodLogItemDao(),
            database = db
        )

        savedMealRepository = SavedMealRepositoryImpl(
            savedMealDao = db.savedMealDao(),
            mealLogRepository = mealLogRepository,
            database = db
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun saveMealAsTemplate_storesItemsAndEmitsInFlow() = runTest {
        val items = listOf(
            FoodLogItem(
                mealLogId = 1L,
                foodId = 100L,
                name = "Greek Yogurt",
                brand = "Fage",
                servingDescription = "200g",
                servingGrams = 200.0,
                calories = 190.0,
                proteinG = 20.0,
                carbsG = 6.0,
                fatG = 10.0,
                fiberG = 0.0,
                source = ItemSource.USER_ENTERED,
                confirmed = true,
                sugarG = 6.0,
                sodiumMg = 70.0
            ),
            FoodLogItem(
                mealLogId = 1L,
                foodId = 101L,
                name = "Blueberries",
                brand = null,
                servingDescription = "50g",
                servingGrams = 50.0,
                calories = 42.0,
                proteinG = 0.5,
                carbsG = 10.5,
                fatG = 0.2,
                fiberG = 1.8,
                source = ItemSource.USER_ENTERED,
                confirmed = true,
                sugarG = 7.5,
                sodiumMg = 1.0
            )
        )

        val templateId = savedMealRepository.saveMealAsTemplate(
            name = "Morning Berry Bowl",
            mealType = MealType.BREAKFAST,
            items = items
        )
        assertTrue(templateId > 0)

        savedMealRepository.observeSavedMeals().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            val saved = list[0]
            assertEquals("Morning Berry Bowl", saved.savedMeal.name)
            assertEquals(MealType.BREAKFAST, saved.savedMeal.defaultMealType)
            assertEquals(2, saved.items.size)
            assertEquals(232.0, saved.totalCalories, 0.001)
            assertEquals(20.5, saved.totalProteinG, 0.001)
            assertEquals(16.5, saved.totalCarbsG, 0.001)
            assertEquals(10.2, saved.totalFatG, 0.001)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun renameSavedMeal_updatesTemplateName() = runTest {
        val templateId = savedMealRepository.saveMealAsTemplate(
            name = "Initial Name",
            mealType = MealType.LUNCH,
            items = emptyList()
        )

        savedMealRepository.renameSavedMeal(templateId, "Renamed Power Lunch")

        val retrieved = savedMealRepository.getSavedMealById(templateId)
        assertNotNull(retrieved)
        assertEquals("Renamed Power Lunch", retrieved?.savedMeal?.name)
    }

    @Test
    fun relogSavedMeal_createsFreshFoodLogEntriesWithExactMatchingTotals() = runTest {
        // Insert referenced food entities
        db.foodDao().insertFood(
            com.kcalgrindai.app.data.local.entity.FoodEntity(
                id = 10L,
                source = "USER_ENTERED",
                name = "Grilled Chicken Breast",
                servingDescription = "200g",
                servingGrams = 200.0,
                calories = 330.0,
                proteinG = 62.0,
                carbsG = 0.0,
                fatG = 7.2,
                fiberG = 0.0,
                isUserCreated = true,
                createdAt = 1000L
            )
        )
        db.foodDao().insertFood(
            com.kcalgrindai.app.data.local.entity.FoodEntity(
                id = 11L,
                source = "USER_ENTERED",
                name = "Brown Rice",
                servingDescription = "1 cup cooked",
                servingGrams = 195.0,
                calories = 216.0,
                proteinG = 5.0,
                carbsG = 45.0,
                fatG = 1.8,
                fiberG = 3.5,
                isUserCreated = true,
                createdAt = 1000L
            )
        )

        // 1. Initial original log
        val originalItems = listOf(
            FoodLogItem(
                mealLogId = 0L,
                foodId = 10L,
                name = "Grilled Chicken Breast",
                brand = null,
                servingDescription = "200g",
                servingGrams = 200.0,
                calories = 330.0,
                proteinG = 62.0,
                carbsG = 0.0,
                fatG = 7.2,
                fiberG = 0.0,
                source = ItemSource.USER_ENTERED,
                confirmed = true,
                sugarG = 0.0,
                sodiumMg = 150.0
            ),
            FoodLogItem(
                mealLogId = 0L,
                foodId = 11L,
                name = "Brown Rice",
                brand = null,
                servingDescription = "1 cup cooked",
                servingGrams = 195.0,
                calories = 216.0,
                proteinG = 5.0,
                carbsG = 45.0,
                fatG = 1.8,
                fiberG = 3.5,
                source = ItemSource.USER_ENTERED,
                confirmed = true,
                sugarG = 0.7,
                sodiumMg = 10.0
            )
        )
        val originalTotalCalories = originalItems.sumOf { it.calories }
        val originalTotalProtein = originalItems.sumOf { it.proteinG }
        val originalTotalCarbs = originalItems.sumOf { it.carbsG }
        val originalTotalFat = originalItems.sumOf { it.fatG }

        // 2. Save as saved meal template
        val templateId = savedMealRepository.saveMealAsTemplate(
            name = "Clean Bulking Lunch",
            mealType = MealType.LUNCH,
            items = originalItems
        )

        // 3. Re-log to a different date and meal slot (e.g. tomorrow dinner)
        val targetDate = "2026-11-05"
        val targetSlot = MealType.DINNER
        val newMealId = savedMealRepository.logSavedMeal(
            savedMealId = templateId,
            targetDate = targetDate,
            targetMealType = targetSlot
        )
        assertTrue(newMealId > 0)

        // 4. Verify the newly logged meal
        val loggedMealWithItems = mealLogRepository.getMealById(newMealId)
        assertNotNull(loggedMealWithItems)
        assertEquals(targetDate, loggedMealWithItems?.meal?.date)
        assertEquals(targetSlot, loggedMealWithItems?.meal?.mealType)
        assertEquals(2, loggedMealWithItems?.items?.size)

        // Totals must EXACTLY match original
        assertEquals(originalTotalCalories, loggedMealWithItems?.calculatedCalories ?: 0.0, 0.0001)
        assertEquals(originalTotalProtein, loggedMealWithItems?.totalProteinG ?: 0.0, 0.0001)
        assertEquals(originalTotalCarbs, loggedMealWithItems?.totalCarbsG ?: 0.0, 0.0001)
        assertEquals(originalTotalFat, loggedMealWithItems?.totalFatG ?: 0.0, 0.0001)
    }

    @Test
    fun deleteSavedMeal_doesNotAffectPastLoggedMeals() = runTest {
        db.foodDao().insertFood(
            com.kcalgrindai.app.data.local.entity.FoodEntity(
                id = 1L,
                source = "USER_ENTERED",
                name = "Salmon Fillet",
                servingDescription = "200g",
                servingGrams = 200.0,
                calories = 500.0,
                proteinG = 40.0,
                carbsG = 0.0,
                fatG = 30.0,
                fiberG = 0.0,
                isUserCreated = true,
                createdAt = 1000L
            )
        )

        // 1. Log a meal directly to diary
        val pastMeal = MealLog(
            id = 0L,
            date = "2026-11-01",
            mealType = MealType.DINNER,
            totalCalories = 500.0,
            loggedAt = 1000L,
            source = LogSource.MANUAL,
            synced = false
        )
        val pastItems = listOf(
            FoodLogItem(
                mealLogId = 0L,
                foodId = 1L,
                name = "Salmon Fillet",
                servingDescription = "200g",
                servingGrams = 200.0,
                calories = 500.0,
                proteinG = 40.0,
                carbsG = 0.0,
                fatG = 30.0,
                fiberG = 0.0,
                source = ItemSource.USER_ENTERED,
                confirmed = true
            )
        )
        val loggedPastMealId = mealLogRepository.saveMeal(pastMeal, pastItems)

        // 2. Save it as template
        val templateId = savedMealRepository.saveMealAsTemplate(
            name = "Salmon Dinner",
            mealType = MealType.DINNER,
            items = pastItems
        )
        assertNotNull(savedMealRepository.getSavedMealById(templateId))

        // 3. Delete the saved meal template
        savedMealRepository.deleteSavedMeal(templateId)
        assertNull(savedMealRepository.getSavedMealById(templateId))

        // 4. Past log MUST still exist completely intact
        val preservedPastMeal = mealLogRepository.getMealById(loggedPastMealId)
        assertNotNull("Past logged meal is completely preserved after template deletion", preservedPastMeal)
        assertEquals(500.0, preservedPastMeal?.calculatedCalories ?: 0.0, 0.001)
        assertEquals(1, preservedPastMeal?.items?.size)
        assertEquals("Salmon Fillet", preservedPastMeal?.items?.get(0)?.name)
    }
}
