package com.kcalgrindai.app.feature.widget.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kcalgrindai.app.feature.widget.receiver.WidgetMidnightReceiver
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetMidnightScheduler @Inject constructor(
    private val clock: Clock
) {

    fun calculateNextMidnightEpochMillis(zoneId: ZoneId = clock.zone ?: ZoneId.systemDefault()): Long {
        val tomorrow = LocalDate.now(clock.withZone(zoneId)).plusDays(1)
        return tomorrow.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun scheduleMidnightAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, WidgetMidnightReceiver::class.java).apply {
            action = ACTION_MIDNIGHT_UPDATE
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_MIDNIGHT, intent, flags)

        val midnightMillis = calculateNextMidnightEpochMillis()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Check exact-alarm permission/policy on Android 12+ (API 31+)
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, midnightMillis, pendingIntent)
                } else {
                    // Safe fallback when exact alarm permission is restricted
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, midnightMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, midnightMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC, midnightMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            // Appropriate fallback if exact alarm throws SecurityException (Requirement 4)
            alarmManager.set(AlarmManager.RTC, midnightMillis, pendingIntent)
        } catch (e: Exception) {
            android.util.Log.e("WidgetMidnightScheduler", "Failed to schedule midnight alarm", e)
        }
    }

    companion object {
        const val ACTION_MIDNIGHT_UPDATE = "com.kcalgrindai.app.widget.ACTION_MIDNIGHT_UPDATE"
        const val REQUEST_CODE_MIDNIGHT = 1001
    }
}
