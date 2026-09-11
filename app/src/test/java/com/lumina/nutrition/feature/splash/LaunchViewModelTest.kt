package com.lumina.nutrition.feature.splash

import com.lumina.nutrition.domain.model.ActivityLevel
import com.lumina.nutrition.domain.model.GoalType
import com.lumina.nutrition.domain.model.TargetBasis
import com.lumina.nutrition.domain.model.UnitSystem
import com.lumina.nutrition.domain.model.UserProfile
import com.lumina.nutrition.fakes.FakeUserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userProfileRepository: FakeUserProfileRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userProfileRepository = FakeUserProfileRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLaunchDestinationResolvesToWelcomeWhenNoProfile() = runTest(testDispatcher) {
        val viewModel = LaunchViewModel(userProfileRepository)
        advanceUntilIdle()

        assertEquals(LaunchDestination.Welcome, viewModel.destination.value)
    }

    @Test
    fun testLaunchDestinationResolvesToHomeWhenProfileExists() = runTest(testDispatcher) {
        val profile = UserProfile(
            id = 1L,
            goal = GoalType.MAINTAIN,
            units = UnitSystem.METRIC,
            age = 25,
            heightCm = 175.0,
            weightKg = 70.0,
            goalWeightKg = 70.0,
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
}
