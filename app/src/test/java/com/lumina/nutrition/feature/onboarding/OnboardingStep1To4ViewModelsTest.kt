package com.lumina.nutrition.feature.onboarding

import com.lumina.nutrition.domain.model.GoalType
import com.lumina.nutrition.feature.onboarding.allergies.AllergiesViewModel
import com.lumina.nutrition.feature.onboarding.diet.DietPreferencesViewModel
import com.lumina.nutrition.feature.onboarding.goal.GoalSelectionViewModel
import com.lumina.nutrition.feature.onboarding.state.OnboardingSessionManager
import com.lumina.nutrition.feature.onboarding.welcome.WelcomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingStep1To4ViewModelsTest {

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
    fun testWelcomeViewModelResetsDraft() {
        val viewModel = WelcomeViewModel(sessionManager)
        assertEquals("Lumina", viewModel.uiState.value.title)

        sessionManager.updateGoal(GoalType.MUSCLE)
        assertEquals(GoalType.MUSCLE, sessionManager.getSnapshot().goal)

        viewModel.onGetStarted()
        assertEquals(GoalType.LOSE, sessionManager.getSnapshot().goal)
    }

    @Test
    fun testGoalSelectionViewModelInitialStateAndSelection() {
        val viewModel = GoalSelectionViewModel(sessionManager)
        assertEquals(GoalType.LOSE, viewModel.uiState.value.selectedGoal)

        viewModel.selectGoal(GoalType.MUSCLE)
        assertEquals(GoalType.MUSCLE, viewModel.uiState.value.selectedGoal)
        assertEquals(GoalType.MUSCLE, sessionManager.getSnapshot().goal)

        viewModel.selectGoal(GoalType.HEALTHIER)
        assertEquals(GoalType.HEALTHIER, viewModel.uiState.value.selectedGoal)
        assertEquals(GoalType.HEALTHIER, sessionManager.getSnapshot().goal)
    }

    @Test
    fun testDietPreferencesViewModelTogglesAndExclusiveNone() {
        val viewModel = DietPreferencesViewModel(sessionManager)
        assertEquals(listOf("none"), viewModel.uiState.value.selectedDiets)

        // Selecting vegetarian removes 'none'
        viewModel.toggleDiet("vegetarian")
        assertEquals(listOf("vegetarian"), viewModel.uiState.value.selectedDiets)
        assertEquals(listOf("vegetarian"), sessionManager.getSnapshot().dietTags)

        // Multi-select high_protein
        viewModel.toggleDiet("high_protein")
        assertTrue(viewModel.uiState.value.selectedDiets.contains("vegetarian"))
        assertTrue(viewModel.uiState.value.selectedDiets.contains("high_protein"))

        // Unselect high_protein
        viewModel.toggleDiet("high_protein")
        assertFalse(viewModel.uiState.value.selectedDiets.contains("high_protein"))
        assertTrue(viewModel.uiState.value.selectedDiets.contains("vegetarian"))

        // Selecting 'none' clears all other selections
        viewModel.toggleDiet("none")
        assertEquals(listOf("none"), viewModel.uiState.value.selectedDiets)
    }

    @Test
    fun testAllergiesViewModelToggles() {
        val viewModel = AllergiesViewModel(sessionManager)
        assertTrue(viewModel.uiState.value.selectedAllergies.isEmpty())

        viewModel.toggleAllergen("peanuts")
        assertEquals(listOf("peanuts"), viewModel.uiState.value.selectedAllergies)
        assertEquals(listOf("peanuts"), sessionManager.getSnapshot().allergies)

        viewModel.toggleAllergen("milk")
        assertEquals(listOf("peanuts", "milk"), viewModel.uiState.value.selectedAllergies)

        viewModel.toggleAllergen("peanuts")
        assertEquals(listOf("milk"), viewModel.uiState.value.selectedAllergies)
        assertEquals(listOf("milk"), sessionManager.getSnapshot().allergies)
    }
}
