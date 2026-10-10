package com.kcalgrindai.app.feature.widget.action

import android.content.Context
import android.content.Intent
import com.kcalgrindai.app.app.MainActivity
import com.kcalgrindai.app.core.navigation.KcalGrindRoute

object WidgetActions {
    const val EXTRA_DESTINATION = "EXTRA_DESTINATION"

    fun openAppIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DESTINATION, KcalGrindRoute.Home.route)
        }
    }

    fun addMealIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DESTINATION, KcalGrindRoute.AddFoodSheet.route)
        }
    }

    fun cameraScanIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DESTINATION, KcalGrindRoute.CameraScannerRedesign.route)
        }
    }

    fun goalSetupIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DESTINATION, KcalGrindRoute.GoalSetting.route)
        }
    }
}
