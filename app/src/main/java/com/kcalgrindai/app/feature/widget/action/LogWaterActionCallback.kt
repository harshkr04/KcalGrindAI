package com.kcalgrindai.app.feature.widget.action

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.kcalgrindai.app.feature.widget.di.WidgetEntryPoint
import com.kcalgrindai.app.feature.widget.util.GlanceWidgetUpdater
import dagger.hilt.android.EntryPointAccessors

class LogWaterActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            val today = entryPoint.widgetStateMapper().getCurrentDateString()
            // Update Room first before widget refresh (Requirement 5)
            entryPoint.waterLogRepository().logWater(250, today)
            // Trigger Glance update
            GlanceWidgetUpdater(context.applicationContext).requestUpdate()
        } catch (e: Exception) {
            // Do not log health data or PII (Requirement 3)
            android.util.Log.e("LogWaterActionCallback", "Failed to log water from widget", e)
        }
    }
}
