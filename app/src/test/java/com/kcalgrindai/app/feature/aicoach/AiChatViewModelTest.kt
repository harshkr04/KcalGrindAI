package com.kcalgrindai.app.feature.aicoach

import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.ItemSource
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.fakes.FakeAIAnalysisRepository
import com.kcalgrindai.app.fakes.FakeAICoachRepository
import com.kcalgrindai.app.fakes.FakeMealLogRepository
import com.kcalgrindai.app.fakes.FakeNutritionGoalRepository
import com.kcalgrindai.app.fakes.FakeUserProfileRepository
import com.kcalgrindai.app.fakes.FakeWaterLogRepository
import com.kcalgrindai.app.fakes.FakeWeightRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AiChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var aiCoachRepository: FakeAICoachRepository
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var nutritionGoalRepository: FakeNutritionGoalRepository
    private lateinit var mealLogRepository: FakeMealLogRepository
    private lateinit var waterLogRepository: FakeWaterLogRepository
    private lateinit var weightRepository: FakeWeightRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        aiCoachRepository = FakeAICoachRepository()
        userProfileRepository = FakeUserProfileRepository()
        nutritionGoalRepository = FakeNutritionGoalRepository()
        mealLogRepository = FakeMealLogRepository()
        waterLogRepository = FakeWaterLogRepository()
        weightRepository = FakeWeightRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `auto-logs water when suggested by AI assistant`() = runTest {
        val viewModel = AiChatViewModel(
            aiCoachRepository,
            userProfileRepository,
            nutritionGoalRepository,
            mealLogRepository,
            waterLogRepository,
            weightRepository
        )

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onInputChanged("log a glass of water")
        viewModel.sendMessage()
        advanceUntilIdle()

        val today = LocalDate.now().toString()
        val waterLogs = waterLogRepository.getWaterLogsByDate(today)
        assertEquals(1, waterLogs.size)
        assertEquals(250, waterLogs[0].amountMl)
        assertNotNull(viewModel.uiState.value.actionSuccessMessage)
        assertTrue(viewModel.uiState.value.actionSuccessMessage!!.contains("Logged 250ml water"))

        collectJob.cancel()
    }

    @Test
    fun `proposes food log requiring explicit confirmation before writing to repository`() = runTest {
        val viewModel = AiChatViewModel(
            aiCoachRepository,
            userProfileRepository,
            nutritionGoalRepository,
            mealLogRepository,
            waterLogRepository,
            weightRepository
        )

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val today = LocalDate.now().toString()

        // 1. Ask to log banana
        viewModel.onInputChanged("log a banana for breakfast")
        viewModel.sendMessage()
        advanceUntilIdle()

        // 2. Meal should NOT be written yet
        val mealsBeforeConfirm = mealLogRepository.getMealsWithItemsByDate(today)
        assertEquals(0, mealsBeforeConfirm.size)

        // 3. User taps confirm button
        val items = listOf(
            FoodLogItem(
                id = 0L,
                mealLogId = 0L,
                name = "Banana",
                servingDescription = "120g",
                servingGrams = 120.0,
                calories = 105.0,
                proteinG = 1.3,
                carbsG = 27.0,
                fatG = 0.3,
                fiberG = 3.0,
                source = ItemSource.AI,
                confidence = 0.95f,
                confirmed = true
            )
        )
        viewModel.confirmFoodLog(MealType.BREAKFAST, items)
        advanceUntilIdle()

        // 4. Now meal is written to Room
        val mealsAfterConfirm = mealLogRepository.getMealsWithItemsByDate(today)
        assertEquals(1, mealsAfterConfirm.size)
        assertEquals(105.0, mealsAfterConfirm[0].meal.totalCalories, 0.1)
        assertEquals("Banana", mealsAfterConfirm[0].items[0].name)

        collectJob.cancel()
    }

    @Test
    fun `photo attachment routes to AIAnalysisRepository and saves breakdown in chat`() = runTest {
        val fakeAnalysisRepo = FakeAIAnalysisRepository()
        val viewModel = AiChatViewModel(
            aiCoachRepository,
            userProfileRepository,
            nutritionGoalRepository,
            mealLogRepository,
            waterLogRepository,
            weightRepository,
            fakeAnalysisRepo
        )

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val dummyUri = android.net.Uri.parse("content://dummy/photo.jpg")
        viewModel.onImageAttached(dummyUri, "base64dummydata")
        assertNotNull(viewModel.uiState.value.attachedImageUri)
        assertEquals("base64dummydata", viewModel.uiState.value.attachedImageBase64)

        viewModel.sendMessage()
        advanceUntilIdle()

        // Attached image should be cleared after sending
        assertNull(viewModel.uiState.value.attachedImageUri)
        assertNull(viewModel.uiState.value.attachedImageBase64)

        // Conversation should contain user message with photo note and assistant reply with food breakdown
        val messages = aiCoachRepository.getMessagesForConversation(viewModel.uiState.value.conversationId)
        assertTrue(messages.any { it.text.contains("What's in this meal?") || it.text.contains("📸") })
        assertTrue(messages.any { it.structuredDataJson?.contains("create_food_log") == true })

        collectJob.cancel()
    }

    @Test
    fun `free plan enforces exactly 5 messages per day and blocks 6th message`() = runTest {
        val usageRepo = com.kcalgrindai.app.fakes.FakeAICoachUsageRepository(initialUsed = 0, initialIsPro = false)
        aiCoachRepository.usageRepository = usageRepo
        val viewModel = AiChatViewModel(
            aiCoachRepository,
            userProfileRepository,
            nutritionGoalRepository,
            mealLogRepository,
            waterLogRepository,
            weightRepository,
            aiCoachUsageRepository = usageRepo
        )

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(5, viewModel.uiState.value.usage.dailyLimit)
        assertEquals(0, viewModel.uiState.value.usage.usedToday)
        assertEquals(5, viewModel.uiState.value.usage.remaining)
        org.junit.Assert.assertFalse(viewModel.uiState.value.usage.isLimitReached)

        // Send 3 messages -> 2 remaining -> warning should be active
        repeat(3) {
            viewModel.onInputChanged("Hello $it")
            viewModel.sendMessage()
            advanceUntilIdle()
        }

        assertEquals(3, viewModel.uiState.value.usage.usedToday)
        assertEquals(2, viewModel.uiState.value.usage.remaining)
        assertTrue(viewModel.uiState.value.usage.isWarning)
        org.junit.Assert.assertFalse(viewModel.uiState.value.usage.isLimitReached)

        // Send 2 more messages -> 5 total -> 0 remaining -> limit reached
        repeat(2) {
            viewModel.onInputChanged("Next message $it")
            viewModel.sendMessage()
            advanceUntilIdle()
        }

        assertEquals(5, viewModel.uiState.value.usage.usedToday)
        assertEquals(0, viewModel.uiState.value.usage.remaining)
        assertTrue(viewModel.uiState.value.usage.isLimitReached)

        // Attempt 6th message -> blocked immediately, errorMessage set
        viewModel.onInputChanged("6th message should fail")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals(5, viewModel.uiState.value.usage.usedToday)
        assertEquals("You've reached today's free AI Coach limit. Your messages reset tomorrow.", viewModel.uiState.value.errorMessage)

        collectJob.cancel()
    }

    @Test
    fun `pro plan allows up to 50 messages per day and blocks 51st message`() = runTest {
        val usageRepo = com.kcalgrindai.app.fakes.FakeAICoachUsageRepository(initialUsed = 0, initialIsPro = true)
        aiCoachRepository.usageRepository = usageRepo
        val viewModel = AiChatViewModel(
            aiCoachRepository,
            userProfileRepository,
            nutritionGoalRepository,
            mealLogRepository,
            waterLogRepository,
            weightRepository,
            aiCoachUsageRepository = usageRepo
        )

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(50, viewModel.uiState.value.usage.dailyLimit)
        assertTrue(viewModel.uiState.value.usage.isPro)

        // Simulate 50 used messages
        usageRepo.resetForTesting(usedCount = 50)
        advanceUntilIdle()

        assertEquals(50, viewModel.uiState.value.usage.usedToday)
        assertEquals(0, viewModel.uiState.value.usage.remaining)
        assertTrue(viewModel.uiState.value.usage.isLimitReached)

        // 51st message blocked
        viewModel.onInputChanged("51st message")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals(50, viewModel.uiState.value.usage.usedToday)
        assertEquals("You've reached today's Pro AI Coach limit.", viewModel.uiState.value.errorMessage)

        collectJob.cancel()
    }

    @Test
    fun `upgrade from free to pro immediately changes limit to 50 and unblocks user`() = runTest {
        val usageRepo = com.kcalgrindai.app.fakes.FakeAICoachUsageRepository(initialUsed = 5, initialIsPro = false)
        aiCoachRepository.usageRepository = usageRepo
        val viewModel = AiChatViewModel(
            aiCoachRepository,
            userProfileRepository,
            nutritionGoalRepository,
            mealLogRepository,
            waterLogRepository,
            weightRepository,
            aiCoachUsageRepository = usageRepo
        )

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Initially blocked at 5/5
        assertTrue(viewModel.uiState.value.usage.isLimitReached)
        assertEquals(0, viewModel.uiState.value.usage.remaining)

        // User upgrades to Pro
        usageRepo.setPro(true)
        advanceUntilIdle()

        // Immediately updated to 50 limit and 45 remaining
        assertEquals(50, viewModel.uiState.value.usage.dailyLimit)
        assertEquals(45, viewModel.uiState.value.usage.remaining)
        org.junit.Assert.assertFalse(viewModel.uiState.value.usage.isLimitReached)

        // Can send messages again
        viewModel.onInputChanged("Hello from Pro!")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals(6, viewModel.uiState.value.usage.usedToday)
        assertEquals(44, viewModel.uiState.value.usage.remaining)

        collectJob.cancel()
    }

    @Test
    fun `downgrade from pro to free immediately enforces 5 limit`() = runTest {
        val usageRepo = com.kcalgrindai.app.fakes.FakeAICoachUsageRepository(initialUsed = 6, initialIsPro = true)
        aiCoachRepository.usageRepository = usageRepo
        val viewModel = AiChatViewModel(
            aiCoachRepository,
            userProfileRepository,
            nutritionGoalRepository,
            mealLogRepository,
            waterLogRepository,
            weightRepository,
            aiCoachUsageRepository = usageRepo
        )

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // User downgrades to Free
        usageRepo.setPro(false)
        advanceUntilIdle()

        // Now has 5 limit, with 6 used -> blocked
        assertEquals(5, viewModel.uiState.value.usage.dailyLimit)
        assertEquals(6, viewModel.uiState.value.usage.usedToday)
        assertEquals(0, viewModel.uiState.value.usage.remaining)
        assertTrue(viewModel.uiState.value.usage.isLimitReached)

        // Message is blocked
        viewModel.onInputChanged("Attempt sending after downgrade")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals(6, viewModel.uiState.value.usage.usedToday)
        assertEquals("You've reached today's free AI Coach limit. Your messages reset tomorrow.", viewModel.uiState.value.errorMessage)

        collectJob.cancel()
    }
}
