package com.kcalgrindai.app.core.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kcalgrindai.app.core.notification.NotificationHelper
import com.kcalgrindai.app.core.notification.ReminderWorkerEntryPoint
import com.kcalgrindai.app.data.local.dao.WaterLogDao
import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime

class HydrationReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    var overrideWaterLogDao: WaterLogDao? = null
    var overrideUserPreferencesRepository: UserPreferencesRepository? = null
    var overrideCurrentTime: LocalTime? = null
    var overrideCurrentMillis: Long? = null
    var overrideNotificationSender: ((Context) -> Unit)? = null

    override suspend fun doWork(): Result {
        // 1. Android 13+ Notification Permission Check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                return Result.success()
            }
        }

        // 2. Resolve dependencies
        val (waterLogDao, userPrefs) = try {
            val ep = EntryPointAccessors.fromApplication(
                applicationContext,
                ReminderWorkerEntryPoint::class.java
            )
            Pair(
                overrideWaterLogDao ?: ep.waterLogDao(),
                overrideUserPreferencesRepository ?: ep.userPreferencesRepository()
            )
        } catch (_: Exception) {
            Pair(
                overrideWaterLogDao ?: return Result.success(),
                overrideUserPreferencesRepository ?: return Result.success()
            )
        }

        // 3. User Preferences Check
        val notificationsOn = userPrefs.notificationsEnabled.first()
        val hydrationOn = userPrefs.hydrationRemindersEnabled.first()
        if (!notificationsOn || !hydrationOn) {
            return Result.success()
        }

        // 4. Time Window Check (Waking hours: 08:00 to 22:00)
        val nowTime = overrideCurrentTime ?: LocalTime.now()
        val hour = nowTime.hour
        if (hour < 8 || hour >= 22) {
            return Result.success()
        }

        // 5. Skip if water was recently logged (within last 2 hours)
        val todayStr = LocalDate.now().toString()
        val todayLogs = waterLogDao.getWaterLogsByDate(todayStr)
        val nowMillis = overrideCurrentMillis ?: System.currentTimeMillis()
        val twoHoursAgoMillis = nowMillis - (2 * 60 * 60 * 1000L)

        val recentlyLogged = todayLogs.any { it.loggedAt >= twoHoursAgoMillis }
        if (recentlyLogged) {
            return Result.success()
        }

        // 6. Send reminder
        if (overrideNotificationSender != null) {
            overrideNotificationSender?.invoke(applicationContext)
        } else {
            NotificationHelper.showHydrationReminder(applicationContext)
        }

        return Result.success()
    }
}
