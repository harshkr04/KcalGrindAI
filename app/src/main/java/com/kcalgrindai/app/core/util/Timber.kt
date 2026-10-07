package com.kcalgrindai.app.core.util

import android.util.Log

/**
 * Lightweight internal Timber logging utility wrapping Android Log and CrashReporter.
 */
object Timber {
    fun e(t: Throwable?, message: String) {
        Log.e("KcalGrindAI", message, t)
        if (t != null) {
            KcalGrindAICrashReporter.recordException(t, mapOf("source" to "Timber.e"))
        }
    }

    fun w(t: Throwable?, message: String) {
        Log.w("KcalGrindAI", message, t)
    }

    fun d(message: String) {
        Log.d("KcalGrindAI", message)
    }

    fun i(message: String) {
        Log.i("KcalGrindAI", message)
    }
}
