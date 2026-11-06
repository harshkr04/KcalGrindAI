package com.kcalgrindai.app.core.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(context)
    }

    suspend fun syncRemindersWithSettings() {
        val notificationsOn = userPreferencesRepository.notificationsEnabled.first()
        val mealOn = userPreferencesRepository.mealRemindersEnabled.first()
        val hydrationOn = userPreferencesRepository.hydrationRemindersEnabled.first()

        if (!notificationsOn) {
            cancelAllReminders()
            return
        }

        if (mealOn) {
            scheduleMealReminders()
        } else {
            cancelMealReminders()
        }

        if (hydrationOn) {
            scheduleHydrationReminders()
        } else {
            cancelHydrationReminders()
        }
    }

    fun scheduleMealReminders() {
        // Inexact 1-hour periodic work to check meal windows and logged status
        val mealWorkRequest = PeriodicWorkRequestBuilder<MealReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME_MEAL_REMINDER,
            ExistingPeriodicWorkPolicy.KEEP,
            mealWorkRequest
        )
    }

    fun scheduleHydrationReminders() {
        // Inexact 2-hour periodic work to prompt water drinking during waking hours
        val hydrationWorkRequest = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            repeatInterval = 2,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME_HYDRATION_REMINDER,
            ExistingPeriodicWorkPolicy.KEEP,
            hydrationWorkRequest
        )
    }

    fun cancelMealReminders() {
        workManager.cancelUniqueWork(WORK_NAME_MEAL_REMINDER)
    }

    fun cancelHydrationReminders() {
        workManager.cancelUniqueWork(WORK_NAME_HYDRATION_REMINDER)
    }

    fun cancelAllReminders() {
        workManager.cancelUniqueWork(WORK_NAME_MEAL_REMINDER)
        workManager.cancelUniqueWork(WORK_NAME_HYDRATION_REMINDER)
    }

    companion object {
        const val WORK_NAME_MEAL_REMINDER = "kcalgrind_meal_reminder_work"
        const val WORK_NAME_HYDRATION_REMINDER = "kcalgrind_hydration_reminder_work"
    }
}
