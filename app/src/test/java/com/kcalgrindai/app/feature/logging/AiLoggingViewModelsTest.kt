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
}
