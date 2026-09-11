package com.lumina.nutrition.feature.logging

import com.lumina.nutrition.domain.model.FoodItem
import com.lumina.nutrition.domain.model.FoodSource
import com.lumina.nutrition.domain.model.MealType
import com.lumina.nutrition.fakes.FakeFoodRepository
import com.lumina.nutrition.fakes.FakeMealLogRepository
import com.lumina.nutrition.feature.logging.detail.FoodDetailViewModel
import com.lumina.nutrition.feature.logging.state.LoggingSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class FoodDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var foodRepository: FakeFoodRepository
    private lateinit var mealLogRepository: FakeMealLogRepository
    private lateinit var loggingSessionManager: LoggingSessionManager

    private val testFood = FoodItem(
        id = 1L,
        source = FoodSource.USDA,
        externalId = "201",
        name = "Oatmeal Raw",
        brand = "Quaker",
        servingDescription = "40g",
        servingGrams = 40.0,
        calories = 150.0,
        proteinG = 5.0,
        carbsG = 27.0,
        fatG = 3.0,
        fiberG = 4.0,
        barcodeUpc = null,
        isUserCreated = false,
        createdAt = 1000L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        foodRepository = FakeFoodRepository()
        mealLogRepository = FakeMealLogRepository()
        loggingSessionManager = LoggingSessionManager()
        loggingSessionManager.setSelectedFood(testFood, MealType.BREAKFAST)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testServingMultiplierScalesNutrients() = runTest(testDispatcher) {
        val viewModel = FoodDetailViewModel(foodRepository, mealLogRepository, loggingSessionManager)
        advanceUntilIdle()

        val initialState = viewModel.uiState.value
        assertEquals(150, initialState.scaledCalories)
        assertEquals(5.0, initialState.scaledProtein, 0.0)
        assertEquals(27.0, initialState.scaledCarbs, 0.0)
        assertEquals(3.0, initialState.scaledFat, 0.0)

        // 2x serving
        viewModel.setServings(2.0)
        val scaledState = viewModel.uiState.value
        assertEquals(300, scaledState.scaledCalories)
        assertEquals(10.0, scaledState.scaledProtein, 0.0)
        assertEquals(54.0, scaledState.scaledCarbs, 0.0)
        assertEquals(6.0, scaledState.scaledFat, 0.0)
        assertEquals(80.0, scaledState.scaledGrams, 0.0)
    }

    @Test
    fun testLogFoodPersistsToMealLogRepository() = runTest(testDispatcher) {
        val viewModel = FoodDetailViewModel(foodRepository, mealLogRepository, loggingSessionManager)
        viewModel.setMealType(MealType.DINNER)
        viewModel.setServings(1.5)

        var loggedSuccess = false
        viewModel.logFood {
            loggedSuccess = true
        }
        advanceUntilIdle()

        assertTrue(loggedSuccess)
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val mealsWithItems = mealLogRepository.observeMealsWithItemsByDate(today).first()
        assertEquals(1, mealsWithItems.size)
        val dinner = mealsWithItems[0].meal
        assertEquals(MealType.DINNER, dinner.mealType)
        assertEquals(225.0, dinner.totalCalories, 0.0) // 150 * 1.5 = 225.0

        val items = mealsWithItems[0].items
        assertEquals(1, items.size)
        assertEquals("Oatmeal Raw", items[0].name)
        assertEquals(225.0, items[0].calories, 0.0)
        assertEquals(7.5, items[0].proteinG, 0.0)
    }
}
