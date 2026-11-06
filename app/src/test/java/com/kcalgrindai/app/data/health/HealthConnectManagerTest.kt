package com.kcalgrindai.app.data.health

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.test.core.app.ApplicationProvider
import com.kcalgrindai.app.domain.usecase.NutritionCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HealthConnectManagerTest {

    @Test
    fun stepsPermissionString_isCorrect() {
        val permission = HealthPermission.getReadPermission(StepsRecord::class)
        assertEquals("android.permission.health.READ_STEPS", permission)
    }

    @Test
    fun activeCaloriesPermissionString_isCorrect() {
        val permission = HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)
        assertEquals("android.permission.health.READ_ACTIVE_CALORIES_BURNED", permission)
    }

    @Test
    fun requiredPermissions_includesBothStepsAndActiveCalories() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val manager = HealthConnectManager(context)

        assertTrue(manager.requiredPermissions.contains(manager.stepsPermission))
        assertTrue(manager.requiredPermissions.contains(manager.activeCaloriesPermission))
        assertEquals(2, manager.requiredPermissions.size)
    }

    @Test
    fun healthConnectActivityState_representsAllRequiredStatesWithActiveCalories() {
        val availableState = HealthConnectActivityState.Available(
            stepsToday = 7500L,
            activeCaloriesBurnedToday = 420.5,
            hasPermission = true
        )
        assertEquals(7500L, availableState.stepsToday)
        assertEquals(420.5, availableState.activeCaloriesBurnedToday, 0.01)
        assertTrue(availableState.hasPermission)

        val permissionNeeded = HealthConnectActivityState.PermissionRequired
        assertTrue(permissionNeeded is HealthConnectActivityState.PermissionRequired)

        val unavailable = HealthConnectActivityState.Unavailable("Health Connect is not supported on this device")
        assertEquals("Health Connect is not supported on this device", unavailable.reason)
    }

    @Test
    fun calculateNetCalories_deductOff_ignoresActiveCaloriesBurned() {
        // Target: 2000, Consumed: 1500, Active Burned: 400, Deduct: false (Default)
        val remaining = NutritionCalculator.calculateNetCalories(
            targetCalories = 2000,
            consumedCalories = 1500.0,
            activeCaloriesBurned = 400.0,
            deductActiveCalories = false
        )
        // 2000 - 1500 = 500 remaining
        assertEquals(500, remaining)
    }

    @Test
    fun calculateNetCalories_deductOn_subtractsActiveCaloriesFromIntake() {
        // Target: 2000, Consumed: 1500, Active Burned: 400, Deduct: true
        val remaining = NutritionCalculator.calculateNetCalories(
            targetCalories = 2000,
            consumedCalories = 1500.0,
            activeCaloriesBurned = 400.0,
            deductActiveCalories = true
        )
        // Net consumed: 1500 - 400 = 1100. Remaining: 2000 - 1100 = 900
        assertEquals(900, remaining)
    }

    @Test
    fun calculateNetCalories_neverNegative() {
        val remaining = NutritionCalculator.calculateNetCalories(
            targetCalories = 1500,
            consumedCalories = 2500.0,
            activeCaloriesBurned = 0.0,
            deductActiveCalories = false
        )
        assertEquals(0, remaining)
    }

    /**
     * NOTE FOR LAUNCH VERIFICATION:
     * In unit test environments and standard Android SDK emulators without Google Play Services /
     * Health Connect APK pre-installed, `HealthConnectClient.getSdkStatus()` returns
     * `SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED` or `SDK_UNAVAILABLE`.
     * Live end-to-end sync of real steps and active calories requires a physical test device
     * running Android 14+ or an Android 13 device with the Health Connect app installed from Play Store.
     */
    @Test
    fun checkAvailability_onStandardRobolectric_returnsSafeNonCrashingStatus() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val manager = HealthConnectManager(context)
        val availability = manager.checkAvailability()
        // Must not crash; returns NotInstalled or NotSupported cleanly
        assertTrue(
            availability is HealthConnectAvailability.NotInstalled ||
            availability is HealthConnectAvailability.NotSupported ||
            availability is HealthConnectAvailability.InstalledAndAvailable
        )
    }
}
