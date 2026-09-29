package com.kcalgrindai.app.domain.model

data class AICoachUsage(
    val usedToday: Int = 0,
    val dailyLimit: Int = 5,
    val isPro: Boolean = false,
    val lastResetDate: String = ""
) {
    val remaining: Int get() = (dailyLimit - usedToday).coerceAtLeast(0)
    val isLimitReached: Boolean get() = remaining <= 0
    val isWarning: Boolean get() = remaining == 2
}

class AICoachQuotaExceededException(
    override val message: String
) : Exception(message)
