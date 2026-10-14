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

    private fun createTestGoal(calories: Int) = com.kcalgrindai.app.domain.model.NutritionGoal(
        id = 1L,
        userId = 1L,
        calories = calories,
        proteinG = 150.0,
        carbsG = 250.0,
        fatG = 70.0,
        waterLiters = 2.5,
        waterGlasses = 8,
        bmr = 1750.0,
        tdee = calories.toDouble(),
        isCustom = false,
        updatedAt = System.currentTimeMillis()
    )

    @Test
    fun `empty diary provides zero consumed calories and full target as remaining calories`() = runTest {
        nutritionGoalRepository.saveGoal(createTestGoal(2200))
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
        val consumed = viewModel.getDailyConsumedCalories(today)
        assertEquals(0, consumed)

        viewModel.onInputChanged("How is my nutrition today?")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals(0, aiCoachRepository.lastConsumedCalories)
        assertEquals(2200, aiCoachRepository.lastRemainingCalories)
        assertEquals(2200, aiCoachRepository.lastTargetCalories)

        collectJob.cancel()
    }

    @Test
    fun `nonzero diary consumption calculates accurate consumed calories`() = runTest {
        nutritionGoalRepository.saveGoal(createTestGoal(2500))
        val today = LocalDate.now().toString()
        val meal1 = com.kcalgrindai.app.domain.model.MealLog(
            id = 1L,
            date = today,
            mealType = MealType.BREAKFAST,
            totalCalories = 400.0,
            loggedAt = System.currentTimeMillis(),
            source = com.kcalgrindai.app.domain.model.LogSource.MANUAL,
            synced = true
        )
        val item1 = FoodLogItem(
            id = 1L,
            mealLogId = 1L,
            name = "Oatmeal",
            servingDescription = "1 bowl",
            servingGrams = 150.0,
            calories = 400.0,
            proteinG = 15.0,
            carbsG = 65.0,
            fatG = 8.0,
            fiberG = 6.0,
            source = ItemSource.MANUAL,
            confidence = 1.0f,
            confirmed = true
        )
        mealLogRepository.saveMeal(meal1, listOf(item1))

        val meal2 = com.kcalgrindai.app.domain.model.MealLog(
            id = 2L,
            date = today,
            mealType = MealType.LUNCH,
            totalCalories = 650.0,
            loggedAt = System.currentTimeMillis(),
            source = com.kcalgrindai.app.domain.model.LogSource.MANUAL,
            synced = true
        )
        val item2 = FoodLogItem(
            id = 2L,
            mealLogId = 2L,
            name = "Chicken Rice",
            servingDescription = "1 plate",
            servingGrams = 300.0,
            calories = 650.0,
            proteinG = 45.0,
            carbsG = 75.0,
            fatG = 12.0,
            fiberG = 4.0,
            source = ItemSource.MANUAL,
            confidence = 1.0f,
            confirmed = true
        )
        mealLogRepository.saveMeal(meal2, listOf(item2))

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

        val consumed = viewModel.getDailyConsumedCalories(today)
        assertEquals(1050, consumed)

        viewModel.onInputChanged("What should I eat for dinner?")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals(1050, aiCoachRepository.lastConsumedCalories)
        assertEquals(1450, aiCoachRepository.lastRemainingCalories)
        assertEquals(2500, aiCoachRepository.lastTargetCalories)

        collectJob.cancel()
    }

    @Test
    fun `remaining calories derives accurately from daily target and consumed calories`() = runTest {
        nutritionGoalRepository.saveGoal(createTestGoal(2000))
        val today = LocalDate.now().toString()
        val meal = com.kcalgrindai.app.domain.model.MealLog(
            id = 1L,
            date = today,
            mealType = MealType.SNACK,
            totalCalories = 320.0,
            loggedAt = System.currentTimeMillis(),
            source = com.kcalgrindai.app.domain.model.LogSource.MANUAL,
            synced = true
        )
        val item = FoodLogItem(
            id = 1L,
            mealLogId = 1L,
            name = "Greek Yogurt",
            servingDescription = "200g",
            servingGrams = 200.0,
            calories = 320.0,
            proteinG = 20.0,
            carbsG = 25.0,
            fatG = 5.0,
            fiberG = 0.0,
            source = ItemSource.MANUAL,
            confidence = 1.0f,
            confirmed = true
        )
        mealLogRepository.saveMeal(meal, listOf(item))

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

        viewModel.onInputChanged("Tell me about my remaining calories")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals(320, aiCoachRepository.lastConsumedCalories)
        assertEquals(1680, aiCoachRepository.lastRemainingCalories)
        assertEquals(2000, aiCoachRepository.lastTargetCalories)

        collectJob.cancel()
    }

    @Test
    fun `repository failure handling gracefully falls back to zero consumed calories without crashing`() = runTest {
        nutritionGoalRepository.saveGoal(createTestGoal(1900))
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

        // Simulate repository observing failure
        mealLogRepository.shouldFailObserve = true

        val today = LocalDate.now().toString()
        val consumed = viewModel.getDailyConsumedCalories(today)
        assertEquals(0, consumed)

        // sendMessage should not throw or crash
        viewModel.onInputChanged("Hello after error")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals(0, aiCoachRepository.lastConsumedCalories)
        assertEquals(1900, aiCoachRepository.lastRemainingCalories)
        assertEquals(1900, aiCoachRepository.lastTargetCalories)
        assertNull(viewModel.uiState.value.errorMessage)

        collectJob.cancel()
    }
}
