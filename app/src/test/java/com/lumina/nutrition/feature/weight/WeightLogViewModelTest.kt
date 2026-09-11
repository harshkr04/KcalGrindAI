package com.lumina.nutrition.feature.weight

import com.lumina.nutrition.domain.model.GoalType
import com.lumina.nutrition.domain.model.UnitSystem
import com.lumina.nutrition.domain.model.UserProfile
import com.lumina.nutrition.domain.model.WeightEntry
import com.lumina.nutrition.fakes.FakeUserProfileRepository
import com.lumina.nutrition.fakes.FakeWeightRepository
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
class WeightLogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var weightRepository: FakeWeightRepository
    private lateinit var userProfileRepository: FakeUserProfileRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        weightRepository = FakeWeightRepository()
        userProfileRepository = FakeUserProfileRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createTestProfile(units: UnitSystem, weightKg: Double) = UserProfile(
        id = 1L,
        goal = GoalType.LOSE,
        units = units,
        age = 28,
        heightCm = 175.0,
        weightKg = weightKg,
        goalWeightKg = 70.0,
        activityLevel = com.lumina.nutrition.domain.model.ActivityLevel.MODERATE,
        dietTags = emptyList(),
        allergies = emptyList(),
        targetsSource = com.lumina.nutrition.domain.model.TargetBasis.CUSTOM,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    @Test
    fun `loadInitialData respects metric user profile`() = runTest {
        userProfileRepository.saveProfile(createTestProfile(UnitSystem.METRIC, 74.2))

        val viewModel = WeightLogViewModel(weightRepository, userProfileRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(UnitSystem.METRIC, state.unitSystem)
        assertEquals("kg", state.unitLabel)
        assertEquals("74.2", state.weightInput)
    }

    @Test
    fun `loadInitialData respects imperial user profile`() = runTest {
        userProfileRepository.saveProfile(createTestProfile(UnitSystem.IMPERIAL, 70.0))

        val viewModel = WeightLogViewModel(weightRepository, userProfileRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(UnitSystem.IMPERIAL, state.unitSystem)
        assertEquals("lbs", state.unitLabel)
        assertEquals("154.3", state.weightInput)
    }

    @Test
    fun `saveWeight validates invalid input and sets error message`() = runTest {
        val viewModel = WeightLogViewModel(weightRepository, userProfileRepository)
        advanceUntilIdle()

        viewModel.onWeightChange("")
        var successCalled = false
        viewModel.saveWeight { successCalled = true }
        advanceUntilIdle()

        assertFalse(successCalled)
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertEquals("Please enter a valid weight", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `saveWeight metric saves rounded weightKg to repository`() = runTest {
        userProfileRepository.saveProfile(createTestProfile(UnitSystem.METRIC, 74.2))

        val viewModel = WeightLogViewModel(weightRepository, userProfileRepository)
        advanceUntilIdle()

        viewModel.onWeightChange("73.8")
        viewModel.onNoteChange("Morning weigh-in")

        var successCalled = false
        viewModel.saveWeight { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertNull(viewModel.uiState.value.errorMessage)

        val latest = weightRepository.getLatestWeightEntry()
        assertNotNull(latest)
        assertEquals(73.8, latest!!.weightKg, 0.01)
        assertEquals("Morning weigh-in", latest.note)
    }
}
