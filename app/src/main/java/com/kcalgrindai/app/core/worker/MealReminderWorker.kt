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
import com.kcalgrindai.app.data.local.dao.MealLogDao
import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime

class MealReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    var overrideMealLogDao: MealLogDao? = null
    var overrideUserPreferencesRepository: UserPreferencesRepository? = null
    var overrideCurrentTime: LocalTime? = null
    var overrideNotificationSender: ((Context, String) -> Unit)? = null

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
        val (mealLogDao, userPrefs) = try {
            val ep = EntryPointAccessors.fromApplication(
                applicationContext,
                ReminderWorkerEntryPoint::class.java
            )
            Pair(
                overrideMealLogDao ?: ep.mealLogDao(),
                overrideUserPreferencesRepository ?: ep.userPreferencesRepository()
            )
        } catch (_: Exception) {
            Pair(
                overrideMealLogDao ?: return Result.success(),
                overrideUserPreferencesRepository ?: return Result.success()
            )
        }

        // 3. User Preferences Check
        val notificationsOn = userPrefs.notificationsEnabled.first()
        val mealsOn = userPrefs.mealRemindersEnabled.first()
        if (!notificationsOn || !mealsOn) {
            return Result.success()
        }

        // 4. Determine Meal Window
        val now = overrideCurrentTime ?: LocalTime.now()
        val minuteOfDay = now.hour * 60 + now.minute

        val (targetMealType, mealDisplayName) = when {
            // Breakfast window: 07:00 to 10:30 (420 to 630 mins)
            minuteOfDay in 420..630 -> Pair("BREAKFAST", "Breakfast")
            // Lunch window: 12:00 to 15:00 (720 to 900 mins)
            minuteOfDay in 720..900 -> Pair("LUNCH", "Lunch")
            // Dinner window: 18:30 to 21:30 (1110 to 1290 mins)
            minuteOfDay in 1110..1290 -> Pair("DINNER", "Dinner")
            else -> return Result.success() // Outside typical meal reminder windows
        }

        // 5. Check if already notified for this meal type today (at most 1 notification per meal type per day)
        val todayStr = LocalDate.now().toString()
        val lastNotifiedDate = userPrefs.getLastMealReminderDate(targetMealType)
        if (lastNotifiedDate == todayStr) {
            return Result.success()
        }

        // 6. Skip if this meal was already logged today
        val todayMeals = mealLogDao.getMealLogsByDate(todayStr)
        val alreadyLogged = todayMeals.any {
            it.mealType.equals(targetMealType, ignoreCase = true)
        }
        if (alreadyLogged) {
            return Result.success()
        }

        // 7. Send meal reminder notification
        if (overrideNotificationSender != null) {
            overrideNotificationSender?.invoke(applicationContext, mealDisplayName)
        } else {
            NotificationHelper.showMealReminder(applicationContext, mealDisplayName)
        }

        // 8. Persist last-notified marker to prevent repeated notifications on subsequent worker runs today
        userPrefs.setLastMealReminderDate(targetMealType, todayStr)

        return Result.success()
    }
}
