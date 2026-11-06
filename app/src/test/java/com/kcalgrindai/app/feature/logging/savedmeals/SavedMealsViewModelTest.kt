package com.kcalgrindai.app.feature.logging.savedmeals

import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.ItemSource
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.model.SavedMeal
import com.kcalgrindai.app.domain.model.SavedMealItem
import com.kcalgrindai.app.domain.model.SavedMealWithItems
import com.kcalgrindai.app.domain.repository.SavedMealRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
class SavedMealsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeSavedMealRepository: FakeSavedMealRepository
    private lateinit var viewModel: SavedMealsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSavedMealRepository = FakeSavedMealRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsSavedMealsFromRepository() = runTest {
        val sampleMeal = SavedMealWithItems(
            savedMeal = SavedMeal(
                id = 1L,
                name = "Post-Workout Shake",
                defaultMealType = MealType.SNACK
            ),
            items = listOf(
                SavedMealItem(
                    id = 10L,
                    savedMealId = 1L,
                    name = "Whey Protein",
                    servingDescription = "1 scoop",
                    servingGrams = 30.0,
                    calories = 120.0,
                    proteinG = 24.0,
                    carbsG = 2.0,
                    fatG = 1.5,
                    source = ItemSource.USER_ENTERED
                )
            )
        )
        fakeSavedMealRepository.emitMeals(listOf(sampleMeal))

        viewModel = SavedMealsViewModel(fakeSavedMealRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.savedMeals.size)
        assertEquals("Post-Workout Shake", state.savedMeals[0].savedMeal.name)
        assertEquals(120.0, state.savedMeals[0].totalCalories, 0.001)
        assertEquals(24.0, state.savedMeals[0].totalProteinG, 0.001)
    }

    @Test
    fun renameMealFlow_setsRenamingMeal_andUpdatesNameOnConfirm() = runTest {
        val sampleMeal = SavedMealWithItems(
            savedMeal = SavedMeal(id = 5L, name = "Old Name", defaultMealType = MealType.LUNCH),
            items = emptyList()
        )
        fakeSavedMealRepository.emitMeals(listOf(sampleMeal))
        viewModel = SavedMealsViewModel(fakeSavedMealRepository)
        advanceUntilIdle()

        viewModel.promptRename(sampleMeal)
        assertEquals(sampleMeal, viewModel.uiState.value.renamingMeal)

        viewModel.confirmRename("New High Protein Lunch")
        advanceUntilIdle()

        assertEquals("New High Protein Lunch", fakeSavedMealRepository.renamedMeals[5L])
        assertNull(viewModel.uiState.value.renamingMeal)
        assertTrue(viewModel.uiState.value.userMessage?.contains("New High Protein Lunch") == true)
    }

    @Test
    fun deleteMealFlow_setsDeletingMeal_andDeletesOnConfirm() = runTest {
        val sampleMeal = SavedMealWithItems(
            savedMeal = SavedMeal(id = 12L, name = "Temp Meal", defaultMealType = MealType.DINNER),
            items = emptyList()
        )
        fakeSavedMealRepository.emitMeals(listOf(sampleMeal))
        viewModel = SavedMealsViewModel(fakeSavedMealRepository)
        advanceUntilIdle()

        viewModel.promptDelete(sampleMeal)
        assertEquals(sampleMeal, viewModel.uiState.value.deletingMeal)

        viewModel.confirmDelete()
        advanceUntilIdle()

        assertTrue(fakeSavedMealRepository.deletedMealIds.contains(12L))
        assertNull(viewModel.uiState.value.deletingMeal)
    }

    @Test
    fun logMealFlow_setsLoggingMeal_andCallsRepositoryWithSlotAndDate() = runTest {
        val sampleMeal = SavedMealWithItems(
            savedMeal = SavedMeal(id = 7L, name = "Quick Breakfast", defaultMealType = MealType.BREAKFAST),
            items = emptyList()
        )
        fakeSavedMealRepository.emitMeals(listOf(sampleMeal))
        viewModel = SavedMealsViewModel(fakeSavedMealRepository)
        advanceUntilIdle()

        viewModel.promptLogMeal(sampleMeal)
        assertEquals(sampleMeal, viewModel.uiState.value.loggingMeal)

        var callbackCalled = false
        viewModel.confirmLogMeal(
            targetDate = "2026-11-04",
            targetSlot = MealType.BREAKFAST,
            onLogged = { callbackCalled = true }
        )
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertEquals(7L, fakeSavedMealRepository.lastLoggedMealId)
        assertEquals("2026-11-04", fakeSavedMealRepository.lastLoggedDate)
        assertEquals(MealType.BREAKFAST, fakeSavedMealRepository.lastLoggedSlot)
        assertNull(viewModel.uiState.value.loggingMeal)
    }
}

private class FakeSavedMealRepository : SavedMealRepository {
    private val mealsFlow = MutableStateFlow<List<SavedMealWithItems>>(emptyList())
    val renamedMeals = mutableMapOf<Long, String>()
    val deletedMealIds = mutableListOf<Long>()
    var lastLoggedMealId: Long? = null
    var lastLoggedDate: String? = null
    var lastLoggedSlot: MealType? = null

    fun emitMeals(meals: List<SavedMealWithItems>) {
        mealsFlow.value = meals
    }

    override suspend fun saveMealAsTemplate(name: String, mealType: MealType, items: List<FoodLogItem>): Long {
        return 99L
    }

    override suspend fun renameSavedMeal(id: Long, newName: String) {
        renamedMeals[id] = newName
    }

    override suspend fun deleteSavedMeal(id: Long) {
        deletedMealIds.add(id)
    }

    override fun observeSavedMeals(): Flow<List<SavedMealWithItems>> = mealsFlow.asStateFlow()

    override suspend fun getSavedMealById(id: Long): SavedMealWithItems? = mealsFlow.value.find { it.savedMeal.id == id }

    override suspend fun logSavedMeal(savedMealId: Long, targetDate: String, targetMealType: MealType): Long {
        lastLoggedMealId = savedMealId
        lastLoggedDate = targetDate
        lastLoggedSlot = targetMealType
        return 1234L
    }
}
