package com.kcalgrindai.app.feature.home

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.kcalgrindai.app.data.health.HealthConnectManager
import com.kcalgrindai.app.fakes.FakeMealLogRepository
import com.kcalgrindai.app.fakes.FakeNutritionGoalRepository
import com.kcalgrindai.app.fakes.FakeUserPreferencesRepository
import com.kcalgrindai.app.fakes.FakeUserProfileRepository
import com.kcalgrindai.app.fakes.FakeWaterLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class HomeWidgetPromoCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var mealLogRepository: FakeMealLogRepository
    private lateinit var nutritionGoalRepository: FakeNutritionGoalRepository
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var waterLogRepository: FakeWaterLogRepository
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var userPreferencesRepository: FakeUserPreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        mealLogRepository = FakeMealLogRepository()
        nutritionGoalRepository = FakeNutritionGoalRepository()
        userProfileRepository = FakeUserProfileRepository()
        waterLogRepository = FakeWaterLogRepository()
        healthConnectManager = HealthConnectManager(context)
        userPreferencesRepository = FakeUserPreferencesRepository(initialPromoDismissed = false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testPromoCardDisplaysCorrectContent() {
        composeTestRule.setContent {
            HomeWidgetPromoCard(
                onAddWidget = {},
                onOpenSettings = {},
                onDismiss = {}
            )
        }

        composeTestRule.onNodeWithText("Bring Sproutling to your home screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Glance at your calories and track water without opening the app.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add widget").assertIsDisplayed()
        composeTestRule.onNodeWithText("Widget settings").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Dismiss banner").assertIsDisplayed()
    }

    @Test
    fun testPromoCardInteractions() {
        var addWidgetClicked = false
        var openSettingsClicked = false
        var dismissClicked = false

        composeTestRule.setContent {
            HomeWidgetPromoCard(
                onAddWidget = { addWidgetClicked = true },
                onOpenSettings = { openSettingsClicked = true },
                onDismiss = { dismissClicked = true }
            )
        }

        composeTestRule.onNodeWithText("Add widget").performClick()
        assertTrue("Add widget should be called", addWidgetClicked)

        composeTestRule.onNodeWithText("Widget settings").performClick()
        assertTrue("Open settings should be called", openSettingsClicked)

        composeTestRule.onNodeWithContentDescription("Dismiss banner").performClick()
        assertTrue("Dismiss should be called", dismissClicked)
    }

    @Test
    fun testPromoDismissalPersistenceInHomeViewModel() = runTest {
        val viewModel = HomeViewModel(
            mealLogRepository = mealLogRepository,
            nutritionGoalRepository = nutritionGoalRepository,
            userProfileRepository = userProfileRepository,
            waterLogRepository = waterLogRepository,
            healthConnectManager = healthConnectManager,
            userPreferencesRepository = userPreferencesRepository,
            context = context
        )

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Initially promo card is shown
        assertTrue("Promo card should be shown initially", viewModel.uiState.value.showWidgetPromoCard)

        // Dismiss promo card
        viewModel.dismissWidgetPromo()
        advanceUntilIdle()

        // Assert repository persisted dismissal
        val persisted = userPreferencesRepository.widgetPromoDismissed.first()
        assertTrue("Dismissal must be persisted in repository", persisted)
        assertFalse("Promo card should be hidden after dismissal", viewModel.uiState.value.showWidgetPromoCard)

        collectJob.cancel()

        // Create fresh ViewModel instance to verify persistence across app launches
        val freshViewModel = HomeViewModel(
            mealLogRepository = mealLogRepository,
            nutritionGoalRepository = nutritionGoalRepository,
            userProfileRepository = userProfileRepository,
            waterLogRepository = waterLogRepository,
            healthConnectManager = healthConnectManager,
            userPreferencesRepository = userPreferencesRepository,
            context = context
        )

        val freshCollectJob = launch { freshViewModel.uiState.collect {} }
        advanceUntilIdle()

        assertFalse(
            "Promo card must remain dismissed across app restarts",
            freshViewModel.uiState.value.showWidgetPromoCard
        )

        freshCollectJob.cancel()
    }
}
