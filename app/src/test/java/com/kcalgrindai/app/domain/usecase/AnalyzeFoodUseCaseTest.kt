package com.kcalgrindai.app.domain.usecase

import com.kcalgrindai.app.domain.model.AIFoodItem
import com.kcalgrindai.app.domain.model.AIAnalysisResult
import com.kcalgrindai.app.domain.model.AIMacros
import com.kcalgrindai.app.fakes.FakeAIAnalysisRepository
import com.kcalgrindai.app.fakes.FakeUserProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class AnalyzeFoodUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var aiRepository: FakeAIAnalysisRepository
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var useCase: AnalyzeFoodUseCase

    @Before
    fun setUp() {
        aiRepository = FakeAIAnalysisRepository()
        userProfileRepository = FakeUserProfileRepository()
        useCase = AnalyzeFoodUseCase(aiRepository, userProfileRepository)
    }

    @Test
    fun testDropsZeroGramZeroCalorieItemsAndReturnsNoFoodDetected() = runTest {
        val testFile = tempFolder.newFile("photo.jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0x00, 0x10))
        }

        // Mock backend returning empty plate placeholder item
        val dummyEmptyItem = AIFoodItem(
            name = "Empty Plate / No Food Detected",
            estimatedGrams = 0.0,
            calories = 0,
            macros = AIMacros(0.0, 0.0, 0.0),
            confidence = 0.1
        )
        aiRepository.mockPhotoResult = AIAnalysisResult(
            foods = listOf(dummyEmptyItem),
            overallConfidence = 0.1
        )

        val result = useCase(testFile.absolutePath)
        assertTrue(result.isFailure)
        assertTrue(
            "Expected 0g/0kcal placeholder item to result in NoFoodDetected exception",
            result.exceptionOrNull() is PhotoAnalysisException.NoFoodDetected
        )
    }

    @Test
    fun testEmptyFoodsListReturnsNoFoodDetected() = runTest {
        val testFile = tempFolder.newFile("photo_empty.jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }

        aiRepository.mockPhotoResult = AIAnalysisResult(
            foods = emptyList(),
            overallConfidence = 0.0
        )

        val result = useCase(testFile.absolutePath)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PhotoAnalysisException.NoFoodDetected)
    }

    @Test
    fun testRetainsValidFoodItems() = runTest {
        val testFile = tempFolder.newFile("photo_meal.jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }

        val validItem = AIFoodItem(
            name = "Roti",
            estimatedGrams = 60.0,
            calories = 180,
            macros = AIMacros(5.0, 30.0, 4.0),
            confidence = 0.95
        )
        val dummyEmptyItem = AIFoodItem(
            name = "Empty Plate / No Food Detected",
            estimatedGrams = 0.0,
            calories = 0,
            macros = AIMacros(0.0, 0.0, 0.0),
            confidence = 0.1
        )
        aiRepository.mockPhotoResult = AIAnalysisResult(
            foods = listOf(validItem, dummyEmptyItem),
            overallConfidence = 0.95
        )

        val result = useCase(testFile.absolutePath)
        assertTrue(result.isSuccess)
        val analysis = result.getOrThrow()
        assertEquals(1, analysis.foods.size)
        assertEquals("Roti", analysis.foods.first().name)
        assertEquals(180, analysis.foods.first().calories)
    }

    @Test
    fun testRepositoryFailurePropagated() = runTest {
        aiRepository.mockPhotoFileFailure = PhotoAnalysisException.PhotoTooLarge("Compressed photo exceeds 3 MB limit")

        val result = useCase("large_photo.jpg")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PhotoAnalysisException.PhotoTooLarge)
    }

    @Test
    fun testRepositoryInvalidImageFailurePropagated() = runTest {
        aiRepository.mockPhotoFileFailure = PhotoAnalysisException.InvalidImage("Photo file was not found on device.")

        val result = useCase("non_existent.jpg")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PhotoAnalysisException.InvalidImage)
    }

    @Test
    fun testRepositoryAiUnavailableFailurePropagated() = runTest {
        aiRepository.mockPhotoFileFailure = PhotoAnalysisException.AiUnavailable("Rate limit exceeded.")

        val result = useCase("photo.jpg")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PhotoAnalysisException.AiUnavailable)
    }
}
