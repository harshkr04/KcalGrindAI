package com.kcalgrindai.app.feature.logging.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.domain.model.FoodItem
import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.ItemSource
import com.kcalgrindai.app.domain.model.LogSource
import com.kcalgrindai.app.domain.model.MealLog
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.repository.FoodRepository
import com.kcalgrindai.app.domain.repository.MealLogRepository
import com.kcalgrindai.app.feature.logging.state.LoggingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.roundToInt

data class FoodDetailUiState(
    val food: FoodItem? = null,
    val servingsMultiplier: Double = 1.0,
    val selectedMealType: MealType = MealType.LUNCH,
    val isLogging: Boolean = false,
    val isLoggedSuccess: Boolean = false
) {
    val scaledCalories: Int
        get() = food?.let { (it.calories * servingsMultiplier).roundToInt() } ?: 0

    val scaledProtein: Double
        get() = food?.let { ((it.proteinG * servingsMultiplier * 10.0).roundToInt() / 10.0) } ?: 0.0

    val scaledCarbs: Double
        get() = food?.let { ((it.carbsG * servingsMultiplier * 10.0).roundToInt() / 10.0) } ?: 0.0

    val scaledFat: Double
        get() = food?.let { ((it.fatG * servingsMultiplier * 10.0).roundToInt() / 10.0) } ?: 0.0

    val scaledFiber: Double
        get() = food?.let { ((it.fiberG * servingsMultiplier * 10.0).roundToInt() / 10.0) } ?: 0.0

    val scaledGrams: Double
        get() = food?.let { ((it.servingGrams * servingsMultiplier * 10.0).roundToInt() / 10.0) } ?: 100.0
}

@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val mealLogRepository: MealLogRepository,
    private val loggingSessionManager: LoggingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodDetailUiState())
    val uiState: StateFlow<FoodDetailUiState> = _uiState.asStateFlow()

    init {
        val draft = loggingSessionManager.state.value
        _uiState.update {
            it.copy(
                food = draft.selectedFood,
                selectedMealType = draft.selectedMealType
            )
        }
    }

    fun setServings(multiplier: Double) {
        if (multiplier > 0.0) {
            _uiState.update { it.copy(servingsMultiplier = multiplier) }
        }
    }

    fun setMealType(mealType: MealType) {
        _uiState.update { it.copy(selectedMealType = mealType) }
        loggingSessionManager.setSelectedMealType(mealType)
    }

    fun logFood(onSuccess: () -> Unit) {
        val state = _uiState.value
        val food = state.food ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLogging = true) }

            val persistedFoodId = if (food.id == 0L) {
                foodRepository.saveFood(food)
            } else {
                food.id
            }

            val targetDate = loggingSessionManager.state.value.targetDate ?: LocalDate.now()
            val targetDateString = targetDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val existingMeals = mealLogRepository.observeMealsWithItemsByDate(targetDateString).firstOrNull()
            val targetMealWithItems = existingMeals?.find { it.meal.mealType == state.selectedMealType }

            val addedCalories = state.scaledCalories.toDouble()

            if (targetMealWithItems != null) {
                val logItem = FoodLogItem(
                    id = 0L,
                    mealLogId = targetMealWithItems.meal.id,
                    foodId = persistedFoodId,
                    name = food.name,
                    brand = food.brand,
                    servingDescription = "${state.scaledGrams}g (${state.servingsMultiplier}x ${food.servingDescription})",
                    servingGrams = state.scaledGrams,
                    calories = addedCalories,
                    proteinG = state.scaledProtein,
                    carbsG = state.scaledCarbs,
                    fatG = state.scaledFat,
                    fiberG = state.scaledFiber,
                    source = ItemSource.VERIFIED,
                    confidence = 1.0f,
                    confirmed = true
                )
                mealLogRepository.saveFoodLogItem(logItem)
                mealLogRepository.updateMeal(
                    targetMealWithItems.meal.copy(totalCalories = targetMealWithItems.meal.totalCalories + addedCalories)
                )
            } else {
                val newMeal = MealLog(
                    id = 0L,
                    date = targetDateString,
                    mealType = state.selectedMealType,
                    totalCalories = addedCalories,
                    loggedAt = System.currentTimeMillis(),
                    source = LogSource.SEARCH,
                    synced = false
                )
                val logItem = FoodLogItem(
                    id = 0L,
                    mealLogId = 0L,
                    foodId = persistedFoodId,
                    name = food.name,
                    brand = food.brand,
                    servingDescription = "${state.scaledGrams}g (${state.servingsMultiplier}x ${food.servingDescription})",
                    servingGrams = state.scaledGrams,
                    calories = addedCalories,
                    proteinG = state.scaledProtein,
                    carbsG = state.scaledCarbs,
                    fatG = state.scaledFat,
                    fiberG = state.scaledFiber,
                    source = ItemSource.VERIFIED,
                    confidence = 1.0f,
                    confirmed = true
                )
                mealLogRepository.saveMeal(newMeal, listOf(logItem))
            }

            _uiState.update { it.copy(isLogging = false, isLoggedSuccess = true) }
            onSuccess()
        }
    }
}
