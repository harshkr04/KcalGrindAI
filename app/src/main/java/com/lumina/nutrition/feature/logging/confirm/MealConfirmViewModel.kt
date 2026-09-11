package com.lumina.nutrition.feature.logging.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.nutrition.domain.model.FoodLogItem
import com.lumina.nutrition.domain.model.ItemSource
import com.lumina.nutrition.domain.model.LogSource
import com.lumina.nutrition.domain.model.MealLog
import com.lumina.nutrition.domain.model.MealType
import com.lumina.nutrition.domain.repository.MealLogRepository
import com.lumina.nutrition.feature.logging.state.LoggingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class MealConfirmUiState(
    val mealType: MealType = MealType.LUNCH,
    val items: List<FoodLogItem> = emptyList(),
    val totalCalories: Int = 0,
    val loggingSource: String = "manual",
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false
)

@HiltViewModel
class MealConfirmViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository,
    private val sessionManager: LoggingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealConfirmUiState())
    val uiState: StateFlow<MealConfirmUiState> = _uiState.asStateFlow()

    init {
        loadFromSession()
    }

    private fun loadFromSession() {
        val draft = sessionManager.state.value
        val logItems = mutableListOf<FoodLogItem>()

        if (draft.candidateFoods.isNotEmpty()) {
            val selected = draft.candidateFoods.filter { it.isSelected }
            selected.forEach { aiFood ->
                logItems.add(
                    FoodLogItem(
                        id = 0L,
                        mealLogId = 0L,
                        foodId = null,
                        name = aiFood.name,
                        brand = null,
                        servingDescription = "1 portion (${aiFood.estimatedGrams.toInt()}g)",
                        servingGrams = aiFood.estimatedGrams,
                        calories = aiFood.calories.toDouble(),
                        proteinG = aiFood.macros.protein,
                        carbsG = aiFood.macros.carbs,
                        fatG = aiFood.macros.fat,
                        fiberG = 0.0,
                        source = ItemSource.AI,
                        confidence = aiFood.confidence.toFloat(),
                        confirmed = true
                    )
                )
            }
        } else if (draft.selectedFood != null) {
            val food = draft.selectedFood
            logItems.add(
                FoodLogItem(
                    id = 0L,
                    mealLogId = 0L,
                    foodId = food.id,
                    name = food.name,
                    brand = food.brand,
                    servingDescription = food.servingDescription,
                    servingGrams = food.servingGrams,
                    calories = food.calories.toDouble(),
                    proteinG = food.proteinG,
                    carbsG = food.carbsG,
                    fatG = food.fatG,
                    fiberG = food.fiberG,
                    source = if (food.barcodeUpc != null) ItemSource.VERIFIED else ItemSource.MANUAL,
                    confidence = 1.0f,
                    confirmed = true
                )
            )
        }

        val totalCals = logItems.sumOf { it.calories }.toInt()
        _uiState.update {
            it.copy(
                mealType = draft.selectedMealType,
                items = logItems,
                totalCalories = totalCals,
                loggingSource = draft.loggingSource
            )
        }
    }

    fun setMealType(mealType: MealType) {
        _uiState.update { it.copy(mealType = mealType) }
        sessionManager.setSelectedMealType(mealType)
    }

    fun removeItem(index: Int) {
        _uiState.update {
            val list = it.items.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            it.copy(items = list, totalCalories = list.sumOf { item -> item.calories }.toInt())
        }
    }

    fun logMeal(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        if (currentState.items.isEmpty()) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val now = System.currentTimeMillis()

            val logSource = when (currentState.loggingSource) {
                "ai_photo" -> LogSource.AI_PHOTO
                "ai_voice" -> LogSource.AI_VOICE
                "ai_text" -> LogSource.AI_TEXT
                "barcode" -> LogSource.BARCODE
                else -> LogSource.MANUAL
            }

            val meal = MealLog(
                id = 0L,
                date = todayDate,
                mealType = currentState.mealType,
                totalCalories = currentState.totalCalories.toDouble(),
                loggedAt = now,
                source = logSource,
                synced = false
            )

            mealLogRepository.saveMeal(meal, currentState.items)
            sessionManager.clear()
            _uiState.update { it.copy(isSaving = false, isSuccess = true) }
            onSuccess()
        }
    }
}
