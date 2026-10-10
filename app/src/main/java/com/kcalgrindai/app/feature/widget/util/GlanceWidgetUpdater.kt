package com.kcalgrindai.app.feature.widget.util

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.kcalgrindai.app.core.util.WidgetUpdater
import com.kcalgrindai.app.feature.widget.KcalGrindAppWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlanceWidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) : WidgetUpdater {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun requestUpdate() {
        scope.launch {
            try {
                KcalGrindAppWidget().updateAll(context)
            } catch (e: Exception) {
                // Never log health data or PII (Requirement 3)
                android.util.Log.e("GlanceWidgetUpdater", "Failed to update widgets", e)
            }
        }
    }

    override fun clearWidgetDataOnSignOut() {
        scope.launch {
            try {
                KcalGrindAppWidget().updateAll(context)
            } catch (e: Exception) {
                // Never log health data or PII (Requirement 3)
                android.util.Log.e("GlanceWidgetUpdater", "Failed to clear widgets on sign-out", e)
            }
        }
    }
}
