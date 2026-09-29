package com.kcalgrindai.app.domain.repository

import com.kcalgrindai.app.domain.model.SubscriptionPlan
import kotlinx.coroutines.flow.Flow

sealed interface BillingStatus {
    data object Ready : BillingStatus
    data class Unavailable(val message: String) : BillingStatus
}

sealed interface PurchaseResult {
    data class Success(val planId: String, val purchaseToken: String, val expiresAtMs: Long) : PurchaseResult
    data class Pending(val message: String) : PurchaseResult
    data class Error(val message: String) : PurchaseResult
}

interface BillingRepository {
    fun getAvailablePlans(): List<SubscriptionPlan>
    fun observeBillingStatus(): Flow<BillingStatus>
    fun observeIsPro(): Flow<Boolean>
    suspend fun isPro(): Boolean
    suspend fun subscribe(planId: String): PurchaseResult
    suspend fun restorePurchases(): PurchaseResult
    suspend fun setPro(isPro: Boolean)
    fun getActivePlanId(): String? = null
    fun getActiveExpiresAt(): Long? = null
}
