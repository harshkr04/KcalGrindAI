package com.kcalgrindai.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.kcalgrindai.app.domain.model.AICoachQuotaExceededException
import com.kcalgrindai.app.domain.model.AICoachUsage
import com.kcalgrindai.app.domain.repository.AICoachUsageRepository
import com.kcalgrindai.app.domain.repository.BillingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AICoachUsageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val billingRepository: BillingRepository
) : AICoachUsageRepository {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val mutex = Mutex()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _usageFlow = MutableStateFlow(AICoachUsage())

    init {
        // Initialize state from prefs and listen to billing pro status changes
        repositoryScope.launch {
            mutex.withLock {
                syncUsageStateLocked()
            }
            billingRepository.observeIsPro().collect { isProFromBilling ->
                mutex.withLock {
                    if (prefs.contains(KEY_OVERRIDE_PRO) && !com.kcalgrindai.app.BuildConfig.DEBUG) {
                        prefs.edit().remove(KEY_OVERRIDE_PRO).apply()
                    }
                    val effectivePro = if (com.kcalgrindai.app.BuildConfig.DEBUG && prefs.contains(KEY_OVERRIDE_PRO)) {
                        prefs.getBoolean(KEY_OVERRIDE_PRO, false)
                    } else {
                        isProFromBilling
                    }
                    val current = syncUsageStateLocked(effectivePro)
                    val newLimit = if (effectivePro) PRO_DAILY_LIMIT else FREE_DAILY_LIMIT
                    val updated = current.copy(
                        isPro = effectivePro,
                        dailyLimit = newLimit
                    )
                    _usageFlow.value = updated
                }
            }
        }
    }

    private fun getTodayDateString(): String {
        return LocalDate.now(ZoneId.systemDefault()).toString()
    }

    /**
     * Synchronizes in-memory and SharedPreferences state.
     * If the local date has changed (i.e. midnight passed), resets used count to 0.
     */
    private fun syncUsageStateLocked(overrideProStatus: Boolean? = null): AICoachUsage {
        val today = getTodayDateString()
        val savedDate = prefs.getString(KEY_LAST_RESET_DATE, null)
        var usedCount = prefs.getInt(KEY_USED_COUNT, 0)

        if (savedDate == null || savedDate != today) {
            // New calendar day in user's local timezone
            usedCount = 0
            prefs.edit()
                .putString(KEY_LAST_RESET_DATE, today)
                .putInt(KEY_USED_COUNT, 0)
                .apply()
        }

        val isPro = overrideProStatus ?: if (com.kcalgrindai.app.BuildConfig.DEBUG && prefs.contains(KEY_OVERRIDE_PRO)) {
            prefs.getBoolean(KEY_OVERRIDE_PRO, false)
        } else {
            _usageFlow.value.isPro
        }

        val dailyLimit = if (isPro) PRO_DAILY_LIMIT else FREE_DAILY_LIMIT
        val usage = AICoachUsage(
            usedToday = usedCount,
            dailyLimit = dailyLimit,
            isPro = isPro,
            lastResetDate = today
        )
        _usageFlow.value = usage
        return usage
    }

    override fun observeUsage(): Flow<AICoachUsage> = _usageFlow.asStateFlow()

    override suspend fun getUsage(): AICoachUsage = mutex.withLock {
        syncUsageStateLocked()
    }

    override suspend fun checkQuota(): Boolean = mutex.withLock {
        val current = syncUsageStateLocked()
        if (current.isLimitReached) {
            val msg = if (current.isPro) {
                "You've reached today's Pro AI Coach limit."
            } else {
                "You've reached today's free AI Coach limit. Your messages reset tomorrow."
            }
            throw AICoachQuotaExceededException(msg)
        }
        true
    }

    override suspend fun recordMessageSent(): Unit = mutex.withLock {
        val current = syncUsageStateLocked()
        val newUsed = current.usedToday + 1
        val today = getTodayDateString()

        prefs.edit()
            .putString(KEY_LAST_RESET_DATE, today)
            .putInt(KEY_USED_COUNT, newUsed)
            .apply()

        _usageFlow.value = current.copy(usedToday = newUsed)
    }

    override suspend fun resetQuotaForNewDay(): Unit = mutex.withLock {
        syncUsageStateLocked()
    }

    override suspend fun setPro(isPro: Boolean): Unit = mutex.withLock {
        if (com.kcalgrindai.app.BuildConfig.DEBUG) {
            prefs.edit().putBoolean(KEY_OVERRIDE_PRO, isPro).apply()
        }
        billingRepository.setPro(isPro)
        val current = syncUsageStateLocked(isPro)
        val newLimit = if (isPro) PRO_DAILY_LIMIT else FREE_DAILY_LIMIT
        _usageFlow.value = current.copy(isPro = isPro, dailyLimit = newLimit)
    }

    override suspend fun resetForTesting(usedCount: Int, date: String?): Unit = mutex.withLock {
        val targetDate = date ?: getTodayDateString()
        prefs.edit()
            .putString(KEY_LAST_RESET_DATE, targetDate)
            .putInt(KEY_USED_COUNT, usedCount)
            .apply()

        val isPro = _usageFlow.value.isPro
        val limit = if (isPro) PRO_DAILY_LIMIT else FREE_DAILY_LIMIT
        _usageFlow.value = AICoachUsage(
            usedToday = usedCount,
            dailyLimit = limit,
            isPro = isPro,
            lastResetDate = targetDate
        )
    }

    companion object {
        const val PREFS_NAME = "kcalgrind_aicoach_usage"
        const val KEY_LAST_RESET_DATE = "last_reset_date"
        const val KEY_USED_COUNT = "used_count"
        const val KEY_OVERRIDE_PRO = "override_pro"

        const val FREE_DAILY_LIMIT = 5
        const val PRO_DAILY_LIMIT = 50
    }
}
