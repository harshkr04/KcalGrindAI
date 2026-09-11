package com.lumina.nutrition.data.remote.api

import com.lumina.nutrition.data.remote.dto.ChatRequestDto
import com.lumina.nutrition.data.remote.dto.ChatResponseDto
import com.lumina.nutrition.data.remote.dto.FoodAnalysisResponseDto
import com.lumina.nutrition.data.remote.dto.PhotoAnalysisRequest
import com.lumina.nutrition.data.remote.dto.TextAnalysisRequest
import com.lumina.nutrition.data.remote.dto.TranscribeRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface LuminaAiApiService {

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
