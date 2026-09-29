package com.kcalgrindai.app.core.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Safe, domain-level wrapper around Firebase Crashlytics.
 * Ensures error reporting is captured in production while gracefully degrading
 * in local unit tests and test fixtures where Firebase isn't initialized.
 */
object KcalGrindAICrashReporter {

    private val isAvailable: Boolean
        get() = try {
            FirebaseCrashlytics.getInstance()
            true
        } catch (e: Throwable) {
            false
        }

    /**
     * Log a breadcrumb message to Firebase Crashlytics.
     */
    fun log(message: String) {
        try {
            if (isAvailable) {
                FirebaseCrashlytics.getInstance().log(message)
            }
        } catch (_: Throwable) {
            // No-op outside Firebase context
        }
    }

    /**
     * Record a non-fatal exception with optional key-value diagnostic metadata.
     */
    fun recordException(throwable: Throwable, customKeys: Map<String, String> = emptyMap()) {
        try {
            if (isAvailable) {
                val crashlytics = FirebaseCrashlytics.getInstance()
                customKeys.forEach { (key, value) ->
                    crashlytics.setCustomKey(key, value)
                }
                crashlytics.recordException(throwable)
            }
        } catch (_: Throwable) {
            // No-op outside Firebase context
        }
    }

    /**
     * Set the authenticated user ID for crash attribution.
     */
    fun setUserId(userId: String?) {
        try {
            if (isAvailable) {
                FirebaseCrashlytics.getInstance().setUserId(userId ?: "")
            }
        } catch (_: Throwable) {
            // No-op outside Firebase context
        }
    }
}
