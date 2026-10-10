package com.kcalgrindai.app.feature.onboarding.widget

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AddWidgetOnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testOnboardingElementsDisplayed() {
        composeTestRule.setContent {
            AddWidgetOnboardingScreen(
                onNavigateNext = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Meet your daily companion").assertExists()
        composeTestRule.onNodeWithText("Keep Sproutling and your daily nutrition in sight right from your home screen.").assertExists()
        composeTestRule.onNodeWithText("Add to home screen").assertExists()
        composeTestRule.onNodeWithText("Maybe later").assertExists()
        composeTestRule.onNodeWithText("Skip").assertExists()
        composeTestRule.onNodeWithText("1-Tap Meal AI Scan").assertExists()
        composeTestRule.onNodeWithText("Instant Glance sync").assertExists()
    }

    @Test
    fun testAddToHomeScreenClickTriggersPinAndNavigateNext() {
        var pinRequested = false
        var navigatedNext = false

        composeTestRule.setContent {
            AddWidgetOnboardingScreen(
                onNavigateNext = { navigatedNext = true },
                onNavigateBack = {},
                onPinWidget = { pinRequested = true }
            )
        }

        composeTestRule.onNodeWithText("Add to home screen").performClick()

        assertTrue("Pinning should be requested", pinRequested)
        assertTrue("Navigation to next should occur", navigatedNext)
    }

    @Test
    fun testMaybeLaterClickTriggersNavigateNextWithoutPinning() {
        var pinRequested = false
        var navigatedNext = false

        composeTestRule.setContent {
            AddWidgetOnboardingScreen(
                onNavigateNext = { navigatedNext = true },
                onNavigateBack = {},
                onPinWidget = { pinRequested = true }
            )
        }

        composeTestRule.onNodeWithText("Maybe later").performClick()

        assertTrue("Navigation to next should occur", navigatedNext)
        assertTrue("Pinning should NOT be requested", !pinRequested)
    }

    @Test
    fun testBackButtonClickTriggersNavigateBack() {
        var navigatedBack = false

        composeTestRule.setContent {
            AddWidgetOnboardingScreen(
                onNavigateNext = {},
                onNavigateBack = { navigatedBack = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Go back").performClick()

        assertTrue("Navigation back should occur", navigatedBack)
    }
}
