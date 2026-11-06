package com.kcalgrindai.app.feature.logging.savedmeals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.model.SavedMealWithItems
import com.kcalgrindai.app.domain.repository.SavedMealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedMealsUiState(
    val savedMeals: List<SavedMealWithItems> = emptyList(),
    val isLoading: Boolean = true,
    val renamingMeal: SavedMealWithItems? = null,
    val deletingMeal: SavedMealWithItems? = null,
    val loggingMeal: SavedMealWithItems? = null,
    val userMessage: String? = null
)

@HiltViewModel
class SavedMealsViewModel @Inject constructor(
    private val savedMealRepository: SavedMealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedMealsUiState())
    val uiState: StateFlow<SavedMealsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            savedMealRepository.observeSavedMeals().collect { meals ->
                _uiState.update { it.copy(savedMeals = meals, isLoading = false) }
            }
        }
    }

    fun promptRename(meal: SavedMealWithItems) {
        _uiState.update { it.copy(renamingMeal = meal) }
    }

    fun dismissRename() {
        _uiState.update { it.copy(renamingMeal = null) }
    }

    fun confirmRename(newName: String) {
        val meal = _uiState.value.renamingMeal ?: return
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            savedMealRepository.renameSavedMeal(meal.savedMeal.id, trimmed)
            _uiState.update { it.copy(renamingMeal = null, userMessage = "Renamed to \"$trimmed\"") }
        }
    }

    fun promptDelete(meal: SavedMealWithItems) {
        _uiState.update { it.copy(deletingMeal = meal) }
    }

    fun dismissDelete() {
        _uiState.update { it.copy(deletingMeal = null) }
    }

    fun confirmDelete() {
        val meal = _uiState.value.deletingMeal ?: return
        viewModelScope.launch {
            savedMealRepository.deleteSavedMeal(meal.savedMeal.id)
            _uiState.update { it.copy(deletingMeal = null, userMessage = "Deleted \"${meal.savedMeal.name}\"") }
        }
    }

    fun promptLogMeal(meal: SavedMealWithItems) {
        _uiState.update { it.copy(loggingMeal = meal) }
    }

    fun dismissLogMeal() {
        _uiState.update { it.copy(loggingMeal = null) }
    }

    fun confirmLogMeal(targetDate: String, targetSlot: MealType, onLogged: () -> Unit) {
        val meal = _uiState.value.loggingMeal ?: return
        viewModelScope.launch {
            savedMealRepository.logSavedMeal(
                savedMealId = meal.savedMeal.id,
                targetDate = targetDate,
                targetMealType = targetSlot
            )
            _uiState.update { it.copy(loggingMeal = null, userMessage = "Logged \"${meal.savedMeal.name}\" to Diary") }
            onLogged()
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
