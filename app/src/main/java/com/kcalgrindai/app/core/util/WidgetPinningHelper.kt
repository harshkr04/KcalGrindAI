package com.kcalgrindai.app.core.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kcalgrindai.app.feature.widget.KcalGrindAppWidgetReceiver

object WidgetPinningHelper {

    fun isWidgetInstalled(context: Context): Boolean {
        return getInstalledWidgetCount(context) > 0
    }

    fun getInstalledWidgetCount(context: Context): Int {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return 0
        val componentName = ComponentName(context, KcalGrindAppWidgetReceiver::class.java)
        return try {
            appWidgetManager.getAppWidgetIds(componentName)?.size ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun isPinningSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return false
        return try {
            appWidgetManager.isRequestPinAppWidgetSupported
        } catch (e: Exception) {
            false
        }
    }

    fun requestPinWidget(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return false
        if (!appWidgetManager.isRequestPinAppWidgetSupported) return false

        val componentName = ComponentName(context, KcalGrindAppWidgetReceiver::class.java)
        val successCallback = try {
            val intent = Intent(context, KcalGrindAppWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } catch (e: Exception) {
            null
        }

        return try {
            appWidgetManager.requestPinAppWidget(componentName, null, successCallback)
        } catch (e: Exception) {
            false
        }
    }
}
