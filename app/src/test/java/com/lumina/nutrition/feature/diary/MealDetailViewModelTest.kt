package com.lumina.nutrition.feature.diary

import com.lumina.nutrition.domain.model.FoodLogItem
import com.lumina.nutrition.domain.model.ItemSource
import com.lumina.nutrition.domain.model.LogSource
import com.lumina.nutrition.domain.model.MealLog
import com.lumina.nutrition.domain.model.MealType
import com.lumina.nutrition.fakes.FakeMealLogRepository
import com.lumina.nutrition.feature.diary.detail.MealDetailViewModel
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MealDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mealLogRepository: FakeMealLogRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mealLogRepository = FakeMealLogRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMeal loads meal items and computes total nutrients`() = runTest {
        val meal = MealLog(
            id = 10L,
            date = "2026-09-01",
            mealType = MealType.DINNER,
            totalCalories = 820.0,
            loggedAt = 1756700000000L,
            source = LogSource.AI_PHOTO,
            synced = true
        )
        val items = listOf(
            FoodLogItem(
                id = 1L,
                mealLogId = 10L,
                name = "Steak",
                servingDescription = "1 portion",
                servingGrams = 250.0,
                calories = 600.0,
                proteinG = 62.0,
                carbsG = 0.0,
                fatG = 38.0,
                fiberG = 0.0,
                source = ItemSource.AI,
                confidence = 0.95f,
                confirmed = true
            ),
            FoodLogItem(
                id = 2L,
                mealLogId = 10L,
                name = "Baked Potato",
                servingDescription = "1 medium potato",
                servingGrams = 200.0,
                calories = 220.0,
                proteinG = 5.0,
                carbsG = 50.0,
                fatG = 0.2,
                fiberG = 4.0,
                source = ItemSource.MANUAL,
                confirmed = true
            )
        )
        mealLogRepository.saveMeal(meal, items)

        val viewModel = MealDetailViewModel(mealLogRepository)
        val collectJob = launch { viewModel.uiState.collect {} }

        viewModel.loadMeal(10L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.mealWithItems)
        assertEquals(820.0, state.totalCalories, 0.1)
        assertEquals(67.0, state.totalProteinG, 0.1) // 62 + 5
        assertEquals(50.0, state.totalCarbsG, 0.1) // 0 + 50
        assertEquals(38.2, state.totalFatG, 0.1) // 38 + 0.2
        assertEquals("AI Photo Log", state.sourceLabel)

        collectJob.cancel()
    }

    @Test
    fun `duplicateMealToToday creates copy in today's diary`() = runTest {
        val pastDate = "2026-08-15"
        val meal = MealLog(
            id = 5L,
            date = pastDate,
            mealType = MealType.BREAKFAST,
            totalCalories = 350.0,
            loggedAt = 1755200000000L,
            source = LogSource.MANUAL,
            synced = true
        )
        val items = listOf(
            FoodLogItem(
                id = 1L,
                mealLogId = 5L,
                name = "Avocado Toast",
                servingDescription = "1 slice",
                servingGrams = 150.0,
                calories = 350.0,
                proteinG = 8.0,
                carbsG = 30.0,
                fatG = 22.0,
                fiberG = 6.0,
                source = ItemSource.MANUAL,
                confirmed = true
            )
        )
        mealLogRepository.saveMeal(meal, items)

        val viewModel = MealDetailViewModel(mealLogRepository)
        val collectJob = launch { viewModel.uiState.collect {} }

        viewModel.loadMeal(5L)
        advanceUntilIdle()

        var duplicatedId = 0L
        viewModel.duplicateMealToToday { newId ->
            duplicatedId = newId
        }
        advanceUntilIdle()

        assertTrue(duplicatedId > 0)
        assertTrue(viewModel.uiState.value.isDuplicated)

        val duplicated = mealLogRepository.getMealById(duplicatedId)
        assertNotNull(duplicated)
        assertEquals(LocalDate.now().toString(), duplicated?.meal?.date)
        assertEquals(350.0, duplicated?.meal?.totalCalories ?: 0.0, 0.1)
        assertEquals(1, duplicated?.items?.size)

        collectJob.cancel()
    }
}
