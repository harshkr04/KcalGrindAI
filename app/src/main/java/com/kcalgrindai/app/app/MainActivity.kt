package com.kcalgrindai.app.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import com.kcalgrindai.app.BuildConfig
import com.kcalgrindai.app.core.notification.NotificationHelper
import com.kcalgrindai.app.core.designsystem.KcalGrindTheme
import com.kcalgrindai.app.core.navigation.KcalGrindNavHost
import com.kcalgrindai.app.domain.model.ThemeMode
import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val destinationRoute = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannels(this)
        if (BuildConfig.DEBUG) {
            handleTriggerReminder(intent)
        }
        destinationRoute.value = extractDestination(intent)
        enableEdgeToEdge()
        setContent {
            val destination by destinationRoute.collectAsState()
            val themeMode by userPreferencesRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val isSystemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val view = LocalView.current
            DisposableEffect(isDark) {
                val activity = view.context as? ComponentActivity
                activity?.enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            KcalGrindTheme(darkTheme = isDark) {
                KcalGrindNavHost(initialRoute = destination)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (BuildConfig.DEBUG) {
            handleTriggerReminder(intent)
        }
        extractDestination(intent)?.let {
            destinationRoute.value = it
        }
    }

    internal fun handleTriggerReminder(intent: android.content.Intent?, isDebug: Boolean = BuildConfig.DEBUG): Boolean {
        if (!isDebug) return false
        val trigger = intent?.getStringExtra(EXTRA_TRIGGER_REMINDER) ?: return false
        return when (trigger.lowercase()) {
            "meal" -> {
                NotificationHelper.showMealReminder(this, "Lunch")
                true
            }
            "water" -> {
                NotificationHelper.showHydrationReminder(this)
                true
            }
            else -> false
        }
    }

    companion object {
        const val EXTRA_DESTINATION = "EXTRA_DESTINATION"
        const val EXTRA_TRIGGER_REMINDER = "EXTRA_TRIGGER_REMINDER"

        val ALLOWED_NON_SENSITIVE_DESTINATIONS = setOf(
            "home",
            "diary",
            "diary?mealslot=breakfast",
            "diary?mealslot=lunch",
            "diary?mealslot=dinner",
            "diary?mealslot=snack",
            "diary?mealslot=snacks"
        )

        /**
         * Validates a destination route against the strict allowlist of non-sensitive destinations.
         * MainActivity is exported, so any external intent in production must match safe targets only.
         */
        fun isAllowedDestination(destination: String?): Boolean {
            if (destination.isNullOrBlank()) return false
            val normalized = destination.trim().lowercase()
            if (ALLOWED_NON_SENSITIVE_DESTINATIONS.contains(normalized)) return true
            if (normalized.startsWith("diary?mealslot=")) {
                val slot = normalized.substringAfter("diary?mealslot=")
                return slot in setOf("breakfast", "lunch", "dinner", "snack", "snacks")
            }
            return false
        }

        /**
         * Extracts and validates the target route from an intent:
         * - In debug builds (isDebug = true): preserves the debug-only EXTRA_DESTINATION bypass for test flexibility.
         * - In release builds (isDebug = false): strictly validates against [isAllowedDestination], rejecting any sensitive routes.
         */
        fun extractDestination(
            intent: android.content.Intent?,
            isDebug: Boolean = BuildConfig.DEBUG
        ): String? {
            val destination = intent?.getStringExtra(EXTRA_DESTINATION)?.takeIf { it.isNotBlank() } ?: return null
            if (isDebug) {
                return destination
            }
            return if (isAllowedDestination(destination)) destination else null
        }
    }
}
