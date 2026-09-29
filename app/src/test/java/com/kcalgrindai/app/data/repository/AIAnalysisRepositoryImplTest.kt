package com.kcalgrindai.app.data.repository

import com.kcalgrindai.app.data.local.dao.AIAnalysisDao
import com.kcalgrindai.app.data.local.entity.AIAnalysisEntity
import com.kcalgrindai.app.data.remote.api.KcalGrindAIAiApiService
import com.kcalgrindai.app.data.remote.dto.AIFoodDto
import com.kcalgrindai.app.data.remote.dto.ChatRequestDto
import com.kcalgrindai.app.data.remote.dto.ChatResponseDto
import com.kcalgrindai.app.data.remote.dto.FoodAnalysisResponseDto
import com.kcalgrindai.app.data.remote.dto.MacrosDto
import com.kcalgrindai.app.data.remote.dto.PhotoAnalysisRequest
import com.kcalgrindai.app.data.remote.dto.TextAnalysisRequest
import com.kcalgrindai.app.data.remote.dto.TranscribeRequest
import com.kcalgrindai.app.domain.usecase.PhotoAnalysisException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException

@RunWith(RobolectricTestRunner::class)
class AIAnalysisRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var fakeDao: FakeAnalysisDao
    private lateinit var fakeApiService: FakeApiService
    private lateinit var repository: AIAnalysisRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeAnalysisDao()
        fakeApiService = FakeApiService()
        repository = AIAnalysisRepositoryImpl(fakeDao, fakeApiService)
    }

    @Test
    fun testNonExistentFileReturnsInvalidImage() = runTest {
        val nonExistentPath = File(tempFolder.root, "does_not_exist.jpg").absolutePath
        val result = repository.analyzePhotoFile(nonExistentPath)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PhotoAnalysisException.InvalidImage)
    }

    @Test
    fun testFileExceeding3MBReturnsPhotoTooLarge() = runTest {
        val largeFile = tempFolder.newFile("huge.jpg").apply {
            writeBytes(ByteArray(3 * 1024 * 1024 + 100))
        }

        val result = repository.analyzePhotoFile(largeFile.absolutePath)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PhotoAnalysisException.PhotoTooLarge)
    }

    @Test
    fun testValidFileSuccessfulAnalysisSavedToDao() = runTest {
        val photoFile = tempFolder.newFile("meal.jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }

        fakeApiService.photoResponse = FoodAnalysisResponseDto(
            foods = listOf(
                AIFoodDto(
                    name = "Poha",
                    estimatedGrams = 150.0,
                    calories = 250,
                    macros = MacrosDto(protein = 4.0, carbs = 45.0, fat = 6.0),
                    confidence = 0.92
                )
            ),
            overallConfidence = 0.92
        )

        val result = repository.analyzePhotoFile(photoFile.absolutePath)
        assertTrue(result.isSuccess)
        val domain = result.getOrThrow()
        assertEquals(1, domain.foods.size)
        assertEquals("Poha", domain.foods[0].name)
        assertEquals(1, fakeDao.savedEntities.size)
    }

    @Test
    fun testHttp401MapsToInvalidImageSessionExpired() = runTest {
        val photoFile = tempFolder.newFile("photo401.jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }
        fakeApiService.throwError = createHttpException(401, "Unauthorized")

        val result = repository.analyzePhotoFile(photoFile.absolutePath)
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is PhotoAnalysisException.InvalidImage)
        assertTrue(error?.message?.contains("Session expired") == true)
    }

    @Test
    fun testHttp429MapsToAiUnavailableRateLimited() = runTest {
        val photoFile = tempFolder.newFile("photo429.jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }
        fakeApiService.throwError = createHttpException(429, "Too Many Requests")

        val result = repository.analyzePhotoFile(photoFile.absolutePath)
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is PhotoAnalysisException.AiUnavailable)
        assertTrue(error?.message?.contains("Rate limit") == true)
    }

    @Test
    fun testHttp503MapsToAiUnavailable() = runTest {
        val photoFile = tempFolder.newFile("photo503.jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }
        fakeApiService.throwError = createHttpException(503, "Service Unavailable")

        val result = repository.analyzePhotoFile(photoFile.absolutePath)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PhotoAnalysisException.AiUnavailable)
    }

    @Test
    fun testHttp413MapsToPhotoTooLarge() = runTest {
        val photoFile = tempFolder.newFile("photo413.jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }
        fakeApiService.throwError = createHttpException(413, "Payload Too Large")

        val result = repository.analyzePhotoFile(photoFile.absolutePath)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PhotoAnalysisException.PhotoTooLarge)
    }

    @Test
    fun testHttp400MapsToInvalidImage() = runTest {
        val photoFile = tempFolder.newFile("photo400.jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }
        fakeApiService.throwError = createHttpException(400, "Bad Request")

        val result = repository.analyzePhotoFile(photoFile.absolutePath)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PhotoAnalysisException.InvalidImage)
    }

    @Test
    fun testNetworkTimeoutMapsToNetworkError() = runTest {
        val photoFile = tempFolder.newFile("photoTimeout.jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }
        fakeApiService.throwError = SocketTimeoutException("Read timed out")

        val result = repository.analyzePhotoFile(photoFile.absolutePath)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PhotoAnalysisException.NetworkError)
    }

    @Test
    fun testRethrowsCancellationException() = runTest {
        val photoFile = tempFolder.newFile("photoCancel.jpg").apply {
            writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        }
        fakeApiService.throwError = CancellationException("Coroutine was cancelled")

        try {
            repository.analyzePhotoFile(photoFile.absolutePath)
            fail("Expected CancellationException to be rethrown")
        } catch (e: CancellationException) {
            assertEquals("Coroutine was cancelled", e.message)
        }
    }

    private fun createHttpException(code: Int, message: String): HttpException {
        val body = "{\"error\":\"$message\"}".toResponseBody("application/json".toMediaTypeOrNull())
        val response = Response.error<Any>(code, body)
        return HttpException(response)
    }

    private class FakeAnalysisDao : AIAnalysisDao {
        val savedEntities = mutableListOf<AIAnalysisEntity>()

        override suspend fun insertAnalysis(analysis: AIAnalysisEntity): Long {
            savedEntities.add(analysis)
            return savedEntities.size.toLong()
        }

        override suspend fun getAnalysisById(id: Long): AIAnalysisEntity? =
            savedEntities.find { it.id == id }

        override suspend fun getAllAnalyses(): List<AIAnalysisEntity> = savedEntities

        override fun observeRecentAnalyses(limit: Int): Flow<List<AIAnalysisEntity>> =
            emptyFlow()

        override suspend fun deleteOlderThan(cutoffTimestamp: Long): Int = 0

        override suspend fun deleteAll() {
            savedEntities.clear()
        }
    }

    private class FakeApiService : KcalGrindAIAiApiService {
        var photoResponse: FoodAnalysisResponseDto? = null
        var throwError: Throwable? = null

        override suspend fun analyzePhoto(request: PhotoAnalysisRequest): FoodAnalysisResponseDto {
            throwError?.let { throw it }
            return photoResponse ?: FoodAnalysisResponseDto(emptyList(), 0.0)
        }

        override suspend fun analyzeText(request: TextAnalysisRequest): FoodAnalysisResponseDto {
            throwError?.let { throw it }
            return FoodAnalysisResponseDto(emptyList(), 0.0)
        }

        override suspend fun transcribe(request: TranscribeRequest): FoodAnalysisResponseDto {
            throwError?.let { throw it }
            return FoodAnalysisResponseDto(emptyList(), 0.0)
        }

        override suspend fun chat(request: ChatRequestDto): ChatResponseDto {
            throwError?.let { throw it }
            return ChatResponseDto(
                reply = "response"
            )
        }
    }
}
