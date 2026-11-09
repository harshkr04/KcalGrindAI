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
import org.junit.Assert.assertFalse
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

    @Test
    fun `quota reached 429 sets friendly message and re-enables input`() = runTest {
        aiCoachRepository.sendError = com.kcalgrindai.app.domain.model.AICoachQuotaExceededException(
            "You've reached today's free AI Coach limit. Your messages reset tomorrow."
        )
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

        viewModel.onInputChanged("What should I eat?")
        viewModel.sendMessage()
        advanceUntilIdle()

        // Input must be re-enabled (isSending = false)
        org.junit.Assert.assertFalse(viewModel.uiState.value.isSending)
        assertEquals(
            "You've reached today's free AI Coach limit. Your messages reset tomorrow.",
            viewModel.uiState.value.errorMessage
        )

        collectJob.cancel()
    }

    @Test
    fun `service unavailable 503 on normal text provides deterministic summary and re-enables input`() = runTest {
        nutritionGoalRepository.saveGoal(createTestGoal(2000))
        aiCoachRepository.sendError = RuntimeException("HTTP 503 Service Unavailable")
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

        viewModel.onInputChanged("Give me meal suggestions")
        viewModel.sendMessage()
        advanceUntilIdle()

        // Input must be re-enabled (isSending = false)
        org.junit.Assert.assertFalse(viewModel.uiState.value.isSending)
        assertEquals(
            "Sproutling needs an internet connection",
            viewModel.uiState.value.errorMessage
        )
        // Saved message must contain the deterministic offline summary with no food suggestions
        val messages = aiCoachRepository.getMessagesForConversation(viewModel.uiState.value.conversationId)
        val assistantMsg = messages.firstOrNull { it.role == com.kcalgrindai.app.domain.model.MessageRole.ASSISTANT }
        assertNotNull(assistantMsg)
        assertTrue(assistantMsg!!.text.startsWith("Sproutling needs an internet connection."))
        assertTrue(assistantMsg.text.contains("Today's remaining budget:"))
        assertTrue(assistantMsg.text.contains("Calories:"))
        assertTrue(!assistantMsg.text.contains("Chicken"))
        assertTrue(!assistantMsg.text.contains("Salmon"))
        assertTrue(!assistantMsg.text.contains("Oatmeal"))

        collectJob.cancel()
    }

    @Test
    fun `normal text offline returns deterministic remaining budget without food suggestions`() = runTest {
        nutritionGoalRepository.saveGoal(createTestGoal(2200))
        aiCoachRepository.sendError = java.io.IOException("Unable to resolve host")
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

        viewModel.onInputChanged("What should I eat?")
        viewModel.sendMessage()
        advanceUntilIdle()

        org.junit.Assert.assertFalse(viewModel.uiState.value.isSending)
        assertEquals(
            "Sproutling needs an internet connection",
            viewModel.uiState.value.errorMessage
        )
        val messages = aiCoachRepository.getMessagesForConversation(viewModel.uiState.value.conversationId)
        val assistantMsg = messages.firstOrNull { it.role == com.kcalgrindai.app.domain.model.MessageRole.ASSISTANT }
        assertNotNull(assistantMsg)
        assertTrue(assistantMsg!!.text.startsWith("Sproutling needs an internet connection."))
        assertTrue(assistantMsg.text.contains("Today's remaining budget:"))
        assertTrue(assistantMsg.text.contains("Calories: 2200 kcal left (Target: 2200 kcal, Consumed: 0 kcal)"))
        assertTrue(assistantMsg.text.contains("Protein:"))
        assertTrue(assistantMsg.text.contains("Carbs:"))
        assertTrue(assistantMsg.text.contains("Fat:"))
        // Zero food suggestions or persona text
        assertTrue(!assistantMsg.text.contains("Chicken"))
        assertTrue(!assistantMsg.text.contains("Salmon"))
        assertTrue(!assistantMsg.text.contains("Oatmeal"))
        assertTrue(!assistantMsg.text.contains("fitness journey"))

        collectJob.cancel()
    }

    @Test
    fun `crisis text offline shows supportive message with verified region-aware helplines`() = runTest {
        aiCoachRepository.sendError = java.io.IOException("Network unreachable")
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

        viewModel.onInputChanged("I want to starve myself and die")
        viewModel.sendMessage()
        advanceUntilIdle()

        org.junit.Assert.assertFalse(viewModel.uiState.value.isSending)
        val messages = aiCoachRepository.getMessagesForConversation(viewModel.uiState.value.conversationId)
        val assistantMsg = messages.firstOrNull { it.role == com.kcalgrindai.app.domain.model.MessageRole.ASSISTANT }
        assertNotNull(assistantMsg)
        
        // Verified region-aware helplines (US environment in Robolectric)
        assertTrue(assistantMsg!!.text.contains("988")) // Suicide & Crisis Lifeline
        assertTrue(assistantMsg.text.contains("1-888-375-7767")) // ANAD Eating Disorders Helpline
        assertTrue(assistantMsg.text.contains("741741")) // Crisis Text Line
        assertTrue(assistantMsg.text.contains("911")) // US Emergency

        // Must not contain meal suggestions or calorie budget
        assertTrue(!assistantMsg.text.contains("Grilled Herb Chicken"))
        assertTrue(!assistantMsg.text.contains("budget"))

        collectJob.cancel()
    }

    @Test
    fun `crisis text on 503 shows supportive message with verified region-aware helplines`() = runTest {
        aiCoachRepository.sendError = RuntimeException("HTTP 503 Service Unavailable")
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

        viewModel.onInputChanged("How do I purge all my calories after dinner?")
        viewModel.sendMessage()
        advanceUntilIdle()

        org.junit.Assert.assertFalse(viewModel.uiState.value.isSending)
        val messages = aiCoachRepository.getMessagesForConversation(viewModel.uiState.value.conversationId)
        val assistantMsg = messages.firstOrNull { it.role == com.kcalgrindai.app.domain.model.MessageRole.ASSISTANT }
        assertNotNull(assistantMsg)

        // Verified helplines
        assertTrue(assistantMsg!!.text.contains("988"))
        assertTrue(assistantMsg.text.contains("1-888-375-7767"))
        assertTrue(assistantMsg.text.contains("911"))

        collectJob.cancel()
    }

    @Test
    fun `getRegionAwareCrisisResponse formats verified helplines defaulting to India and 112 plus all other regions`() {
        // Regardless of locale, India + 112 is primary, and all international regions are listed below
        val localesToTest = listOf("IN", "GB", "US", "ZZ", "")
        for (locale in localesToTest) {
            val response = AiChatViewModel.getRegionAwareCrisisResponse(locale)
            // India & 112 verified primary
            assertTrue(response.contains("112")) // National Emergency Number, India
            assertTrue(response.contains("+91 98204 66726")) // AASRA
            assertTrue(response.contains("1800-599-0019")) // KIRAN
            assertTrue(response.contains("+91 9999 666 555")) // Vandrevala Foundation
            assertTrue(response.contains("14416")) // Tele-MANAS

            // United States & Canada verified
            assertTrue(response.contains("911")) // Emergency US
            assertTrue(response.contains("988")) // Suicide & Crisis Lifeline
            assertTrue(response.contains("1-888-375-7767")) // ANAD Helpline
            assertTrue(response.contains("741741")) // Crisis Text Line

            // United Kingdom verified
            assertTrue(response.contains("999")) // Emergency UK
            assertTrue(response.contains("116 123")) // Samaritans
            assertTrue(response.contains("0808 801 0677")) // Beat Eating Disorders
            assertTrue(response.contains("85258")) // Shout

            // International verified
            assertTrue(response.contains("befrienders.org"))
            assertTrue(response.contains("findahelpline.com"))
        }
    }

    @Test
    fun `checkClientCrisisKeywords detects misspelled and Hinglish crisis phrases while allowing benign fasting phrases`() {
        val benignPhrases = listOf(
            "Can I do 16:8 fasting?",
            "What are the benefits of 16/8 intermittent fasting?",
            "I want to try a 16:8 fast starting today",
            "Fasting for 16 hours and eating in an 8 hour window",
            "How much protein should I eat during 16:8 intermittent fasting?",
            "Tell me healthy recipes for dinner with 500 kcal total",
            "I ate 1800 calories today, is that good?",
            "Can I have green tea during intermittent fasting?"
        )

        for (phrase in benignPhrases) {
            assertFalse(
                "Benign phrase should not trigger crisis check: $phrase",
                AiChatViewModel.checkClientCrisisKeywords(phrase)
            )
        }

        val crisisPhrases = listOf(
            // English standard & misspellings
            "I want to kill myself",
            "I am thinking of committing suicide",
            "how to starve myself to drop weight quickly",
            "I feel so depressed I might commit sucide",
            "thinking about suicde tonight",
            "I want to kil myself",
            "how to starrve fast",
            "I have been purgging every meal",
            "vommit after eating lunch",
            // Hinglish
            "main mar jana chahta hoon",
            "zindagi khatam karni hai ab",
            "main jaan de dunga",
            "suicide karne ka man kar raha hai",
            "khana chhod dunga jab tak patla na ho jau",
            "khana khane ke baad ulti karna chahta hu"
        )

        for (phrase in crisisPhrases) {
            assertTrue(
                "Crisis phrase should trigger check: $phrase",
                AiChatViewModel.checkClientCrisisKeywords(phrase)
            )
        }
    }

    @Test
    fun `crisis text online intercepts locally and never consumes quota or calls remote API`() = runTest {
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

        assertEquals(0, aiCoachRepository.sendChatMessageCallCount)

        viewModel.onInputChanged("I feel suicidal and want to end my life")
        viewModel.sendMessage()
        advanceUntilIdle()

        org.junit.Assert.assertFalse(viewModel.uiState.value.isSending)

        // Strict assertion: remote API was NEVER called, zero quota consumed
        assertEquals(0, aiCoachRepository.sendChatMessageCallCount)

        val messages = aiCoachRepository.getMessagesForConversation(viewModel.uiState.value.conversationId)
        val assistantMsg = messages.firstOrNull { it.role == com.kcalgrindai.app.domain.model.MessageRole.ASSISTANT }
        assertNotNull(assistantMsg)
        assertTrue(assistantMsg!!.structuredDataJson?.contains("crisis_intercepted") == true)
        // Verified emergency & crisis helplines present in response
        assertTrue(assistantMsg.text.contains("112") || assistantMsg.text.contains("988") || assistantMsg.text.contains("911") || assistantMsg.text.contains("116 123"))

        collectJob.cancel()
    }
}
