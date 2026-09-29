package com.kcalgrindai.app.feature.logging

import com.kcalgrindai.app.domain.model.FoodItem
import com.kcalgrindai.app.domain.model.FoodSource
import com.kcalgrindai.app.fakes.FakeFoodRepository
import com.kcalgrindai.app.feature.logging.search.FoodSearchViewModel
import com.kcalgrindai.app.feature.logging.state.LoggingSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FoodSearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var foodRepository: FakeFoodRepository
    private lateinit var loggingSessionManager: LoggingSessionManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        foodRepository = FakeFoodRepository()
        loggingSessionManager = LoggingSessionManager()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSearchQueriesOnlineAndFilters() = runTest(testDispatcher) {
        val banana = FoodItem(
            id = 1L,
            source = FoodSource.USDA,
            externalId = "101",
            name = "Banana Raw",
            brand = null,
            servingDescription = "1 medium",
            servingGrams = 118.0,
            calories = 105.0,
            proteinG = 1.3,
            carbsG = 27.0,
            fatG = 0.4,
            fiberG = 3.1,
            barcodeUpc = null,
            isUserCreated = false,
            createdAt = 1000L
        )
        foodRepository.saveFood(banana)

        val viewModel = FoodSearchViewModel(foodRepository, loggingSessionManager)
        advanceUntilIdle()

        viewModel.onQueryChanged("Banana")
        advanceTimeBy(400)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Banana", state.query)
        assertFalse(state.isLoading)
        assertFalse(state.isOffline)
        assertEquals(1, state.searchResults.size)
        assertEquals("Banana Raw", state.searchResults[0].name)
    }

    @Test
    fun testSearchHandlesOfflineFailureGracefully() = runTest(testDispatcher) {
        val apple = FoodItem(
            id = 2L,
            source = FoodSource.USDA,
            externalId = "102",
            name = "Apple Fuji",
            brand = null,
            servingDescription = "1 medium",
            servingGrams = 182.0,
            calories = 95.0,
            proteinG = 0.5,
            carbsG = 25.0,
            fatG = 0.3,
            fiberG = 4.4,
            barcodeUpc = null,
            isUserCreated = false,
            createdAt = 1000L
        )
        foodRepository.saveFood(apple)
        foodRepository.shouldFailOnline = true

        val viewModel = FoodSearchViewModel(foodRepository, loggingSessionManager)
        advanceUntilIdle()

        viewModel.onQueryChanged("Apple")
        advanceTimeBy(400)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isOffline)
        assertNotNull(state.errorMessage)
        assertEquals(1, state.cachedResults.size)
        assertEquals("Apple Fuji", state.cachedResults[0].name)
    }

    @Test
    fun testSelectFoodSetsDraftAndNavigates() = runTest(testDispatcher) {
        val food = FoodItem(
            id = 3L,
            source = FoodSource.USDA,
            externalId = "103",
            name = "Chicken Breast",
            brand = null,
            servingDescription = "100g",
            servingGrams = 100.0,
            calories = 165.0,
            proteinG = 31.0,
            carbsG = 0.0,
            fatG = 3.6,
            fiberG = 0.0,
            barcodeUpc = null,
            isUserCreated = false,
            createdAt = 1000L
        )

        val viewModel = FoodSearchViewModel(foodRepository, loggingSessionManager)
        var navigated = false

        viewModel.selectFood(food) {
            navigated = true
        }

        assertTrue(navigated)
        assertEquals(food, loggingSessionManager.state.value.selectedFood)
    }
}
