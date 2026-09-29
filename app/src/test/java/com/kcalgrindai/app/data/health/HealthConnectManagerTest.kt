package com.kcalgrindai.app.data.health

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthConnectManagerTest {

    @Test
    fun `steps permission string is correct`() {
        val permission = HealthPermission.getReadPermission(StepsRecord::class)
        assertEquals("android.permission.health.READ_STEPS", permission)
    }

    @Test
    fun `health connect activity states represent all required states`() {
        val availableState = HealthConnectActivityState.Available(stepsToday = 4500L, hasPermission = true)
        assertEquals(4500L, availableState.stepsToday)
        assertTrue(availableState.hasPermission)

        val permissionNeeded = HealthConnectActivityState.PermissionRequired
        assertTrue(permissionNeeded is HealthConnectActivityState.PermissionRequired)

        val unavailable = HealthConnectActivityState.Unavailable("Health Connect is not supported on this device")
        assertEquals("Health Connect is not supported on this device", unavailable.reason)
    }
}
