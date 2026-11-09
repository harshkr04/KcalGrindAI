package com.kcalgrindai.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kcalgrindai.app.R
import com.kcalgrindai.app.app.MainActivity

object NotificationHelper {

    const val CHANNEL_ID_MEALS = "kcalgrind_meal_reminders"
    const val CHANNEL_ID_HYDRATION = "kcalgrind_hydration_reminders"

    const val NOTIFICATION_ID_MEAL = 1001
    const val NOTIFICATION_ID_HYDRATION = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // Meal reminders channel
            val mealChannel = NotificationChannel(
                CHANNEL_ID_MEALS,
                "Meal & Logging Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to log your breakfast, lunch, and dinner to stay consistent"
                enableLights(true)
                setShowBadge(true)
            }

            // Hydration reminders channel
            val hydrationChannel = NotificationChannel(
                CHANNEL_ID_HYDRATION,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic reminders during the day to keep yourself hydrated"
                enableLights(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(mealChannel)
            notificationManager.createNotificationChannel(hydrationChannel)
        }
    }

    fun showMealReminder(
        context: Context,
        mealName: String,
        message: String = "Don't forget to track your $mealName to keep your daily streak alive!"
    ) {
        val slot = when (mealName.lowercase().trim()) {
            "snacks" -> "snack"
            else -> mealName.lowercase().trim()
        }
        val destination = "diary?mealSlot=$slot"
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(MainActivity.EXTRA_DESTINATION, destination)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = androidx.core.app.TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                NOTIFICATION_ID_MEAL,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MEALS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time to log your $mealName 🥗")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_MEAL, notification)
        } catch (_: SecurityException) {
            // Android 13+ permission denied
        }
    }

    fun showHydrationReminder(
        context: Context,
        message: String = "Take a moment to drink a glass of water and hit your hydration target today! 💧"
    ) {
        val destination = "home"
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(MainActivity.EXTRA_DESTINATION, destination)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = androidx.core.app.TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                NOTIFICATION_ID_HYDRATION,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_HYDRATION)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Hydration Check 💧")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_HYDRATION, notification)
        } catch (_: SecurityException) {
            // Android 13+ permission denied
        }
    }
}
