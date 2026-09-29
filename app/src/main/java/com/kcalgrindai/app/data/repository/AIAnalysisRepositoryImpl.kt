package com.kcalgrindai.app.data.repository

import android.util.Base64
import com.kcalgrindai.app.data.local.dao.AIAnalysisDao
import com.kcalgrindai.app.data.mapper.toDomain
import com.kcalgrindai.app.data.mapper.toEntity
import com.kcalgrindai.app.data.remote.api.KcalGrindAIAiApiService
import com.kcalgrindai.app.data.remote.dto.PhotoAnalysisRequest
import com.kcalgrindai.app.data.remote.dto.TextAnalysisRequest
import com.kcalgrindai.app.data.remote.dto.TranscribeRequest
import com.kcalgrindai.app.data.remote.dto.toDomain
import com.kcalgrindai.app.domain.model.AIAnalysisRecord
import com.kcalgrindai.app.domain.model.AIAnalysisResult
import com.kcalgrindai.app.domain.repository.AIAnalysisRepository
import com.kcalgrindai.app.domain.usecase.PhotoAnalysisException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class AIAnalysisRepositoryImpl @Inject constructor(
    private val aiAnalysisDao: AIAnalysisDao,
    private val aiApiService: KcalGrindAIAiApiService
) : AIAnalysisRepository {

    override suspend fun analyzePhoto(
        imageBase64: String,
        dietTags: List<String>,
        allergies: List<String>
    ): Result<AIAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val response = aiApiService.analyzePhoto(
                PhotoAnalysisRequest(
                    imageBase64 = imageBase64,
                    dietTags = dietTags,
                    allergies = allergies
                )
            )
            val domain = response.toDomain(inputType = "photo")

            // Save raw analysis record to Room
            saveAnalysis(
                AIAnalysisRecord(
                    id = 0L,
                    inputType = "photo",
                    rawInputRef = "image_base64_payload",
                    resultJson = response.toString(),
                    overallConfidence = domain.overallConfidence.toFloat(),
                    createdAt = System.currentTimeMillis()
                )
            )
            Result.success(domain)
        } catch (e: Throwable) {
            if (e is CancellationException || e is kotlinx.coroutines.CancellationException) {
                throw e
            }
            val mapped = mapError(e)
            com.kcalgrindai.app.core.util.KcalGrindAICrashReporter.recordException(
                mapped,
                mapOf("feature" to "ai_analysis", "type" to "photo")
            )
            Result.failure(mapped)
        }
    }

    override suspend fun analyzePhotoFile(
        photoPath: String,
        dietTags: List<String>,
        allergies: List<String>
    ): Result<AIAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val file = File(photoPath)
            if (!file.exists() || !file.isFile || file.length() == 0L) {
                return@withContext Result.failure(
                    PhotoAnalysisException.InvalidImage("Photo file was not found on device.")
                )
            }

            val rawBytes = file.readBytes()
            // Pre-send guard: if file payload > 3 MB, do not send to server
            if (rawBytes.size > 3 * 1024 * 1024) {
                return@withContext Result.failure(
                    PhotoAnalysisException.PhotoTooLarge("Compressed photo exceeds 3 MB limit (${rawBytes.size} bytes).")
                )
            }

            val base64 = Base64.encodeToString(rawBytes, Base64.NO_WRAP)
            if (base64.length > 4 * 1024 * 1024) {
                return@withContext Result.failure(
                    PhotoAnalysisException.PhotoTooLarge("Encoded photo exceeds 4 MB request limit.")
                )
            }

            val response = aiApiService.analyzePhoto(
                PhotoAnalysisRequest(
                    imageBase64 = base64,
                    dietTags = dietTags,
                    allergies = allergies
                )
            )
            val domain = response.toDomain(inputType = "photo")

            // Save raw analysis record to Room
            saveAnalysis(
                AIAnalysisRecord(
                    id = 0L,
                    inputType = "photo",
                    rawInputRef = photoPath,
                    resultJson = response.toString(),
                    overallConfidence = domain.overallConfidence.toFloat(),
                    createdAt = System.currentTimeMillis()
                )
            )
            Result.success(domain)
        } catch (e: Throwable) {
            if (e is CancellationException || e is kotlinx.coroutines.CancellationException) {
                throw e
            }
            val mapped = mapError(e)
            com.kcalgrindai.app.core.util.KcalGrindAICrashReporter.recordException(
                mapped,
                mapOf("feature" to "ai_analysis", "type" to "photo_file")
            )
            Result.failure(mapped)
        }
    }

    private fun mapError(error: Throwable): Throwable {
        return when (error) {
            is PhotoAnalysisException -> error
            is HttpException -> {
                when (error.code()) {
                    401 -> PhotoAnalysisException.InvalidImage("Session expired. Please sign in again.")
                    413 -> PhotoAnalysisException.PhotoTooLarge("Photo too large for meal analysis.")
                    400 -> PhotoAnalysisException.InvalidImage("Unsupported or invalid image. Please retake photo.")
                    429 -> PhotoAnalysisException.AiUnavailable("Rate limit exceeded. Please try again later.")
                    503 -> PhotoAnalysisException.AiUnavailable("Meal analysis is busy right now. Please try again in a minute.")
                    else -> PhotoAnalysisException.NetworkError("Server error (${error.code()}). Please try again.")
                }
            }
            is SocketTimeoutException, is UnknownHostException -> {
                PhotoAnalysisException.NetworkError("Network timeout. Please check your connection and try again.")
            }
            is IOException -> {
                PhotoAnalysisException.NetworkError("Network error. Unable to reach the AI server.")
            }
            else -> error
        }
    }

    override suspend fun analyzeText(
        text: String,
        dietTags: List<String>,
        allergies: List<String>
    ): Result<AIAnalysisResult> = runCatching {
        val response = aiApiService.analyzeText(
            TextAnalysisRequest(
                text = text,
                dietTags = dietTags,
                allergies = allergies
            )
        )
        val domain = response.toDomain(inputType = "text", rawQuery = text)

        saveAnalysis(
            AIAnalysisRecord(
                id = 0L,
                inputType = "text",
                rawInputRef = text,
                resultJson = response.toString(),
                overallConfidence = domain.overallConfidence.toFloat(),
                createdAt = System.currentTimeMillis()
            )
        )
        domain
    }.onFailure { e ->
        com.kcalgrindai.app.core.util.KcalGrindAICrashReporter.recordException(e, mapOf("feature" to "ai_analysis", "type" to "text"))
    }

    override suspend fun transcribeAndAnalyze(
        transcript: String,
        dietTags: List<String>,
        allergies: List<String>
    ): Result<AIAnalysisResult> = runCatching {
        val response = aiApiService.transcribe(
            TranscribeRequest(
                transcript = transcript,
                dietTags = dietTags,
                allergies = allergies
            )
        )
        val domain = response.toDomain(inputType = "voice", rawQuery = transcript)

        saveAnalysis(
            AIAnalysisRecord(
                id = 0L,
                inputType = "voice",
                rawInputRef = transcript,
                resultJson = response.toString(),
                overallConfidence = domain.overallConfidence.toFloat(),
                createdAt = System.currentTimeMillis()
            )
        )
        domain
    }.onFailure { e ->
        com.kcalgrindai.app.core.util.KcalGrindAICrashReporter.recordException(e, mapOf("feature" to "ai_analysis", "type" to "voice"))
    }

    override suspend fun saveAnalysis(analysis: AIAnalysisRecord): Long {
        return aiAnalysisDao.insertAnalysis(analysis.toEntity())
    }

    override suspend fun getAnalysisById(id: Long): AIAnalysisRecord? {
        return aiAnalysisDao.getAnalysisById(id)?.toDomain()
    }

    override fun observeRecentAnalyses(limit: Int): Flow<List<AIAnalysisRecord>> {
        return aiAnalysisDao.observeRecentAnalyses(limit).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun deleteOlderThan(cutoffTimestamp: Long): Int {
        return aiAnalysisDao.deleteOlderThan(cutoffTimestamp)
    }

    override suspend fun clearAll() {
        aiAnalysisDao.deleteAll()
    }
}
