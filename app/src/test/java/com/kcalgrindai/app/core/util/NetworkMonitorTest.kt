package com.kcalgrindai.app.core.util

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkMonitorTest {

    private class FakeNetworkMonitor(
        private var online: Boolean = true
    ) : NetworkMonitor {
        override val isOnline = flowOf(online)
        override fun isCurrentlyOnline(): Boolean = online

        fun setOnline(value: Boolean) {
            online = value
        }
    }

    @Test
    fun `network monitor reports online state when connected`() = runTest {
        val monitor = FakeNetworkMonitor(online = true)
        assertTrue(monitor.isCurrentlyOnline())
        assertTrue(monitor.isOnline.first())
    }

    @Test
    fun `network monitor reports offline state when disconnected`() = runTest {
        val monitor = FakeNetworkMonitor(online = false)
        assertFalse(monitor.isCurrentlyOnline())
        assertFalse(monitor.isOnline.first())
    }
}
