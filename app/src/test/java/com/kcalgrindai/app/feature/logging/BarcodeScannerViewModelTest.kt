package com.kcalgrindai.app.feature.logging

import com.kcalgrindai.app.domain.model.FoodItem
import com.kcalgrindai.app.domain.model.FoodSource
import com.kcalgrindai.app.fakes.FakeFoodRepository
import com.kcalgrindai.app.feature.logging.scanner.BarcodeScannerViewModel
import com.kcalgrindai.app.feature.logging.state.LoggingSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class BarcodeScannerViewModelTest {

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
    fun testBarcodeDetectedAndFoundNavigatesToDetail() = runTest(testDispatcher) {
        val nutella = FoodItem(
            id = 10L,
            source = FoodSource.OPEN_FOOD_FACTS,
            externalId = "3017620422003",
            name = "Nutella Hazelnut Spread",
            brand = "Ferrero",
            servingDescription = "15g",
            servingGrams = 15.0,
            calories = 80.0,
            proteinG = 1.0,
            carbsG = 8.5,
            fatG = 4.5,
            fiberG = 0.5,
            barcodeUpc = "3017620422003",
            isUserCreated = false,
            createdAt = 1000L
        )
        foodRepository.saveFood(nutella)

        val viewModel = BarcodeScannerViewModel(foodRepository, loggingSessionManager)
        var navigatedToDetail = false

        viewModel.onBarcodeDetected("3017620422003") {
            navigatedToDetail = true
        }
        advanceUntilIdle()

        assertTrue(navigatedToDetail)
        assertFalse(viewModel.uiState.value.isLookingUp)
        assertFalse(viewModel.uiState.value.showNotFoundDialog)
        assertEquals(nutella, loggingSessionManager.state.value.selectedFood)
    }

    @Test
    fun testBarcodeNotFoundTriggersDialogAndFallback() = runTest(testDispatcher) {
        val viewModel = BarcodeScannerViewModel(foodRepository, loggingSessionManager)
        var navigatedToDetail = false

        viewModel.onBarcodeDetected("9999999999999") {
            navigatedToDetail = true
        }
        advanceUntilIdle()

        assertFalse(navigatedToDetail)
        assertFalse(viewModel.uiState.value.isLookingUp)
        assertTrue(viewModel.uiState.value.showNotFoundDialog)
        assertEquals("9999999999999", viewModel.uiState.value.scannedBarcode)

        var navigatedToSearch = false
        viewModel.fallbackToManualSearch {
            navigatedToSearch = true
        }

        assertTrue(navigatedToSearch)
        assertFalse(viewModel.uiState.value.showNotFoundDialog)
        assertEquals("9999999999999", loggingSessionManager.state.value.prefilledSearchQuery)
    }
}
