package com.lumina.nutrition.feature

import com.lumina.nutrition.domain.model.ActivityLevel
import com.lumina.nutrition.domain.model.GoalType
import com.lumina.nutrition.domain.model.NutritionGoal
import com.lumina.nutrition.domain.model.TargetBasis
import com.lumina.nutrition.domain.model.UnitSystem
import com.lumina.nutrition.domain.model.UserProfile
import com.lumina.nutrition.fakes.FakeNutritionGoalRepository
import com.lumina.nutrition.fakes.FakeUserProfileRepository
import com.lumina.nutrition.feature.profile.ProfileUiState
import com.lumina.nutrition.feature.profile.ProfileViewModel
import com.lumina.nutrition.feature.splash.LaunchDestination
import com.lumina.nutrition.feature.splash.LaunchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileAndLaunchViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var nutritionGoalRepository: FakeNutritionGoalRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userProfileRepository = FakeUserProfileRepository()
        nutritionGoalRepository = FakeNutritionGoalRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLaunchViewModelNavigatesToWelcomeWhenNoProfile() = runTest(testDispatcher) {
        val viewModel = LaunchViewModel(userProfileRepository)
        advanceUntilIdle()

        assertEquals(LaunchDestination.Welcome, viewModel.destination.value)
    }

    @Test
    fun testLaunchViewModelNavigatesToHomeWhenProfileExists() = runTest(testDispatcher) {
        val profile = UserProfile(
            id = 1L,
            goal = GoalType.LOSE,
            units = UnitSystem.METRIC,
            age = 28,
            heightCm = 175.0,
            weightKg = 70.0,
            goalWeightKg = 65.0,
            activityLevel = ActivityLevel.MODERATE,
            dietTags = emptyList(),
            allergies = emptyList(),
            targetsSource = TargetBasis.RECOMMENDED,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        userProfileRepository.saveProfile(profile)

        val viewModel = LaunchViewModel(userProfileRepository)
        advanceUntilIdle()

        assertEquals(LaunchDestination.Home, viewModel.destination.value)
    }

    @Test
    fun testProfileViewModelObservesAndUpdatesProfile() = runTest(testDispatcher) {
        val profile = UserProfile(
            id = 1L,
            goal = GoalType.LOSE,
            units = UnitSystem.METRIC,
            age = 28,
            heightCm = 175.0,
            weightKg = 70.0,
            goalWeightKg = 65.0,
            activityLevel = ActivityLevel.MODERATE,
            dietTags = listOf("vegetarian"),
            allergies = listOf("peanuts"),
            targetsSource = TargetBasis.RECOMMENDED,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val goal = NutritionGoal(
            id = 1L,
            userId = 1L,
            calories = 1980,
            proteinG = 140.0,
            carbsG = 216.0,
            fatG = 62.0,
            waterLiters = 2.4,
            waterGlasses = 8,
            bmr = 1566.0,
            tdee = 2427.0,
            isCustom = false,
            updatedAt = 1000L
        )

        userProfileRepository.saveProfile(profile)
        userProfileRepository.setGoal(goal)
        nutritionGoalRepository.saveGoal(goal)

        val viewModel = ProfileViewModel(userProfileRepository, nutritionGoalRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ProfileUiState.Content)
        val content = viewModel.uiState.value as ProfileUiState.Content
        assertEquals(70.0, content.profile.weightKg, 0.0)

        // Toggle unit system
        viewModel.toggleUnitSystem()
        advanceUntilIdle()

        val updatedProfile = userProfileRepository.getProfile()
        assertNotNull(updatedProfile)
        assertEquals(UnitSystem.IMPERIAL, updatedProfile!!.units)
    }
}
