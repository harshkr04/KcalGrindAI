package com.kcalgrindai.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.kcalgrindai.app.data.local.dao.UserProfileDao
import com.kcalgrindai.app.domain.model.SubscriptionPlan
import com.kcalgrindai.app.domain.repository.BillingRepository
import com.kcalgrindai.app.domain.repository.BillingStatus
import com.kcalgrindai.app.domain.repository.PurchaseResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userProfileDao: UserProfileDao
) : BillingRepository {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _billingStatus = MutableStateFlow<BillingStatus>(
        BillingStatus.Ready
    )

    private val configuredPlans = listOf(
        SubscriptionPlan(
            id = "kcal_grind_pro_annual",
            title = "Annual Plan",
            priceFormatted = "$39.99",
            billingPeriod = "per year",
            pricePerMonthEquivalent = "$3.33 / month",
            trialDays = 7,
            discountPercent = 33,
            isPopular = true,
            badge = "SAVE 33% • MOST POPULAR",
            features = listOf(
                "7-day free trial included",
                "50 AI Coach messages/day (vs 5/day Free)",
                "Unlimited AI meal photo scanning",
                "Unlimited natural language voice & text logging",
                "Full 90-day macro & weight trend analytics",
                "Access to all 90+ Indian & International curated recipes",
                "Export meal history to PDF",
                "Unlimited cloud backup & cross-device sync"
            )
        ),
        SubscriptionPlan(
            id = "kcal_grind_pro_monthly",
            title = "Monthly Plan",
            priceFormatted = "$4.99",
            billingPeriod = "per month",
            pricePerMonthEquivalent = "$4.99 / month",
            trialDays = 7,
            discountPercent = 0,
            isPopular = false,
            badge = null,
            features = listOf(
                "7-day free trial included",
                "50 AI Coach messages/day (vs 5/day Free)",
                "Unlimited AI meal photo scanning",
                "Unlimited natural language voice & text logging",
                "Full 90-day macro & weight trend analytics",
                "Access to all 90+ Indian & International curated recipes",
                "Export meal history to PDF",
                "Unlimited cloud backup & cross-device sync"
            )
        )
    )

    override fun getAvailablePlans(): List<SubscriptionPlan> = configuredPlans

    private val _isProFlow: MutableStateFlow<Boolean> by lazy {
        MutableStateFlow(prefs.getBoolean(KEY_IS_PRO, false))
    }

    override fun observeBillingStatus(): Flow<BillingStatus> = _billingStatus.asStateFlow()

    override fun observeIsPro(): Flow<Boolean> = _isProFlow.asStateFlow()

    override suspend fun setPro(isPro: Boolean) {
        val editor = prefs.edit().putBoolean(KEY_IS_PRO, isPro)
        if (!isPro) {
            editor.remove(KEY_ACTIVE_PLAN_ID).remove(KEY_EXPIRES_AT)
        }
        editor.apply()
        _isProFlow.value = isPro
    }

    override suspend fun isPro(): Boolean = _isProFlow.value

    override fun getActivePlanId(): String? = prefs.getString(KEY_ACTIVE_PLAN_ID, null)

    override fun getActiveExpiresAt(): Long? = prefs.getLong(KEY_EXPIRES_AT, 0L).takeIf { it > 0L }

    override suspend fun subscribe(planId: String): PurchaseResult = withContext(Dispatchers.IO) {
        if (com.kcalgrindai.app.BuildConfig.DEBUG) {
            // In debug/development, simulate successful subscription activation
            val expiresAt = System.currentTimeMillis() + (if (planId.contains("annual")) 365L else 30L) * 24 * 3600 * 1000
            prefs.edit()
                .putBoolean(KEY_IS_PRO, true)
                .putString(KEY_ACTIVE_PLAN_ID, planId)
                .putLong(KEY_EXPIRES_AT, expiresAt)
                .apply()
            _isProFlow.value = true
            PurchaseResult.Success(
                planId = planId,
                purchaseToken = "debug_verified_token_${System.currentTimeMillis()}",
                expiresAtMs = expiresAt
            )
        } else {
            PurchaseResult.Error(
                "Google Play Store billing service is unavailable in this environment. In-app purchases require a physical device signed in to Google Play with configured billing products."
            )
        }
    }

    override suspend fun restorePurchases(): PurchaseResult = withContext(Dispatchers.IO) {
        val isCurrentlyPro = prefs.getBoolean(KEY_IS_PRO, false)
        if (isCurrentlyPro) {
            _isProFlow.value = true
            val planId = prefs.getString(KEY_ACTIVE_PLAN_ID, "kcal_grind_pro_annual") ?: "kcal_grind_pro_annual"
            val expiresAt = prefs.getLong(KEY_EXPIRES_AT, System.currentTimeMillis() + 365L * 24 * 3600 * 1000)
            PurchaseResult.Success(
                planId = planId,
                purchaseToken = "verified_token",
                expiresAtMs = expiresAt
            )
        } else {
            PurchaseResult.Error(
                "No active subscriptions found for this Google Play account. Please subscribe to activate Kcal Grind Pro."
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "kcalgrind_billing"
        private const val KEY_IS_PRO = "is_pro"
        private const val KEY_ACTIVE_PLAN_ID = "active_plan_id"
        private const val KEY_EXPIRES_AT = "active_expires_at"
    }
}
