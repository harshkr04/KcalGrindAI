package com.lumina.nutrition.feature.onboarding

import com.lumina.nutrition.domain.model.ActivityLevel
import com.lumina.nutrition.domain.model.UnitSystem
import com.lumina.nutrition.feature.onboarding.activity.ActivityLevelViewModel
import com.lumina.nutrition.feature.onboarding.body.BodyMetricsViewModel
import com.lumina.nutrition.feature.onboarding.personal.PersonalDetailsViewModel
import com.lumina.nutrition.feature.onboarding.state.OnboardingSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
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
class OnboardingStep5To7ViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var sessionManager: OnboardingSessionManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sessionManager = OnboardingSessionManager()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testPersonalDetailsViewModelValidationAndUnitSwitching() {
        val viewModel = PersonalDetailsViewModel(sessionManager)
        assertTrue(viewModel.uiState.value.isValid)
        assertNull(viewModel.uiState.value.ageError)
        assertNull(viewModel.uiState.value.heightError)

        // Invalid age < 13
        viewModel.onAgeChanged("10")
        assertFalse(viewModel.uiState.value.isValid)
        assertNotNull(viewModel.uiState.value.ageError)

        // Valid age
        viewModel.onAgeChanged("30")
        assertTrue(viewModel.uiState.value.isValid)
        assertNull(viewModel.uiState.value.ageError)

        // Invalid height > 250 cm
        viewModel.onHeightCmChanged("280")
        assertFalse(viewModel.uiState.value.isValid)
        assertNotNull(viewModel.uiState.value.heightError)

        // Valid height
        viewModel.onHeightCmChanged("180")
        assertTrue(viewModel.uiState.value.isValid)
        assertNull(viewModel.uiState.value.heightError)

        // Unit system toggle to Imperial
        viewModel.setUnits(UnitSystem.IMPERIAL)
        assertEquals(UnitSystem.IMPERIAL, viewModel.uiState.value.units)
        assertEquals(UnitSystem.IMPERIAL, sessionManager.getSnapshot().units)
    }

    @Test
    fun testActivityLevelViewModelSelection() {
        val viewModel = ActivityLevelViewModel(sessionManager)
        assertEquals(ActivityLevel.MODERATE, viewModel.uiState.value.selectedActivity)

        // Select each activity level in turn and verify UI state matches only chosen option
        for (level in ActivityLevel.entries) {
            viewModel.selectActivity(level)
            assertEquals(level, viewModel.uiState.value.selectedActivity)
            assertEquals(level, sessionManager.getSnapshot().activityLevel)

            for (other in ActivityLevel.entries) {
                if (other == level) {
                    assertEquals(other, viewModel.uiState.value.selectedActivity)
                } else {
                    assertTrue(other != viewModel.uiState.value.selectedActivity)
                }
            }
        }
    }

    @Test
    fun testBodyMetricsViewModelValidationAndNote() {
        val viewModel = BodyMetricsViewModel(sessionManager)
        assertTrue(viewModel.uiState.value.isValid)
        assertNull(viewModel.uiState.value.weightError)
        assertNull(viewModel.uiState.value.goalWeightError)

        // Weight out of range
        viewModel.onWeightChanged("20")
        assertFalse(viewModel.uiState.value.isValid)
        assertNotNull(viewModel.uiState.value.weightError)

        // Valid weight
        viewModel.onWeightChanged("75")
        assertTrue(viewModel.uiState.value.isValid)
        assertNull(viewModel.uiState.value.weightError)

        // Contradictory goal weight triggers guidance note
        viewModel.onGoalWeightChanged("85") // Goal is LOSE, but target is 85kg > 75kg
        assertNotNull(viewModel.uiState.value.note)
    }
}
