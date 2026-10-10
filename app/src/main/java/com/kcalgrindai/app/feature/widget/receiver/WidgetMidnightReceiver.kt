package com.kcalgrindai.app.feature.widget.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kcalgrindai.app.feature.widget.util.GlanceWidgetUpdater
import com.kcalgrindai.app.feature.widget.util.WidgetMidnightScheduler
import java.time.Clock

class WidgetMidnightReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == WidgetMidnightScheduler.ACTION_MIDNIGHT_UPDATE ||
            action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_BOOT_COMPLETED
        ) {
            try {
                // 1. Refresh widget content for new date/rollover
                GlanceWidgetUpdater(context.applicationContext).requestUpdate()

                // 2. Schedule next midnight update
                val scheduler = WidgetMidnightScheduler(Clock.systemDefaultZone())
                scheduler.scheduleMidnightAlarm(context.applicationContext)
            } catch (e: Exception) {
                // Never log health data or PII (Requirement 3)
                android.util.Log.e("WidgetMidnightReceiver", "Error processing date/time change broadcast", e)
            }
        }
    }
}
