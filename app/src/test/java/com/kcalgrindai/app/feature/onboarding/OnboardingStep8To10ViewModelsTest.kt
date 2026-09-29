package com.kcalgrindai.app.feature.onboarding

import com.kcalgrindai.app.domain.model.GoalType
import com.kcalgrindai.app.domain.usecase.CalculateNutritionGoalUseCase
import com.kcalgrindai.app.fakes.FakeNutritionGoalRepository
import com.kcalgrindai.app.fakes.FakeUserProfileRepository
import com.kcalgrindai.app.feature.onboarding.goalsetting.GoalSettingViewModel
import com.kcalgrindai.app.feature.onboarding.permissions.PermissionSetupViewModel
import com.kcalgrindai.app.feature.onboarding.state.OnboardingSessionManager
import com.kcalgrindai.app.feature.onboarding.target.CalorieTargetUiState
import com.kcalgrindai.app.feature.onboarding.target.CalorieTargetViewModel
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
class OnboardingStep8To10ViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var sessionManager: OnboardingSessionManager
    private lateinit var calculateUseCase: CalculateNutritionGoalUseCase
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var nutritionGoalRepository: FakeNutritionGoalRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sessionManager = OnboardingSessionManager()
        calculateUseCase = CalculateNutritionGoalUseCase()
        userProfileRepository = FakeUserProfileRepository()
        nutritionGoalRepository = FakeNutritionGoalRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testCalorieTargetViewModelComputesTargets() = runTest(testDispatcher) {
        val viewModel = CalorieTargetViewModel(calculateUseCase, sessionManager)
        assertTrue(viewModel.uiState.value is CalorieTargetUiState.Loading)

        advanceUntilIdle()

        val state = viewModel.uiState.value as CalorieTargetUiState.Success
        assertEquals(1990, state.calories)
        assertEquals(126.0, state.proteinG, 0.0)
        assertNotNull(sessionManager.getSnapshot().customCalories)
    }

    @Test
    fun testGoalSettingViewModelSliderAdjustmentAndTolerance() = runTest(testDispatcher) {
        sessionManager.updateCustomMacros(2000, 150.0, 200.0, 66.0)
        val viewModel = GoalSettingViewModel(sessionManager)

        assertTrue(viewModel.uiState.value.withinTolerance)

        // Drastically reduce carbs -> triggers out of tolerance
        viewModel.onCarbsChanged(50.0)
        assertFalse(viewModel.uiState.value.withinTolerance)

        viewModel.onConfirm()
        assertEquals(50.0, sessionManager.getSnapshot().customCarbsG)
    }

    @Test
    fun testPermissionSetupViewModelPersistsToRoomOnComplete() = runTest(testDispatcher) {
        val viewModel = PermissionSetupViewModel(
            sessionManager = sessionManager,
            userProfileRepository = userProfileRepository
        )

        viewModel.updateCameraPermission(true)
        viewModel.updateMicPermission(true)
        assertTrue(viewModel.uiState.value.cameraGranted)
        assertTrue(viewModel.uiState.value.micGranted)

        var onSuccessCalled = false
        viewModel.completeOnboarding {
            onSuccessCalled = true
        }

        advanceUntilIdle()

        assertTrue(onSuccessCalled)
        assertNotNull(userProfileRepository.getProfile())
        assertEquals(GoalType.LOSE, userProfileRepository.getProfile()!!.goal)
    }
}
