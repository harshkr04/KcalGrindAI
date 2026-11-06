package com.kcalgrindai.app.data.health

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

sealed interface HealthConnectAvailability {
    data object InstalledAndAvailable : HealthConnectAvailability
    data object NotInstalled : HealthConnectAvailability
    data class NotSupported(val reason: String = "Health Connect is not supported on this device") : HealthConnectAvailability
}

sealed interface HealthConnectActivityState {
    data class Available(val stepsToday: Long, val activeCaloriesBurnedToday: Double = 0.0, val hasPermission: Boolean) : HealthConnectActivityState
    data object PermissionRequired : HealthConnectActivityState
    data class Unavailable(val reason: String) : HealthConnectActivityState
}

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val stepsPermission: String = HealthPermission.getReadPermission(StepsRecord::class)
    val activeCaloriesPermission: String = HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)
    val requiredPermissions: Set<String> = setOf(stepsPermission, activeCaloriesPermission)

    fun checkAvailability(): HealthConnectAvailability {
        return try {
            when (HealthConnectClient.getSdkStatus(context)) {
                HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.InstalledAndAvailable
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectAvailability.NotInstalled
                else -> HealthConnectAvailability.NotSupported()
            }
        } catch (e: Exception) {
            HealthConnectAvailability.NotSupported(e.message ?: "Health Connect unavailable")
        }
    }

    fun getHealthConnectClient(): HealthConnectClient? {
        return if (checkAvailability() == HealthConnectAvailability.InstalledAndAvailable) {
            try {
                HealthConnectClient.getOrCreate(context)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun createPermissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    suspend fun hasPermissions(): Boolean {
        val client = getHealthConnectClient() ?: return false
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            granted.containsAll(requiredPermissions)
        } catch (_: Exception) {
            false
        }
    }

    suspend fun readTodaySteps(): Long {
        val client = getHealthConnectClient() ?: return 0L
        if (!hasPermissions()) return 0L

        return try {
            val now = Instant.now()
            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    suspend fun readStepsInRange(start: Instant, end: Instant): Long {
        val client = getHealthConnectClient() ?: return 0L
        if (!hasPermissions()) return 0L

        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    suspend fun readTodayActiveCaloriesBurned(): Double {
        val client = getHealthConnectClient() ?: return 0.0
        if (!hasPermissions()) return 0.0

        return try {
            val now = Instant.now()
            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )
            response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0
        } catch (_: Exception) {
            0.0
        }
    }

    suspend fun getActivityState(): HealthConnectActivityState {
        return when (val availability = checkAvailability()) {
            is HealthConnectAvailability.InstalledAndAvailable -> {
                if (hasPermissions()) {
                    val steps = readTodaySteps()
                    val activeCalories = readTodayActiveCaloriesBurned()
                    HealthConnectActivityState.Available(
                        stepsToday = steps,
                        activeCaloriesBurnedToday = activeCalories,
                        hasPermission = true
                    )
                } else {
                    HealthConnectActivityState.PermissionRequired
                }
            }
            is HealthConnectAvailability.NotInstalled -> {
                HealthConnectActivityState.Unavailable("Health Connect is not installed")
            }
            is HealthConnectAvailability.NotSupported -> {
                HealthConnectActivityState.Unavailable(availability.reason)
            }
        }
    }
}
