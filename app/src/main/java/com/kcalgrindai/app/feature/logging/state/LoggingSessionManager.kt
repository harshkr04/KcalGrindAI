package com.kcalgrindai.app.feature.logging.state

import com.kcalgrindai.app.domain.model.AIFoodItem
import com.kcalgrindai.app.domain.model.AIAnalysisResult
import com.kcalgrindai.app.domain.model.FoodItem
import com.kcalgrindai.app.domain.model.MealType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class LoggingDraft(
    val selectedFood: FoodItem? = null,
    val selectedMealType: MealType = MealType.LUNCH,
    val targetDate: LocalDate? = null,
    val prefilledSearchQuery: String? = null,
    val capturedPhotoPath: String? = null,
    val pendingTranscript: String? = null,
    val pendingAnalysisResult: AIAnalysisResult? = null,
    val candidateFoods: List<AIFoodItem> = emptyList(),
    val loggingSource: String = "manual" // "manual", "barcode", "ai_photo", "ai_voice", "ai_text"
)

@Singleton
class LoggingSessionManager @Inject constructor() {

    private val _state = MutableStateFlow(LoggingDraft())
    val state: StateFlow<LoggingDraft> = _state.asStateFlow()

    fun setTargetDate(date: LocalDate?) {
        _state.update { it.copy(targetDate = date) }
    }

    fun setSelectedFood(food: FoodItem?, mealType: MealType = _state.value.selectedMealType) {
        _state.update { it.copy(selectedFood = food, selectedMealType = mealType, loggingSource = "manual") }
    }

    fun setSelectedMealType(mealType: MealType) {
        _state.update { it.copy(selectedMealType = mealType) }
    }

    fun setPrefilledSearchQuery(query: String?) {
        _state.update { it.copy(prefilledSearchQuery = query) }
    }

    fun setCapturedPhotoPath(path: String?) {
        _state.update { it.copy(capturedPhotoPath = path) }
    }

    fun setPendingTranscript(transcript: String?) {
        _state.update { it.copy(pendingTranscript = transcript) }
    }

    fun setAiAnalysisResult(result: AIAnalysisResult, source: String) {
        _state.update {
            it.copy(
                pendingAnalysisResult = result,
                candidateFoods = result.foods,
                loggingSource = source
            )
        }
    }

    fun updateCandidateFood(index: Int, updated: AIFoodItem) {
        _state.update {
            val list = it.candidateFoods.toMutableList()
            if (index in list.indices) {
                list[index] = updated
            }
            it.copy(candidateFoods = list)
        }
    }

    fun toggleCandidateSelection(index: Int) {
        _state.update {
            val list = it.candidateFoods.toMutableList()
            if (index in list.indices) {
                list[index] = list[index].copy(isSelected = !list[index].isSelected)
            }
            it.copy(candidateFoods = list)
        }
    }

    fun replaceCandidateFood(index: Int, newName: String, estimatedGrams: Double, calories: Int) {
        _state.update {
            val list = it.candidateFoods.toMutableList()
            if (index in list.indices) {
                val old = list[index]
                list[index] = old.copy(
                    name = newName,
                    estimatedGrams = estimatedGrams,
                    calories = calories,
                    confidence = 1.0
                )
            }
            it.copy(candidateFoods = list)
        }
    }

    fun clear() {
        _state.value = LoggingDraft()
    }
}
