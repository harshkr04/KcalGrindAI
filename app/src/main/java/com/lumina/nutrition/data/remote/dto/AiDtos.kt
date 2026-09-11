package com.lumina.nutrition.data.remote.dto

import com.lumina.nutrition.domain.model.AIFoodItem
import com.lumina.nutrition.domain.model.AIAnalysisResult
import com.lumina.nutrition.domain.model.AIMacros
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhotoAnalysisRequest(
    @SerialName("imageBase64")
    val imageBase64: String,
    @SerialName("dietTags")
    val dietTags: List<String> = emptyList(),
    @SerialName("allergies")
    val allergies: List<String> = emptyList()
)

@Serializable
data class TextAnalysisRequest(
    @SerialName("text")
    val text: String,
    @SerialName("dietTags")
    val dietTags: List<String> = emptyList(),
    @SerialName("allergies")
    val allergies: List<String> = emptyList()
)

@Serializable
data class TranscribeRequest(
    @SerialName("transcript")
    val transcript: String,
    @SerialName("dietTags")
    val dietTags: List<String> = emptyList(),
    @SerialName("allergies")
    val allergies: List<String> = emptyList()
)

@Serializable
data class FoodAnalysisResponseDto(
    @SerialName("foods")
    val foods: List<AIFoodDto> = emptyList(),
    @SerialName("overallConfidence")
    val overallConfidence: Double = 0.0
)

@Serializable
data class AIFoodDto(
    @SerialName("name")
    val name: String? = null,
    @SerialName("estimatedGrams")
    val estimatedGrams: Double? = null,
    @SerialName("calories")
    val calories: Int? = null,
    @SerialName("macros")
    val macros: MacrosDto? = null,
    @SerialName("confidence")
    val confidence: Double? = null
)

@Serializable
data class MacrosDto(
    @SerialName("protein")
    val protein: Double? = null,
    @SerialName("carbs")
    val carbs: Double? = null,
    @SerialName("fat")
    val fat: Double? = null
)

@Serializable
data class ChatMessageDto(
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: String
)

@Serializable
data class RecentTrendsDto(
    @SerialName("avgCalories")
    val avgCalories: Int? = null,
    @SerialName("targetCalories")
    val targetCalories: Int? = null,
    @SerialName("daysTracked")
    val daysTracked: Int? = null,
    @SerialName("totalDays")
    val totalDays: Int? = null,
    @SerialName("trackingRatePercent")
    val trackingRatePercent: Int? = null,
    @SerialName("avgProteinG")
    val avgProteinG: Double? = null,
    @SerialName("avgCarbsG")
    val avgCarbsG: Double? = null,
    @SerialName("avgFatG")
    val avgFatG: Double? = null,
    @SerialName("weightChangeKg")
    val weightChangeKg: Double? = null
)

@Serializable
data class UserContextDto(
    @SerialName("remainingCalories")
    val remainingCalories: Int? = null,
    @SerialName("consumedCalories")
    val consumedCalories: Int? = null,
    @SerialName("targetCalories")
    val targetCalories: Int? = null,
    @SerialName("goal")
    val goal: String? = null,
    @SerialName("recentTrends")
    val recentTrends: RecentTrendsDto? = null
)

@Serializable
data class ChatRequestDto(
    @SerialName("messages")
    val messages: List<ChatMessageDto>,
    @SerialName("userContext")
    val userContext: UserContextDto? = null
)

@Serializable
data class ChatResponseDto(
    @SerialName("reply")
    val reply: String? = null,
    @SerialName("suggestedAction")
    val suggestedAction: String? = null,
    @SerialName("actionType")
    val actionType: String? = null,
    @SerialName("actionPayload")
    val actionPayload: String? = null
)

@Serializable
data class FoodItemActionDto(
    @SerialName("name")
    val name: String = "Food",
    @SerialName("estimatedGrams")
    val estimatedGrams: Double = 100.0,
    @SerialName("calories")
    val calories: Double = 100.0,
    @SerialName("macros")
    val macros: MacrosDto? = null,
    @SerialName("confidence")
    val confidence: Double = 0.95
)

@Serializable
data class ProposedFoodActionDto(
    @SerialName("mealType")
    val mealType: String = "snack",
    @SerialName("items")
    val items: List<FoodItemActionDto> = emptyList()
)

@Serializable
data class WaterLogActionDto(
    @SerialName("amountMl")
    val amountMl: Int = 250
)

@Serializable
data class SuggestedActionEnvelopeDto(
    @SerialName("type")
    val type: String,
    @SerialName("action")
    val action: String? = null,
    @SerialName("data")
    val data: kotlinx.serialization.json.JsonObject? = null
)

fun FoodAnalysisResponseDto.toDomain(inputType: String = "photo", rawQuery: String? = null): AIAnalysisResult {
    val domainFoods = foods.map { dto ->
        AIFoodItem(
            name = dto.name ?: "Unknown Food",
            estimatedGrams = dto.estimatedGrams ?: 100.0,
            calories = dto.calories ?: 150,
            macros = AIMacros(
                protein = dto.macros?.protein ?: 0.0,
                carbs = dto.macros?.carbs ?: 0.0,
                fat = dto.macros?.fat ?: 0.0
            ),
            confidence = dto.confidence ?: 0.5,
            isSelected = true
        )
    }
    return AIAnalysisResult(
        foods = domainFoods,
        overallConfidence = overallConfidence,
        rawQuery = rawQuery,
        inputType = inputType
    )
}
