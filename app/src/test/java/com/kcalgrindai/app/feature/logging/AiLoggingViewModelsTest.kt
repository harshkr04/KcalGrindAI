package com.kcalgrindai.app.feature.logging

import com.kcalgrindai.app.domain.model.AIFoodItem
import com.kcalgrindai.app.domain.model.AIMacros
import com.kcalgrindai.app.domain.model.FoodItem
import com.kcalgrindai.app.domain.model.ItemSource
import com.kcalgrindai.app.domain.model.LogSource
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.fakes.FakeAIAnalysisRepository
import com.kcalgrindai.app.fakes.FakeMealLogRepository
import com.kcalgrindai.app.fakes.FakePhotoDraftStore
import com.kcalgrindai.app.fakes.FakeUserProfileRepository
import com.kcalgrindai.app.feature.logging.confirm.MealConfirmViewModel
import com.kcalgrindai.app.feature.logging.state.LoggingSessionManager
import com.kcalgrindai.app.feature.logging.text.TextInputViewModel
import com.kcalgrindai.app.feature.logging.voice.VoiceInputViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiLoggingViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var aiRepository: FakeAIAnalysisRepository
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var mealLogRepository: FakeMealLogRepository
    private lateinit var sessionManager: LoggingSessionManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        aiRepository = FakeAIAnalysisRepository()
        userProfileRepository = FakeUserProfileRepository()
        mealLogRepository = FakeMealLogRepository()
        sessionManager = LoggingSessionManager()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testTextInputViewModelSuccessfulAnalysis() = runTest(testDispatcher) {
        val viewModel = TextInputViewModel(aiRepository, userProfileRepository, sessionManager)

        viewModel.onTextChanged("2 scrambled eggs and toast")
        assertEquals("2 scrambled eggs and toast", viewModel.uiState.value.queryText)

        var successCalled = false
        viewModel.analyzeMeal { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertFalse(viewModel.uiState.value.isAnalyzing)
        assertEquals("ai_text", sessionManager.state.value.loggingSource)
        assertEquals(2, sessionManager.state.value.candidateFoods.size)
    }

    @Test
    fun testTextInputViewModelNetworkError() = runTest(testDispatcher) {
        aiRepository.shouldFail = true
        val viewModel = TextInputViewModel(aiRepository, userProfileRepository, sessionManager)

        viewModel.onTextChanged("Chicken salad")
        var successCalled = false
        viewModel.analyzeMeal { successCalled = true }
        advanceUntilIdle()

        assertFalse(successCalled)
        assertFalse(viewModel.uiState.value.isAnalyzing)
        assertTrue(viewModel.uiState.value.isNetworkError)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testVoiceInputViewModelTranscriptAndAnalysis() = runTest(testDispatcher) {
        val viewModel = VoiceInputViewModel(aiRepository, userProfileRepository, sessionManager)

        viewModel.simulateTranscript("Greek yogurt bowl with berries")
        assertEquals("Greek yogurt bowl with berries", viewModel.uiState.value.transcript)

        var successCalled = false
        viewModel.analyzeTranscript { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals("ai_voice", sessionManager.state.value.loggingSource)
        assertEquals(1, sessionManager.state.value.candidateFoods.size)
        assertEquals("Greek Yogurt Bowl", sessionManager.state.value.candidateFoods[0].name)
    }

    @Test
    fun testMealConfirmViewModelPersistsAiPhotoWithCorrectSourceAndConfidence() = runTest(testDispatcher) {
        sessionManager.setAiAnalysisResult(
            result = com.kcalgrindai.app.domain.model.AIAnalysisResult(
                foods = listOf(
                    AIFoodItem("Grilled Salmon Salad", 300.0, 380, AIMacros(32.0, 10.0, 22.0), 0.91)
                ),
                overallConfidence = 0.91,
                inputType = "photo"
            ),
            source = "ai_photo"
        )
        sessionManager.setSelectedMealType(MealType.DINNER)

        val viewModel = MealConfirmViewModel(mealLogRepository, sessionManager, FakePhotoDraftStore())
        assertEquals(MealType.DINNER, viewModel.uiState.value.mealType)
        assertEquals("ai_photo", viewModel.uiState.value.loggingSource)

        var loggedSuccess = false
        viewModel.logMeal { loggedSuccess = true }
        advanceUntilIdle()

        assertTrue(loggedSuccess)
        val savedMeal = mealLogRepository.getMealById(1L)
        assertNotNull(savedMeal)
        assertEquals(LogSource.AI_PHOTO, savedMeal!!.meal.source)
        assertEquals(380.0, savedMeal.meal.totalCalories, 0.001)
        assertEquals(1, savedMeal.items.size)
        assertEquals("Grilled Salmon Salad", savedMeal.items[0].name)
        assertEquals(ItemSource.AI, savedMeal.items[0].source)
        assertEquals(0.91f, savedMeal.items[0].confidence!!, 0.001f)
    }

    @Test
    fun testMealConfirmViewModelPersistsAiVoiceWithCorrectSourceAndConfidence() = runTest(testDispatcher) {
        sessionManager.setAiAnalysisResult(
            result = com.kcalgrindai.app.domain.model.AIAnalysisResult(
                foods = listOf(
                    AIFoodItem("Greek Yogurt Bowl", 200.0, 180, AIMacros(18.0, 12.0, 5.0), 0.94)
                ),
                overallConfidence = 0.94,
                inputType = "voice"
            ),
            source = "ai_voice"
        )
        sessionManager.setSelectedMealType(MealType.BREAKFAST)

        val viewModel = MealConfirmViewModel(mealLogRepository, sessionManager, FakePhotoDraftStore())
        assertEquals("ai_voice", viewModel.uiState.value.loggingSource)

        var loggedSuccess = false
        viewModel.logMeal { loggedSuccess = true }
        advanceUntilIdle()

        assertTrue(loggedSuccess)
        val savedMeal = mealLogRepository.getMealById(1L)
        assertNotNull(savedMeal)
        assertEquals(LogSource.AI_VOICE, savedMeal!!.meal.source)
        assertEquals(1, savedMeal.items.size)
        assertEquals(ItemSource.AI, savedMeal.items[0].source)
        assertEquals(0.94f, savedMeal.items[0].confidence!!, 0.001f)
    }

    @Test
    fun testMealConfirmViewModelPersistsAiTextWithCorrectSourceAndConfidence() = runTest(testDispatcher) {
        sessionManager.setAiAnalysisResult(
            result = com.kcalgrindai.app.domain.model.AIAnalysisResult(
                foods = listOf(
                    AIFoodItem("Pasta with Red Sauce", 350.0, 500, AIMacros(13.5, 87.5, 9.0), 0.65)
                ),
                overallConfidence = 0.65,
                inputType = "text"
            ),
            source = "ai_text"
        )
        sessionManager.setSelectedMealType(MealType.LUNCH)

        val viewModel = MealConfirmViewModel(mealLogRepository, sessionManager, FakePhotoDraftStore())
        assertEquals("ai_text", viewModel.uiState.value.loggingSource)

        var loggedSuccess = false
        viewModel.logMeal { loggedSuccess = true }
        advanceUntilIdle()

        assertTrue(loggedSuccess)
        val savedMeal = mealLogRepository.getMealById(1L)
        assertNotNull(savedMeal)
        assertEquals(LogSource.AI_TEXT, savedMeal!!.meal.source)
        assertEquals(1, savedMeal.items.size)
        assertEquals(ItemSource.AI, savedMeal.items[0].source)
        assertEquals(0.65f, savedMeal.items[0].confidence!!, 0.001f)
    }

    @Test
    fun testMealConfirmViewModelPersistsBarcodeWithVerifiedSourceAndFullConfidence() = runTest(testDispatcher) {
        sessionManager.setSelectedFood(
            FoodItem(
                id = 42L,
                source = com.kcalgrindai.app.domain.model.FoodSource.OPEN_FOOD_FACTS,
                name = "Protein Bar",
                brand = "Quest",
                barcodeUpc = "088884900001",
                servingDescription = "1 bar (60g)",
                servingGrams = 60.0,
                calories = 200.0,
                proteinG = 21.0,
                carbsG = 22.0,
                fatG = 7.0,
                fiberG = 14.0,
                isUserCreated = false,
                createdAt = System.currentTimeMillis()
            ),
            mealType = MealType.SNACK
        )

        val viewModel = MealConfirmViewModel(mealLogRepository, sessionManager, FakePhotoDraftStore())
        var loggedSuccess = false
        viewModel.logMeal { loggedSuccess = true }
        advanceUntilIdle()

        assertTrue(loggedSuccess)
        val savedMeal = mealLogRepository.getMealById(1L)
        assertNotNull(savedMeal)
        assertEquals(1, savedMeal!!.items.size)
        assertEquals(ItemSource.VERIFIED, savedMeal.items[0].source)
        assertEquals(1.0f, savedMeal.items[0].confidence!!, 0.001f)
        assertEquals(42L, savedMeal.items[0].foodId)
    }

    @Test
    fun testMealConfirmViewModelSaveFailurePreservesDraftAndSession() = runTest(testDispatcher) {
        val fakeDraftStore = FakePhotoDraftStore()
        val draftPhoto = "/data/user/0/com.kcalgrindai.app/cache/draft_meal.jpg"
        sessionManager.setCapturedPhotoPath(draftPhoto)
        sessionManager.setAiAnalysisResult(
            result = com.kcalgrindai.app.domain.model.AIAnalysisResult(
                foods = listOf(
                    AIFoodItem("Avocado Toast", 120.0, 280, AIMacros(8.0, 30.0, 15.0), 0.90)
                ),
                overallConfidence = 0.90,
                inputType = "photo"
            ),
            source = "ai_photo"
        )
        mealLogRepository.shouldFail = true

        val viewModel = MealConfirmViewModel(mealLogRepository, sessionManager, fakeDraftStore)
        var successCalled = false
        viewModel.logMeal { successCalled = true }
        advanceUntilIdle()

        // 1. onSuccess must NOT be called
        assertFalse(successCalled)
        // 2. Draft photo must NOT be deleted
        assertTrue("Draft photo must not be deleted on save failure", fakeDraftStore.deleteDraftCalls.isEmpty())
        // 3. Friendly error message set and isSaving reset
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertEquals("Unable to save meal. Please try again.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSaving)
        // 4. Session must remain intact
        assertEquals(draftPhoto, sessionManager.state.value.capturedPhotoPath)
        assertEquals(1, sessionManager.state.value.candidateFoods.size)
    }

    @Test
    fun testMealConfirmViewModelDoubleTapCallsSaveMealOnlyOnce() = runTest(testDispatcher) {
        sessionManager.setAiAnalysisResult(
            result = com.kcalgrindai.app.domain.model.AIAnalysisResult(
                foods = listOf(
                    AIFoodItem("Black Coffee", 250.0, 5, AIMacros(0.5, 0.0, 0.0), 0.99)
                ),
                overallConfidence = 0.99,
                inputType = "text"
            ),
            source = "ai_text"
        )
        val viewModel = MealConfirmViewModel(mealLogRepository, sessionManager, FakePhotoDraftStore())

        var successCount = 0
        // Simulate rapid double-tap
        viewModel.logMeal { successCount++ }
        viewModel.logMeal { successCount++ }
        advanceUntilIdle()

        assertEquals("saveMeal must only be called once on double-tap", 1, mealLogRepository.saveCallCount)
        assertEquals(1, successCount)
    }

    @Test
    fun testMealConfirmViewModelSuccessfulSaveDeletesDraftOnceAndClearsSession() = runTest(testDispatcher) {
        val fakeDraftStore = FakePhotoDraftStore()
        val draftPhoto = "/data/user/0/com.kcalgrindai.app/cache/draft_meal.jpg"
        sessionManager.setCapturedPhotoPath(draftPhoto)
        sessionManager.setAiAnalysisResult(
            result = com.kcalgrindai.app.domain.model.AIAnalysisResult(
                foods = listOf(
                    AIFoodItem("Greek Yogurt", 200.0, 150, AIMacros(15.0, 10.0, 4.0), 0.95)
                ),
                overallConfidence = 0.95,
                inputType = "photo"
            ),
            source = "ai_photo"
        )

        val viewModel = MealConfirmViewModel(mealLogRepository, sessionManager, fakeDraftStore)
        var successCalled = false
        viewModel.logMeal { successCalled = true }
        advanceUntilIdle()

        // 1. onSuccess called
        assertTrue(successCalled)
        // 2. Draft deleted exactly once
        assertEquals(listOf(draftPhoto), fakeDraftStore.deleteDraftCalls)
        // 3. Session cleared
        org.junit.Assert.assertNull(sessionManager.state.value.capturedPhotoPath)
        assertTrue(sessionManager.state.value.candidateFoods.isEmpty())
        // 4. UI state updated
        assertTrue(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.isSaving)
        org.junit.Assert.assertNull(viewModel.uiState.value.errorMessage)
    }
}
