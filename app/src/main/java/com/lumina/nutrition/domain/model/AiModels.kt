package com.lumina.nutrition.domain.model

data class AIMacros(
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0
)

data class AIFoodItem(
    val name: String,
    val estimatedGrams: Double,
    val calories: Int,
    val macros: AIMacros,
    val confidence: Double,
    val isSelected: Boolean = true
) {
    val confidenceLevel: ConfidenceLevel
        get() = when {
            confidence >= 0.85 -> ConfidenceLevel.HIGH
            confidence >= 0.50 -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
}

enum class ConfidenceLevel {
    HIGH,     // >= 0.85 (Likely correct, pre-checked, editable)
    MEDIUM,   // 0.50 - 0.84 (Flagged "please confirm")
    LOW       // < 0.50 ("AI wasn't sure", force disambiguation / replace)
}

data class AIAnalysisResult(
    val foods: List<AIFoodItem>,
    val overallConfidence: Double,
    val rawQuery: String? = null,
    val inputType: String = "photo" // photo, text, voice
)

data class AIChatMessage(
    val id: Long = 0L,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AIChatResponse(
    val reply: String,
    val suggestedAction: String? = null
)
