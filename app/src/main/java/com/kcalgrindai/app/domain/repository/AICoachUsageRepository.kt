package com.kcalgrindai.app.domain.repository

import com.kcalgrindai.app.domain.model.AICoachUsage
import kotlinx.coroutines.flow.Flow

interface AICoachUsageRepository {
    fun observeUsage(): Flow<AICoachUsage>
    suspend fun getUsage(): AICoachUsage
    suspend fun checkQuota(): Boolean
    suspend fun recordMessageSent()
    suspend fun resetQuotaForNewDay()
    suspend fun setPro(isPro: Boolean)
    suspend fun resetForTesting(usedCount: Int = 0, date: String? = null)
}
