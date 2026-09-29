package com.kcalgrindai.app.domain.repository

import com.kcalgrindai.app.domain.model.AIAnalysisRecord
import com.kcalgrindai.app.domain.model.AIAnalysisResult
import kotlinx.coroutines.flow.Flow

interface AIAnalysisRepository {
    suspend fun analyzePhoto(
        imageBase64: String,
        dietTags: List<String> = emptyList(),
        allergies: List<String> = emptyList()
    ): Result<AIAnalysisResult>

    suspend fun analyzePhotoFile(
        photoPath: String,
        dietTags: List<String> = emptyList(),
        allergies: List<String> = emptyList()
    ): Result<AIAnalysisResult>

    suspend fun analyzeText(
        text: String,
        dietTags: List<String> = emptyList(),
        allergies: List<String> = emptyList()
    ): Result<AIAnalysisResult>

    suspend fun transcribeAndAnalyze(
        transcript: String,
        dietTags: List<String> = emptyList(),
        allergies: List<String> = emptyList()
    ): Result<AIAnalysisResult>

    suspend fun saveAnalysis(analysis: AIAnalysisRecord): Long
    suspend fun getAnalysisById(id: Long): AIAnalysisRecord?
    fun observeRecentAnalyses(limit: Int = 20): Flow<List<AIAnalysisRecord>>
    suspend fun deleteOlderThan(cutoffTimestamp: Long): Int
    suspend fun clearAll()
}
