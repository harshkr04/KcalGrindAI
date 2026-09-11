package com.lumina.nutrition.feature.aicoach

import com.lumina.nutrition.domain.model.FoodLogItem
import com.lumina.nutrition.domain.model.ItemSource
import com.lumina.nutrition.domain.model.MealType
import com.lumina.nutrition.fakes.FakeAICoachRepository
import com.lumina.nutrition.fakes.FakeMealLogRepository
import com.lumina.nutrition.fakes.FakeNutritionGoalRepository
import com.lumina.nutrition.fakes.FakeUserProfileRepository
import com.lumina.nutrition.fakes.FakeWaterLogRepository
import com.lumina.nutrition.fakes.FakeWeightRepository
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
class AiChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var aiCoachRepository: FakeAICoachRepository
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var nutritionGoalRepository: FakeNutritionGoalRepository
    private lateinit var mealLogRepository: FakeMealLogRepository
    private lateinit var waterLogRepository: FakeWaterLogRepository
    private lateinit var weightRepository: FakeWeightRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        aiCoachRepository = FakeAICoachRepository()
        userProfileRepository = FakeUserProfileRepository()
        nutritionGoalRepository = FakeNutritionGoalRepository()
        mealLogRepository = FakeMealLogRepository()
        waterLogRepository = FakeWaterLogRepository()
        weightRepository = FakeWeightRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `auto-logs water when suggested by AI assistant`() = runTest {
        val viewModel = AiChatViewModel(
            aiCoachRepository,
            userProfileRepository,
            nutritionGoalRepository,
            mealLogRepository,
            waterLogRepository,
            weightRepository
        )

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onInputChanged("log a glass of water")
        viewModel.sendMessage()
        advanceUntilIdle()

        val today = LocalDate.now().toString()
        val waterLogs = waterLogRepository.getWaterLogsByDate(today)
        assertEquals(1, waterLogs.size)
        assertEquals(250, waterLogs[0].amountMl)
        assertNotNull(viewModel.uiState.value.actionSuccessMessage)
        assertTrue(viewModel.uiState.value.actionSuccessMessage!!.contains("Logged 250ml water"))

        collectJob.cancel()
    }

    @Test
    fun `proposes food log requiring explicit confirmation before writing to repository`() = runTest {
        val viewModel = AiChatViewModel(
            aiCoachRepository,
            userProfileRepository,
            nutritionGoalRepository,
            mealLogRepository,
            waterLogRepository,
            weightRepository
        )

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val today = LocalDate.now().toString()

        // 1. Ask to log banana
        viewModel.onInputChanged("log a banana for breakfast")
        viewModel.sendMessage()
        advanceUntilIdle()

        // 2. Meal should NOT be written yet
        val mealsBeforeConfirm = mealLogRepository.getMealsWithItemsByDate(today)
        assertEquals(0, mealsBeforeConfirm.size)

        // 3. User taps confirm button
        val items = listOf(
            FoodLogItem(
                id = 0L,
                mealLogId = 0L,
                name = "Banana",
                servingDescription = "120g",
                servingGrams = 120.0,
                calories = 105.0,
                proteinG = 1.3,
                carbsG = 27.0,
                fatG = 0.3,
                fiberG = 3.0,
                source = ItemSource.AI,
                confidence = 0.95f,
                confirmed = true
            )
        )
        viewModel.confirmFoodLog(MealType.BREAKFAST, items)
        advanceUntilIdle()

        // 4. Now meal is written to Room
        val mealsAfterConfirm = mealLogRepository.getMealsWithItemsByDate(today)
        assertEquals(1, mealsAfterConfirm.size)
        assertEquals(105.0, mealsAfterConfirm[0].meal.totalCalories, 0.1)
        assertEquals("Banana", mealsAfterConfirm[0].items[0].name)

        collectJob.cancel()
    }
}
