package com.kcalgrindai.app.data.remote.api

import com.kcalgrindai.app.data.remote.dto.ChatRequestDto
import com.kcalgrindai.app.data.remote.dto.ChatResponseDto
import com.kcalgrindai.app.data.remote.dto.FoodAnalysisResponseDto
import com.kcalgrindai.app.data.remote.dto.PhotoAnalysisRequest
import com.kcalgrindai.app.data.remote.dto.TextAnalysisRequest
import com.kcalgrindai.app.data.remote.dto.TranscribeRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface KcalGrindAIAiApiService {

    @POST("ai/analyze-photo")
    suspend fun analyzePhoto(
        @Body request: PhotoAnalysisRequest
    ): FoodAnalysisResponseDto

    @POST("ai/analyze-text")
    suspend fun analyzeText(
        @Body request: TextAnalysisRequest
    ): FoodAnalysisResponseDto

    @POST("ai/transcribe")
    suspend fun transcribe(
        @Body request: TranscribeRequest
    ): FoodAnalysisResponseDto

    @POST("ai/chat")
    suspend fun chat(
        @Body request: ChatRequestDto
    ): ChatResponseDto
}
