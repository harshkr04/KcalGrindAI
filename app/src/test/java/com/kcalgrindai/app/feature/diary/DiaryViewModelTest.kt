package com.kcalgrindai.app.feature.diary

import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.ItemSource
import com.kcalgrindai.app.domain.model.LogSource
import com.kcalgrindai.app.domain.model.MealLog
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.fakes.FakeMealLogRepository
import com.kcalgrindai.app.fakes.FakeNutritionGoalRepository
import com.kcalgrindai.app.feature.logging.state.LoggingSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mealLogRepository: FakeMealLogRepository
    private lateinit var nutritionGoalRepository: FakeNutritionGoalRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mealLogRepository = FakeMealLogRepository()
        nutritionGoalRepository = FakeNutritionGoalRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `date navigation filters meals accurately`() = runTest {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        // Meal on yesterday
        mealLogRepository.saveMeal(
            MealLog(
                id = 1L,
                date = yesterday.toString(),
                mealType = MealType.BREAKFAST,
                totalCalories = 400.0,
                loggedAt = System.currentTimeMillis(),
                source = LogSource.MANUAL,
                synced = true
            ),
            listOf(
                FoodLogItem(
                    id = 1L,
                    mealLogId = 1L,
                    name = "Oatmeal",
                    servingDescription = "1 bowl",
                    servingGrams = 200.0,
                    calories = 400.0,
                    proteinG = 12.0,
                    carbsG = 60.0,
                    fatG = 6.0,
                    fiberG = 8.0,
                    source = ItemSource.MANUAL,
                    confirmed = true
                )
            )
        )

        // Meal on today
        mealLogRepository.saveMeal(
            MealLog(
                id = 2L,
                date = today.toString(),
                mealType = MealType.DINNER,
                totalCalories = 750.0,
                loggedAt = System.currentTimeMillis(),
                source = LogSource.AI_TEXT,
                synced = true
            ),
            listOf(
                FoodLogItem(
                    id = 2L,
                    mealLogId = 2L,
                    name = "Salmon and Rice",
                    servingDescription = "1 plate",
                    servingGrams = 400.0,
                    calories = 750.0,
                    proteinG = 50.0,
                    carbsG = 65.0,
                    fatG = 25.0,
                    fiberG = 2.0,
                    source = ItemSource.AI,
                    confirmed = true
                )
            )
        )

        val viewModel = DiaryViewModel(mealLogRepository, nutritionGoalRepository)
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Today state
        assertEquals(750, viewModel.uiState.value.totalCalories)
        assertEquals(today, viewModel.uiState.value.selectedDate)
        assertTrue(viewModel.uiState.value.hasMeals)

        // Navigate to yesterday
        viewModel.previousDay()
        advanceUntilIdle()

        assertEquals(400, viewModel.uiState.value.totalCalories)
        assertEquals(yesterday, viewModel.uiState.value.selectedDate)
        assertFalse(viewModel.uiState.value.isToday)

        // Reset to today
        viewModel.resetToToday()
        advanceUntilIdle()

        assertEquals(750, viewModel.uiState.value.totalCalories)
        assertTrue(viewModel.uiState.value.isToday)

        collectJob.cancel()
    }

    @Test
    fun `delete item and undo restoration`() = runTest {
        val today = LocalDate.now()
        val item = FoodLogItem(
            id = 1L,
            mealLogId = 1L,
            name = "Banana",
            servingDescription = "1 fruit",
            servingGrams = 120.0,
            calories = 105.0,
            proteinG = 1.3,
            carbsG = 27.0,
            fatG = 0.3,
            fiberG = 3.0,
            source = ItemSource.MANUAL,
            confirmed = true
        )

        mealLogRepository.saveMeal(
            MealLog(
                id = 1L,
                date = today.toString(),
                mealType = MealType.SNACK,
                totalCalories = 105.0,
                loggedAt = System.currentTimeMillis(),
                source = LogSource.MANUAL,
                synced = true
            ),
            listOf(item)
        )

        val viewModel = DiaryViewModel(mealLogRepository, nutritionGoalRepository)
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(105, viewModel.uiState.value.totalCalories)

        // Delete item
        viewModel.deleteFoodItem(item)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.recentlyDeletedItem)
        assertEquals("Deleted Banana", viewModel.uiState.value.undoMessage)

        // Undo delete
        viewModel.undoDelete()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.recentlyDeletedItem)
        assertEquals(105, viewModel.uiState.value.totalCalories)

        collectJob.cancel()
    }

    @Test
    fun testPrepareAddFoodSetsTargetDateAndMealTypeInSession() = runTest(testDispatcher) {
        val sessionManager = LoggingSessionManager()
        val viewModel = DiaryViewModel(mealLogRepository, nutritionGoalRepository, sessionManager)
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Move to yesterday
        viewModel.previousDay()
        advanceUntilIdle()

        val expectedDate = LocalDate.now().minusDays(1)
        assertEquals(expectedDate, viewModel.uiState.value.selectedDate)

        viewModel.prepareAddFood(MealType.DINNER)
        assertEquals(expectedDate, sessionManager.state.value.targetDate)
        assertEquals(MealType.DINNER, sessionManager.state.value.selectedMealType)

        collectJob.cancel()
    }
}
