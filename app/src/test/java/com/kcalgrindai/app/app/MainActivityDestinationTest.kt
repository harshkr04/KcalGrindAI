package com.kcalgrindai.app.app

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityDestinationTest {

    @Test
    fun testExtraDestinationExtractedInDebug() {
        val intent = Intent().apply {
            putExtra(MainActivity.EXTRA_DESTINATION, "profile")
        }

        // Debug mode allows any destination
        val destination = MainActivity.extractDestination(intent, isDebug = true)
        assertEquals("profile", destination)
    }

    @Test
    fun testAllowedDestinationsExtractedInRelease() {
        val allowed = listOf(
            "home",
            "diary",
            "diary?mealSlot=breakfast",
            "diary?mealSlot=lunch",
            "diary?mealSlot=dinner",
            "diary?mealSlot=snack",
            "diary?mealSlot=snacks",
            "diary?mealslot=breakfast",
            "diary?mealslot=snacks"
        )
        for (target in allowed) {
            val intent = Intent().apply {
                putExtra(MainActivity.EXTRA_DESTINATION, target)
            }
            val destination = MainActivity.extractDestination(intent, isDebug = false)
            assertEquals("Destination $target should be allowed in release", target, destination)
        }
    }

    @Test
    fun testSensitiveDestinationsRejectedInRelease() {
        val rejected = listOf(
            "profile",
            "auth",
            "subscription",
            "permission_setup",
            "camera_scanner_redesign",
            "diary?mealSlot=invalidSlot",
            "some_arbitrary_route",
            "javascript:void(0)"
        )
        for (target in rejected) {
            val intent = Intent().apply {
                putExtra(MainActivity.EXTRA_DESTINATION, target)
            }
            val destination = MainActivity.extractDestination(intent, isDebug = false)
            assertNull("Destination $target must be rejected in release mode", destination)
        }
    }

    @Test
    fun testNullOrEmptyIntentReturnsNull() {
        assertNull(MainActivity.extractDestination(null, isDebug = true))
        assertNull(MainActivity.extractDestination(null, isDebug = false))

        val emptyIntent = Intent()
        assertNull(MainActivity.extractDestination(emptyIntent, isDebug = true))
        assertNull(MainActivity.extractDestination(emptyIntent, isDebug = false))

        val blankIntent = Intent().apply {
            putExtra(MainActivity.EXTRA_DESTINATION, "   ")
        }
        assertNull(MainActivity.extractDestination(blankIntent, isDebug = true))
        assertNull(MainActivity.extractDestination(blankIntent, isDebug = false))
    }

    @Test
    fun testExtraTriggerReminderGatedInRelease() {
        val activity = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).get()

        val mealIntent = Intent().apply {
            putExtra(MainActivity.EXTRA_TRIGGER_REMINDER, "meal")
        }
        val waterIntent = Intent().apply {
            putExtra(MainActivity.EXTRA_TRIGGER_REMINDER, "water")
        }

        // In release mode (isDebug = false), debug triggers are strictly unreachable and ignored
        val mealHandledInRelease = activity.handleTriggerReminder(mealIntent, isDebug = false)
        org.junit.Assert.assertFalse("Meal debug trigger extra must be disabled in release", mealHandledInRelease)

        val waterHandledInRelease = activity.handleTriggerReminder(waterIntent, isDebug = false)
        org.junit.Assert.assertFalse("Water debug trigger extra must be disabled in release", waterHandledInRelease)
    }
}
