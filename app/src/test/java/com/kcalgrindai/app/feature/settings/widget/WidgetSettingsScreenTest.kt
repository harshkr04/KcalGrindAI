package com.kcalgrindai.app.feature.settings.widget

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.kcalgrindai.app.fakes.FakeUserPreferencesRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WidgetSettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var fakePrefsRepo: FakeUserPreferencesRepository
    private lateinit var viewModel: WidgetSettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        fakePrefsRepo = FakeUserPreferencesRepository(initialPrivacy = false)
        viewModel = WidgetSettingsViewModel(
            context = context,
            userPreferencesRepository = fakePrefsRepo
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSettingsScreenDisplaysExpectedElements() {
        composeTestRule.setContent {
            WidgetSettingsScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Home Screen Widget").assertExists()
        composeTestRule.onNodeWithText("Available Widget Sizes").assertExists()
        composeTestRule.onNodeWithText("4×2 Detailed").assertExists()
        composeTestRule.onNodeWithText("2×2 Quick Widget").assertExists()
        composeTestRule.onNodeWithText("Hide numbers on widget").assertExists()
        composeTestRule.onNodeWithText("Manual Setup Instructions").assertExists()
        composeTestRule.onNodeWithText("Long-press an empty area of the home screen.").assertExists()
        composeTestRule.onNodeWithText("Open Widgets.").assertExists()
        composeTestRule.onNodeWithText("Find KcalGrind.").assertExists()
        composeTestRule.onNodeWithText("Drag the widget onto the home screen.").assertExists()
    }

    @Test
    fun testTabSwitchingUpdatesPreview() = runTest {
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        composeTestRule.setContent {
            WidgetSettingsScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }

        // Initially 4x2 Detailed
        composeTestRule.onNodeWithText("680 kcal left").assertExists()

        // Click 2x2 tab
        composeTestRule.onNodeWithText("2×2 Quick Widget").performClick()
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        // 2x2 preview should now be displayed
        composeTestRule.onNodeWithText("680 left").assertExists()
        composeTestRule.onNodeWithText("Sproutling is energized").assertExists()

        collectJob.cancel()
    }

    @Test
    fun testPrivacyMaskingUI() {
        // Test unmasked state
        composeTestRule.setContent {
            WidgetDetailed4x2Preview(isPrivacyEnabled = false)
        }
        composeTestRule.onNodeWithText("680 kcal left").assertExists()
        composeTestRule.onNodeWithText("1,420 / 2,100").assertExists()
        composeTestRule.onNodeWithText("Budget: 2,100 kcal").assertExists()
        composeTestRule.onNodeWithText("C: 180g").assertExists()
        composeTestRule.onNodeWithText("P: 120g").assertExists()
        composeTestRule.onNodeWithText("F: 55g").assertExists()
    }

    @Test
    fun testPrivacyMaskedStateHidesNumbersAndIndicators() {
        // Test masked state (privacy enabled)
        composeTestRule.setContent {
            WidgetDetailed4x2Preview(isPrivacyEnabled = true)
        }
        composeTestRule.onNodeWithText("•••• kcal left").assertExists()
        composeTestRule.onNodeWithText("Secure Mode").assertExists()
        composeTestRule.onNodeWithText("•••• / ••••").assertExists()
        composeTestRule.onNodeWithText("C: •••g").assertExists()
        composeTestRule.onNodeWithText("P: •••g").assertExists()
        composeTestRule.onNodeWithText("F: ••g").assertExists()

        // Ensure sensitive numbers do NOT exist
        composeTestRule.onNodeWithText("680 kcal left").assertDoesNotExist()
        composeTestRule.onNodeWithText("1,420 / 2,100").assertDoesNotExist()
        composeTestRule.onNodeWithText("Budget: 2,100 kcal").assertDoesNotExist()
        composeTestRule.onNodeWithText("C: 180g").assertDoesNotExist()
        composeTestRule.onNodeWithText("P: 120g").assertDoesNotExist()
        composeTestRule.onNodeWithText("F: 55g").assertDoesNotExist()
    }

    @Test
    fun testPrivacyCardDisplaysCorrectTextForModes() {
        val isPrivacy = androidx.compose.runtime.mutableStateOf(false)
        composeTestRule.setContent {
            WidgetPrivacyCard(
                isPrivacyEnabled = isPrivacy.value,
                onTogglePrivacy = { isPrivacy.value = it }
            )
        }
        composeTestRule.onNodeWithText("680 kcal visible").assertExists()

        isPrivacy.value = true
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("•••• kcal masked").assertExists()
    }

    @Test
    fun testPrivacyCardSwitchToggle() {
        var toggledValue = false
        composeTestRule.setContent {
            WidgetPrivacyCard(
                isPrivacyEnabled = false,
                onTogglePrivacy = { toggledValue = it }
            )
        }
        composeTestRule.onNodeWithContentDescription("Toggle hiding numbers on widget").performClick()
        assertTrue("Switch click should trigger onTogglePrivacy with true", toggledValue)
    }

    @Test
    fun testPrivacyToggleUpdatesViewModelAndPersistence() = runTest {
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertFalse("Initial privacy should be false", viewModel.uiState.value.isPrivacyEnabled)

        viewModel.togglePrivacy(true)
        advanceUntilIdle()

        assertTrue("Privacy state should be enabled in ViewModel", viewModel.uiState.value.isPrivacyEnabled)
        assertTrue("Repository must persist privacy setting as true", fakePrefsRepo.widgetPrivacyEnabled.first())

        viewModel.togglePrivacy(false)
        advanceUntilIdle()

        assertFalse("Privacy state should be disabled", viewModel.uiState.value.isPrivacyEnabled)
        assertFalse("Repository must persist privacy setting as false", fakePrefsRepo.widgetPrivacyEnabled.first())

        collectJob.cancel()
    }

    @Test
    fun testBackButtonClickTriggersNavigation() {
        var navigatedBack = false

        composeTestRule.setContent {
            WidgetSettingsScreen(
                onNavigateBack = { navigatedBack = true },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithContentDescription("Go back").performClick()

        assertTrue("Navigation back should occur", navigatedBack)
    }
}
