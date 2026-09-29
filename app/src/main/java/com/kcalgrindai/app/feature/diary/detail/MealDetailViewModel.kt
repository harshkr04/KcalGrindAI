package com.kcalgrindai.app.feature.diary.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.LogSource
import com.kcalgrindai.app.domain.model.MealLog
import com.kcalgrindai.app.domain.model.MealWithFoodItems
import com.kcalgrindai.app.domain.repository.MealLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MealDetailViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealDetailUiState())
    val uiState: StateFlow<MealDetailUiState> = _uiState.asStateFlow()

    private var currentMealId: Long = 0L

    fun loadMeal(mealId: Long) {
        currentMealId = mealId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val mealWithItems = mealLogRepository.getMealById(mealId)
            if (mealWithItems != null) {
                updateStateWithMeal(mealWithItems)
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Meal not found") }
            }
        }
    }

    private fun updateStateWithMeal(mealWithItems: MealWithFoodItems) {
        val totalCals = mealWithItems.meal.totalCalories
        val totalP = mealWithItems.items.sumOf { it.proteinG }
        val totalC = mealWithItems.items.sumOf { it.carbsG }
        val totalF = mealWithItems.items.sumOf { it.fatG }

        val loggedDateTime = Instant.ofEpochMilli(mealWithItems.meal.loggedAt)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMM d, yyyy  •  h:mm a", Locale.getDefault()))

        val sourceLabel = when (mealWithItems.meal.source) {
            LogSource.AI_PHOTO -> "AI Photo Log"
            LogSource.AI_VOICE -> "AI Voice Log"
            LogSource.AI_TEXT -> "AI Text Log"
            LogSource.BARCODE -> "Barcode Scan"
            LogSource.MANUAL -> "Manual Log"
            LogSource.SEARCH -> "Manual Search"
            LogSource.RECIPE, LogSource.CURATED_RECIPE -> "Curated Recipe"
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                mealWithItems = mealWithItems,
                totalCalories = totalCals,
                totalProteinG = totalP,
                totalCarbsG = totalC,
                totalFatG = totalF,
                formattedDate = loggedDateTime,
                sourceLabel = sourceLabel
            )
        }
    }

    fun duplicateMealToToday(onSuccess: (Long) -> Unit) {
        val current = _uiState.value.mealWithItems ?: return
        viewModelScope.launch {
            val newMeal = MealLog(
                id = 0L,
                date = LocalDate.now().toString(),
                mealType = current.meal.mealType,
                totalCalories = current.meal.totalCalories,
                loggedAt = System.currentTimeMillis(),
                source = current.meal.source,
                synced = false
            )
            val newItems = current.items.map { item ->
                item.copy(id = 0L, mealLogId = 0L)
            }
            val newId = mealLogRepository.saveMeal(newMeal, newItems)
            _uiState.update { it.copy(isDuplicated = true) }
            onSuccess(newId)
        }
    }

    fun deleteMeal(onDeleted: () -> Unit) {
        if (currentMealId == 0L) return
        viewModelScope.launch {
            mealLogRepository.deleteMeal(currentMealId)
            _uiState.update { it.copy(isDeleted = true) }
            onDeleted()
        }
    }

    fun deleteFoodItem(item: FoodLogItem) {
        viewModelScope.launch {
            mealLogRepository.deleteFoodLogItem(item.id)
            val updatedMeal = mealLogRepository.getMealById(currentMealId)
            if (updatedMeal != null) {
                val newTotal = updatedMeal.items.sumOf { it.calories }
                mealLogRepository.updateMeal(updatedMeal.meal.copy(totalCalories = newTotal))
                val refreshed = mealLogRepository.getMealById(currentMealId)
                if (refreshed != null) {
                    updateStateWithMeal(refreshed)
                }
            }
        }
    }
}
