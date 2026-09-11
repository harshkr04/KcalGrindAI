package com.lumina.nutrition.feature.home

import androidx.test.core.app.ApplicationProvider
import com.lumina.nutrition.data.health.HealthConnectManager
import com.lumina.nutrition.domain.model.FoodLogItem
import com.lumina.nutrition.domain.model.ItemSource
import com.lumina.nutrition.domain.model.LogSource
import com.lumina.nutrition.domain.model.MealLog
import com.lumina.nutrition.domain.model.MealType
import com.lumina.nutrition.domain.model.NutritionGoal
import com.lumina.nutrition.fakes.FakeMealLogRepository
import com.lumina.nutrition.fakes.FakeNutritionGoalRepository
import com.lumina.nutrition.fakes.FakeUserProfileRepository
import com.lumina.nutrition.fakes.FakeWaterLogRepository
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mealLogRepository: FakeMealLogRepository
    private lateinit var nutritionGoalRepository: FakeNutritionGoalRepository
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var waterLogRepository: FakeWaterLogRepository
    private lateinit var healthConnectManager: HealthConnectManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mealLogRepository = FakeMealLogRepository()
        nutritionGoalRepository = FakeNutritionGoalRepository()
        userProfileRepository = FakeUserProfileRepository()
        waterLogRepository = FakeWaterLogRepository()
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        healthConnectManager = HealthConnectManager(context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state shows 0 consumed calories and default targets`() = runTest {
        nutritionGoalRepository.saveGoal(
            NutritionGoal(
                id = 1L,
                userId = 1L,
                calories = 2200,
                proteinG = 160.0,
                carbsG = 240.0,
                fatG = 70.0,
                waterLiters = 2.5,
                waterGlasses = 8,
                bmr = 1750.0,
                tdee = 2200.0,
                isCustom = false,
                updatedAt = System.currentTimeMillis()
            )
        )

        val viewModel = HomeViewModel(
            mealLogRepository = mealLogRepository,
            nutritionGoalRepository = nutritionGoalRepository,
            userProfileRepository = userProfileRepository,
            waterLogRepository = waterLogRepository,
            healthConnectManager = healthConnectManager
        )
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2200, state.targetCalories)
        assertEquals(0, state.consumedCalories)
        assertEquals(2200, state.remainingCalories)
        assertFalse(state.hasLoggedMealsToday)

        collectJob.cancel()
    }

    @Test
    fun `logging meals updates consumed calories and macro calculations`() = runTest {
        nutritionGoalRepository.saveGoal(
            NutritionGoal(
                id = 1L,
                userId = 1L,
                calories = 2000,
                proteinG = 150.0,
                carbsG = 200.0,
                fatG = 65.0,
                waterLiters = 2.5,
                waterGlasses = 8,
                bmr = 1750.0,
                tdee = 2000.0,
                isCustom = false,
                updatedAt = System.currentTimeMillis()
            )
        )

        val todayStr = LocalDate.now().toString()
        val meal = MealLog(
            id = 1L,
            date = todayStr,
            mealType = MealType.LUNCH,
            totalCalories = 650.0,
            loggedAt = System.currentTimeMillis(),
            source = LogSource.AI_PHOTO,
            synced = true
        )
        val items = listOf(
            FoodLogItem(
                id = 1L,
                mealLogId = 1L,
                name = "Grilled Chicken Salad",
                servingDescription = "1 large bowl",
                servingGrams = 350.0,
                calories = 450.0,
                proteinG = 45.0,
                carbsG = 15.0,
                fatG = 18.0,
                fiberG = 4.0,
                source = ItemSource.AI,
                confirmed = true
            ),
            FoodLogItem(
                id = 2L,
                mealLogId = 1L,
                name = "Apple",
                servingDescription = "1 medium",
                servingGrams = 180.0,
                calories = 200.0,
                proteinG = 1.0,
                carbsG = 40.0,
                fatG = 0.5,
                fiberG = 3.0,
                source = ItemSource.MANUAL,
                confirmed = true
            )
        )
        mealLogRepository.saveMeal(meal, items)

        val viewModel = HomeViewModel(
            mealLogRepository = mealLogRepository,
            nutritionGoalRepository = nutritionGoalRepository,
            userProfileRepository = userProfileRepository,
            waterLogRepository = waterLogRepository,
            healthConnectManager = healthConnectManager
        )
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2000, state.targetCalories)
        assertEquals(650, state.consumedCalories)
        assertEquals(1350, state.remainingCalories) // 2000 - 650
        assertEquals(46.0, state.protein.currentGrams, 0.1) // 45 + 1
        assertEquals(55.0, state.carbs.currentGrams, 0.1) // 15 + 40
        assertEquals(18.5, state.fat.currentGrams, 0.1) // 18 + 0.5
        assertTrue(state.hasLoggedMealsToday)

        val lunchSummary = state.mealsGrouped.find { it.mealType == MealType.LUNCH }
        assertEquals(650.0, lunchSummary?.totalCalories ?: 0.0, 0.1)
        assertEquals(2, lunchSummary?.itemCount)

        collectJob.cancel()
    }

    @Test
    fun `manual water logging updates water progress and writes to repository`() = runTest {
        nutritionGoalRepository.saveGoal(
            NutritionGoal(
                id = 1L,
                userId = 1L,
                calories = 2000,
                proteinG = 150.0,
                carbsG = 200.0,
                fatG = 65.0,
                waterLiters = 2.4,
                waterGlasses = 8,
                bmr = 1750.0,
                tdee = 2000.0,
                isCustom = false,
                updatedAt = System.currentTimeMillis()
            )
        )

        val viewModel = HomeViewModel(
            mealLogRepository = mealLogRepository,
            nutritionGoalRepository = nutritionGoalRepository,
            userProfileRepository = userProfileRepository,
            waterLogRepository = waterLogRepository,
            healthConnectManager = healthConnectManager
        )
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.water.consumedMl)
        assertEquals(2400, viewModel.uiState.value.water.targetMl)

        viewModel.logWater(250)
        advanceUntilIdle()

        val stateAfter1 = viewModel.uiState.value
        assertEquals(250, stateAfter1.water.consumedMl)
        assertEquals(1, stateAfter1.water.logsCountToday)

        viewModel.logWater(500)
        advanceUntilIdle()

        val stateAfter2 = viewModel.uiState.value
        assertEquals(750, stateAfter2.water.consumedMl)
        assertEquals(2, stateAfter2.water.logsCountToday)

        viewModel.undoLatestWaterLog()
        advanceUntilIdle()

        val stateAfterUndo = viewModel.uiState.value
        assertEquals(250, stateAfterUndo.water.consumedMl)
        assertEquals(1, stateAfterUndo.water.logsCountToday)

        collectJob.cancel()
    }
}
