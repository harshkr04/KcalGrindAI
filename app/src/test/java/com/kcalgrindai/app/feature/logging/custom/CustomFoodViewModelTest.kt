package com.kcalgrindai.app.feature.logging.custom

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kcalgrindai.app.domain.model.FoodItem
import com.kcalgrindai.app.domain.model.FoodSource
import com.kcalgrindai.app.fakes.FakeFoodRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CustomFoodViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeFoodRepository: FakeFoodRepository
    private lateinit var viewModel: CustomFoodViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeFoodRepository = FakeFoodRepository()
        viewModel = CustomFoodViewModel(fakeFoodRepository, SavedStateHandle())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasCorrectDefaults() {
        val state = viewModel.uiState.value
        assertEquals("", state.name)
        assertEquals("", state.brand)
        assertEquals("1", state.servingSize)
        assertEquals("serving", state.servingUnit)
        assertEquals("100", state.servingGrams)
        assertEquals("", state.calories)
        assertEquals("", state.protein)
        assertEquals("", state.carbs)
        assertEquals("", state.fat)
        assertNull(state.macroWarning)
        assertFalse(state.isSaving)
        assertFalse(state.isEditMode)
    }

    @Test
    fun validation_failsWhenRequiredFieldsAreBlank() {
        val isValid = viewModel.validate()
        assertFalse(isValid)

        val state = viewModel.uiState.value
        assertEquals("Food name is required", state.nameError)
        assertEquals("Calories are required", state.caloriesError)
        assertEquals("Protein is required", state.proteinError)
        assertEquals("Carbs are required", state.carbsError)
        assertEquals("Fat is required", state.fatError)
    }

    @Test
    fun validation_failsWhenNumbersAreNegativeOrExceedLimits() {
        viewModel.onNameChange("Extreme Food")
        viewModel.onServingSizeChange("-2")
        viewModel.onCaloriesChange("6000") // Exceeds 5000 kcal limit
        viewModel.onProteinChange("600")   // Exceeds 500g limit
        viewModel.onCarbsChange("-1")      // Negative
        viewModel.onFatChange("0")

        val isValid = viewModel.validate()
        assertFalse(isValid)

        val state = viewModel.uiState.value
        assertEquals("Must be greater than 0", state.servingSizeError)
        assertEquals("Cannot exceed 5,000 kcal per serving", state.caloriesError)
        assertEquals("Cannot exceed 500g", state.proteinError)
        assertEquals("Cannot be negative", state.carbsError)
    }

    @Test
    fun macroWarning_warnsWhenCaloriesInconsistentWithMacros_doesNotBlockSave() = runTest {
        viewModel.onNameChange("Protein Pudding")
        viewModel.onServingSizeChange("1")
        viewModel.onServingUnitChange("cup")
        viewModel.onServingGramsChange("150")

        // 500 kcal entered, but P=10g (40), C=10g (40), F=0g (0) -> sum = 80 kcal
        viewModel.onCaloriesChange("500")
        viewModel.onProteinChange("10")
        viewModel.onCarbsChange("10")
        viewModel.onFatChange("0")

        val state = viewModel.uiState.value
        assertNotNull(state.macroWarning)
        assertTrue(state.macroWarning!!.contains("Macros sum to 80 kcal"))

        // Verification that it can still be validated and saved
        assertTrue(viewModel.validate())

        viewModel.events.test {
            viewModel.saveCustomFood()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is CustomFoodEvent.SaveSuccess)
            cancelAndIgnoreRemainingEvents()
        }

        val saved = fakeFoodRepository.getFoodById(1L)
        assertNotNull(saved)
        assertEquals("Protein Pudding", saved!!.name)
        assertEquals(500.0, saved.calories, 0.001)
        assertEquals(FoodSource.USER_ENTERED, saved.source)
        assertTrue(saved.isUserCreated)
    }

    @Test
    fun saveCustomFood_storesSourceUserEntered_andAllMacros() = runTest {
        viewModel.onNameChange("High Protein Oats")
        viewModel.onBrandChange("My Kitchen")
        viewModel.onServingSizeChange("1")
        viewModel.onServingUnitChange("bowl")
        viewModel.onServingGramsChange("180")
        viewModel.onCaloriesChange("340")
        viewModel.onProteinChange("25")
        viewModel.onCarbsChange("45")
        viewModel.onFatChange("6")
        viewModel.onFiberChange("7")
        viewModel.onSugarChange("4")
        viewModel.onSodiumChange("120")

        viewModel.events.test {
            viewModel.saveCustomFood()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is CustomFoodEvent.SaveSuccess)
            assertEquals(1L, (event as CustomFoodEvent.SaveSuccess).foodId)
            cancelAndIgnoreRemainingEvents()
        }

        val saved = fakeFoodRepository.getFoodById(1L)
        assertNotNull(saved)
        assertEquals("High Protein Oats", saved!!.name)
        assertEquals("My Kitchen", saved.brand)
        assertEquals("1 bowl", saved.servingDescription)
        assertEquals(180.0, saved.servingGrams, 0.001)
        assertEquals(340.0, saved.calories, 0.001)
        assertEquals(25.0, saved.proteinG, 0.001)
        assertEquals(45.0, saved.carbsG, 0.001)
        assertEquals(6.0, saved.fatG, 0.001)
        assertEquals(7.0, saved.fiberG, 0.001)
        assertEquals(4.0, saved.sugarG, 0.001)
        assertEquals(120.0, saved.sodiumMg, 0.001)
        assertEquals(FoodSource.USER_ENTERED, saved.source)
        assertTrue(saved.isUserCreated)
    }

    @Test
    fun loadFoodForEdit_populatesState_andCanUpdateOrDelete() = runTest {
        val existing = FoodItem(
            id = 42L,
            source = FoodSource.USER_ENTERED,
            name = "Special Smoothie",
            brand = "NutriBullet",
            servingDescription = "1 bottle",
            servingGrams = 350.0,
            calories = 220.0,
            proteinG = 15.0,
            carbsG = 30.0,
            fatG = 3.0,
            fiberG = 5.0,
            sugarG = 18.0,
            sodiumMg = 80.0,
            isUserCreated = true,
            createdAt = 1000L
        )
        fakeFoodRepository.saveFood(existing)

        val editVm = CustomFoodViewModel(
            fakeFoodRepository,
            SavedStateHandle(mapOf("foodId" to 42L))
        )
        advanceUntilIdle()

        val editState = editVm.uiState.value
        assertTrue(editState.isEditMode)
        assertEquals(42L, editState.id)
        assertEquals("Special Smoothie", editState.name)
        assertEquals("NutriBullet", editState.brand)
        assertEquals("1", editState.servingSize)
        assertEquals("bottle", editState.servingUnit)
        assertEquals("220", editState.calories)

        // Test delete
        editVm.events.test {
            editVm.deleteCustomFood()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is CustomFoodEvent.DeleteSuccess)
            cancelAndIgnoreRemainingEvents()
        }

        assertNull(fakeFoodRepository.getFoodById(42L))
    }
}
