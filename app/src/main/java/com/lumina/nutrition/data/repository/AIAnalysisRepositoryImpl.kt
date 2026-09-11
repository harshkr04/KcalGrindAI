package com.lumina.nutrition.data.repository

import com.lumina.nutrition.data.local.dao.AIAnalysisDao
import com.lumina.nutrition.data.mapper.toDomain
import com.lumina.nutrition.data.mapper.toEntity
import com.lumina.nutrition.data.remote.api.LuminaAiApiService
import com.lumina.nutrition.data.remote.dto.PhotoAnalysisRequest
import com.lumina.nutrition.data.remote.dto.TextAnalysisRequest
import com.lumina.nutrition.data.remote.dto.TranscribeRequest
import com.lumina.nutrition.data.remote.dto.toDomain
import com.lumina.nutrition.domain.model.AIAnalysisRecord
import com.lumina.nutrition.domain.model.AIAnalysisResult
import com.lumina.nutrition.domain.repository.AIAnalysisRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIAnalysisRepositoryImpl @Inject constructor(
    private val aiAnalysisDao: AIAnalysisDao,
    private val aiApiService: LuminaAiApiService
) : AIAnalysisRepository {

    override suspend fun analyzePhoto(
        imageBase64: String,
        dietTags: List<String>,
        allergies: List<String>
    ): Result<AIAnalysisResult> = runCatching {
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
        domain
    }.onFailure { e ->
        com.lumina.nutrition.core.util.LuminaCrashReporter.recordException(e, mapOf("feature" to "ai_analysis", "type" to "photo"))
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
        com.lumina.nutrition.core.util.LuminaCrashReporter.recordException(e, mapOf("feature" to "ai_analysis", "type" to "text"))
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
        com.lumina.nutrition.core.util.LuminaCrashReporter.recordException(e, mapOf("feature" to "ai_analysis", "type" to "voice"))
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
