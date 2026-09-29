package com.kcalgrindai.app.domain.usecase

import com.kcalgrindai.app.domain.model.AIAnalysisResult
import com.kcalgrindai.app.domain.repository.AIAnalysisRepository
import com.kcalgrindai.app.domain.repository.UserProfileRepository
import javax.inject.Inject

sealed class PhotoAnalysisException(message: String) : Exception(message) {
    data class PhotoTooLarge(val customMessage: String = "Photo is too large to analyze. Please retake.") : PhotoAnalysisException(customMessage)
    data class InvalidImage(val customMessage: String = "Could not read this photo. Please retake with better lighting.") : PhotoAnalysisException(customMessage)
    data class AiUnavailable(val customMessage: String = "Meal analysis is busy right now. Please try again in a minute.") : PhotoAnalysisException(customMessage)
    data class NetworkError(val customMessage: String = "AI logging needs a connection. Unable to reach AI server.") : PhotoAnalysisException(customMessage)
    data class NoFoodDetected(val customMessage: String = "No food detected in this photo. Please retake with a closer view or search manually.") : PhotoAnalysisException(customMessage)
}

class AnalyzeFoodUseCase @Inject constructor(
    private val aiAnalysisRepository: AIAnalysisRepository,
    private val userProfileRepository: UserProfileRepository
) {

    /**
     * Pure domain use case: fetches user dietary profile, invokes repository to analyze
     * the photo file, drops zero-gram & zero-calorie items, and returns NoFoodDetected if empty.
     */
    suspend operator fun invoke(photoPath: String): Result<AIAnalysisResult> {
        val profile = userProfileRepository.getProfile()
        val dietTags = profile?.dietTags ?: emptyList()
        val allergies = profile?.allergies ?: emptyList()

        val result = aiAnalysisRepository.analyzePhotoFile(
            photoPath = photoPath,
            dietTags = dietTags,
            allergies = allergies
        )

        return result.fold(
            onSuccess = { analysis ->
                val filteredFoods = analysis.foods.filterNot { it.estimatedGrams <= 0.0 && it.calories <= 0 }
                if (filteredFoods.isEmpty()) {
                    Result.failure(PhotoAnalysisException.NoFoodDetected())
                } else {
                    Result.success(analysis.copy(foods = filteredFoods))
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}
