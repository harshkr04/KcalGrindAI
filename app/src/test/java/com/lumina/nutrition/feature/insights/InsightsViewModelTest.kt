package com.lumina.nutrition.feature.insights

import com.lumina.nutrition.domain.model.FoodLogItem
import com.lumina.nutrition.domain.model.ItemSource
import com.lumina.nutrition.domain.model.LogSource
import com.lumina.nutrition.domain.model.MealLog
import com.lumina.nutrition.domain.model.MealType
import com.lumina.nutrition.domain.model.NutritionGoal
import com.lumina.nutrition.domain.model.WeightEntry
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mealLogRepository: FakeMealLogRepository
    private lateinit var nutritionGoalRepository: FakeNutritionGoalRepository
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var weightRepository: FakeWeightRepository
    private lateinit var waterLogRepository: FakeWaterLogRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mealLogRepository = FakeMealLogRepository()
        nutritionGoalRepository = FakeNutritionGoalRepository()
        userProfileRepository = FakeUserProfileRepository()
        weightRepository = FakeWeightRepository()
        waterLogRepository = FakeWaterLogRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `computes 7-day calorie average, tracking rate, and macro adherence accurately`() = runTest {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        nutritionGoalRepository.saveGoal(
            NutritionGoal(
                id = 1L,
                userId = 1L,
                calories = 2000,
                proteinG = 120.0,
                carbsG = 200.0,
                fatG = 65.0,
                waterLiters = 2.5,
                waterGlasses = 10,
                bmr = 1700.0,
                tdee = 2200.0,
                isCustom = false,
                updatedAt = System.currentTimeMillis()
            )
        )

        // Meal on today: 600 kcal, 40g P, 60g C, 20g F
        mealLogRepository.saveMeal(
            MealLog(1L, today.toString(), MealType.LUNCH, 600.0, System.currentTimeMillis(), LogSource.AI_TEXT, true),
            listOf(
                FoodLogItem(1L, 1L, null, "Salmon & Rice", null, "1 plate", 300.0, 600.0, 40.0, 60.0, 20.0, 3.0, ItemSource.AI, 0.95f, true)
            )
        )

        // Meal on yesterday: 400 kcal, 30g P, 40g C, 10g F
        mealLogRepository.saveMeal(
            MealLog(2L, yesterday.toString(), MealType.LUNCH, 400.0, System.currentTimeMillis(), LogSource.AI_TEXT, true),
            listOf(
                FoodLogItem(2L, 2L, null, "Chicken Salad", null, "1 bowl", 250.0, 400.0, 30.0, 40.0, 10.0, 4.0, ItemSource.AI, 0.95f, true)
            )
        )

        // Meal on two days ago: 800 kcal, 50g P, 80g C, 30g F
        mealLogRepository.saveMeal(
            MealLog(3L, twoDaysAgo.toString(), MealType.DINNER, 800.0, System.currentTimeMillis(), LogSource.AI_TEXT, true),
            listOf(
                FoodLogItem(3L, 3L, null, "Steak & Potatoes", null, "1 portion", 400.0, 800.0, 50.0, 80.0, 30.0, 5.0, ItemSource.AI, 0.95f, true)
            )
        )

        // Weight entries
        weightRepository.saveWeightEntry(WeightEntry(1L, 75.0, twoDaysAgo.toString(), null, System.currentTimeMillis() - 172800000))
        weightRepository.saveWeightEntry(WeightEntry(2L, 74.5, today.toString(), null, System.currentTimeMillis()))

        // Water entries
        waterLogRepository.logWater(500, today.toString())
        waterLogRepository.logWater(250, today.toString())

        val viewModel = InsightsViewModel(
            mealLogRepository,
            nutritionGoalRepository,
            userProfileRepository,
            weightRepository,
            waterLogRepository
        )

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.daysTrackedCount)
        assertEquals(7, state.totalDaysCount)
        assertEquals(43, state.trackingRatePercent) // 3/7 * 100 = 42.85 -> 43%

        // Avg calories: (600 + 400 + 800) / 3 = 600 kcal
        assertEquals(600, state.avgCaloriesPerDay)
        assertEquals(2000, state.targetCaloriesPerDay)
        assertEquals(-1400, state.calorieDelta)

        // Macros: Protein avg (40+30+50)/3 = 40.0g
        assertEquals(40.0, state.proteinAdherence.avgGrams, 0.1)
        assertEquals(120.0, state.proteinAdherence.targetGrams, 0.1)
        assertEquals(33, state.proteinAdherence.adherencePercent) // 40/120 = 33%

        // Weight
        assertEquals(74.5, state.latestWeightKg!!, 0.1)
        assertEquals(75.0, state.startWeightKg!!, 0.1)
        assertEquals(-0.5, state.weightChangeKg!!, 0.1)
        assertTrue(state.hasWeightData)

        // Tab switching
        viewModel.selectTab(InsightsTab.WEIGHT)
        advanceUntilIdle()
        assertEquals(InsightsTab.WEIGHT, viewModel.uiState.value.selectedTab)

        collectJob.cancel()
    }
}
