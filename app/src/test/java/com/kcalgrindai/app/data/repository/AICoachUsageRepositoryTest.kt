package com.kcalgrindai.app.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kcalgrindai.app.domain.model.AICoachQuotaExceededException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class AICoachUsageRepositoryTest {

    private lateinit var context: Context
    private val isProFlow = kotlinx.coroutines.flow.MutableStateFlow(false)
    private val billingRepo = object : com.kcalgrindai.app.domain.repository.BillingRepository {
        override fun getAvailablePlans() = emptyList<com.kcalgrindai.app.domain.model.SubscriptionPlan>()
        override fun observeBillingStatus() = kotlinx.coroutines.flow.MutableStateFlow(com.kcalgrindai.app.domain.repository.BillingStatus.Ready)
        override fun observeIsPro() = isProFlow
        override suspend fun isPro() = isProFlow.value
        override suspend fun subscribe(planId: String) = com.kcalgrindai.app.domain.repository.PurchaseResult.Success(planId, "token", 0L)
        override suspend fun restorePurchases() = com.kcalgrindai.app.domain.repository.PurchaseResult.Success("annual", "token", 0L)
        override suspend fun setPro(isPro: Boolean) { isProFlow.value = isPro }
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("kcalgrind_aicoach_usage", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        isProFlow.value = false
    }

    @Test
    fun `free tier default has 5 daily limit and 0 used`() = runTest {
        val repo = AICoachUsageRepositoryImpl(context, billingRepo)
        val usage = repo.getUsage()

        assertEquals(5, usage.dailyLimit)
        assertEquals(0, usage.usedToday)
        assertEquals(5, usage.remaining)
        assertFalse(usage.isPro)
        assertFalse(usage.isLimitReached)
    }

    @Test
    fun `pro tier has 50 daily limit`() = runTest {
        val repo = AICoachUsageRepositoryImpl(context, billingRepo)
        repo.setPro(true)
        val usage = repo.getUsage()

        assertEquals(50, usage.dailyLimit)
        assertTrue(usage.isPro)
        assertEquals(50, usage.remaining)
    }

    @Test
    fun `usage persists across repository instances (app restart simulation)`() = runTest {
        val repo1 = AICoachUsageRepositoryImpl(context, billingRepo)
        repo1.recordMessageSent()
        repo1.recordMessageSent()
        repo1.recordMessageSent()

        assertEquals(3, repo1.getUsage().usedToday)

        // Simulate app restart by creating a new repository instance
        val repo2 = AICoachUsageRepositoryImpl(context, billingRepo)
        val usageAfterRestart = repo2.getUsage()

        assertEquals(3, usageAfterRestart.usedToday)
        assertEquals(2, usageAfterRestart.remaining)
        assertTrue(usageAfterRestart.isWarning)
    }

    @Test
    fun `quota resets on next local calendar day`() = runTest {
        val prefs = context.getSharedPreferences("kcalgrind_aicoach_usage", Context.MODE_PRIVATE)
        // Store usage from yesterday
        val yesterday = LocalDate.now().minusDays(1).toString()
        prefs.edit()
            .putInt("used_count", 5)
            .putString("last_reset_date", yesterday)
            .commit()

        val repo = AICoachUsageRepositoryImpl(context, billingRepo)
        val usage = repo.getUsage()

        // Should automatically reset to 0 for today
        assertEquals(0, usage.usedToday)
        assertEquals(5, usage.remaining)
        assertFalse(usage.isLimitReached)
        assertEquals(LocalDate.now().toString(), usage.lastResetDate)
    }

    @Test
    fun `checkQuota blocks on 6th message for Free plan`() = runTest {
        val repo = AICoachUsageRepositoryImpl(context, billingRepo)

        repeat(5) {
            assertTrue(repo.checkQuota())
            repo.recordMessageSent()
        }

        assertEquals(5, repo.getUsage().usedToday)
        assertEquals(0, repo.getUsage().remaining)
        assertTrue(repo.getUsage().isLimitReached)

        // 6th message throws quota exceeded
        var exceptionThrown = false
        try {
            repo.checkQuota()
        } catch (ex: AICoachQuotaExceededException) {
            exceptionThrown = true
            assertTrue(ex.message?.contains("free AI Coach limit") == true)
        }
        assertTrue("Expected AICoachQuotaExceededException was not thrown", exceptionThrown)
    }

    @Test
    fun `upgrade to Pro immediately unblocks and sets limit to 50`() = runTest {
        val repo = AICoachUsageRepositoryImpl(context, billingRepo)
        repeat(5) { repo.recordMessageSent() }

        assertTrue(repo.getUsage().isLimitReached)

        // Upgrade to Pro
        repo.setPro(true)
        val usage = repo.getUsage()

        assertEquals(50, usage.dailyLimit)
        assertEquals(5, usage.usedToday)
        assertEquals(45, usage.remaining)
        assertFalse(usage.isLimitReached)
        assertTrue(repo.checkQuota())
    }

    @Test
    fun `downgrade to Free immediately restricts to 5 limit`() = runTest {
        val repo = AICoachUsageRepositoryImpl(context, billingRepo)
        repo.setPro(true)
        repeat(6) { repo.recordMessageSent() }

        // Downgrade to Free
        repo.setPro(false)
        val usage = repo.getUsage()

        assertEquals(5, usage.dailyLimit)
        assertEquals(6, usage.usedToday)
        assertEquals(0, usage.remaining)
        assertTrue(usage.isLimitReached)

        var exceptionThrown = false
        try {
            repo.checkQuota()
        } catch (ex: AICoachQuotaExceededException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
    }
}
