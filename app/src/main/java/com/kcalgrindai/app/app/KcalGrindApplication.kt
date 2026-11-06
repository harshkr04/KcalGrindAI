package com.kcalgrindai.app.app

import android.app.Application
import com.kcalgrindai.app.core.notification.NotificationHelper
import com.kcalgrindai.app.core.worker.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class KcalGrindApplication : Application() {

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        appScope.launch {
            try {
                reminderScheduler.syncRemindersWithSettings()
            } catch (_: Exception) {
                // Ignore during testing / early mock environments
            }
        }
    }
}
