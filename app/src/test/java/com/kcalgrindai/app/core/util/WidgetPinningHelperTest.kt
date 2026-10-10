package com.kcalgrindai.app.core.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WidgetPinningHelperTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testInitialWidgetStatusUsingAppWidgetIds() {
        val count = WidgetPinningHelper.getInstalledWidgetCount(context)
        assertEquals(0, count)

        val isInstalled = WidgetPinningHelper.isWidgetInstalled(context)
        assertFalse("Widget should not be installed initially in test environment", isInstalled)
    }

    @Test
    fun testPinningSupportCheckDoesNotCrash() {
        val isSupported = WidgetPinningHelper.isPinningSupported(context)
        assertNotNull(isSupported)
    }
}
