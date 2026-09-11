package com.lumina.nutrition.feature.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.nutrition.domain.model.FoodLogItem
import com.lumina.nutrition.domain.model.MealType
import com.lumina.nutrition.domain.model.MealWithFoodItems
import com.lumina.nutrition.domain.repository.MealLogRepository
import com.lumina.nutrition.domain.repository.NutritionGoalRepository
import com.lumina.nutrition.feature.home.MealCategorySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository,
    private val nutritionGoalRepository: NutritionGoalRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _recentlyDeletedItem = MutableStateFlow<FoodLogItem?>(null)

    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

    val uiState: StateFlow<DiaryUiState> = _selectedDate.flatMapLatest { date ->
        combine(
            mealLogRepository.observeMealsWithItemsByDate(date.toString()),
            nutritionGoalRepository.observeLatestGoal(),
            _recentlyDeletedItem
        ) { mealsWithItems, goal, deletedItem ->
            val targetCalories = goal?.calories ?: 2000

            val validMealsWithItems = mealsWithItems.filter { it.items.isNotEmpty() }
            var totalCalories = 0.0
            var totalProtein = 0.0
            var totalCarbs = 0.0
            var totalFat = 0.0

            validMealsWithItems.forEach { mealWithItems ->
                totalCalories += mealWithItems.items.sumOf { it.calories }
                mealWithItems.items.forEach { item ->
                    totalProtein += item.proteinG
                    totalCarbs += item.carbsG
                    totalFat += item.fatG
                }
            }

            val grouped = MealType.entries.map { type ->
                val mealsForType = validMealsWithItems.filter { it.meal.mealType == type }
                val cals = mealsForType.sumOf { meal -> meal.items.sumOf { it.calories } }
                val itemsCount = mealsForType.sumOf { it.items.size }
                MealCategorySummary(
                    mealType = type,
                    totalCalories = cals,
                    meals = mealsForType,
                    itemCount = itemsCount
                )
            }

            val today = LocalDate.now()
            val formattedDate = when {
                date == today -> "Today, ${date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))}"
                date == today.minusDays(1) -> "Yesterday, ${date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))}"
                date == today.plusDays(1) -> "Tomorrow, ${date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))}"
                else -> date.format(dateFormatter)
            }

            DiaryUiState(
                isLoading = false,
                selectedDate = date,
                selectedDateFormatted = formattedDate,
                isToday = date == today,
                targetCalories = targetCalories,
                totalCalories = totalCalories.roundToInt(),
                totalProteinG = totalProtein,
                totalCarbsG = totalCarbs,
                totalFatG = totalFat,
                mealsGrouped = grouped,
                hasMeals = validMealsWithItems.isNotEmpty(),
                recentlyDeletedItem = deletedItem,
                undoMessage = if (deletedItem != null) "Deleted ${deletedItem.name}" else null
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DiaryUiState()
    )

    fun previousDay() {
        _selectedDate.update { it.minusDays(1) }
    }

    fun nextDay() {
        _selectedDate.update { it.plusDays(1) }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun resetToToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun deleteFoodItem(item: FoodLogItem) {
        viewModelScope.launch {
            _recentlyDeletedItem.value = item
            mealLogRepository.deleteFoodLogItem(item.id)

            // Recalculate meal total calories
            val meal = mealLogRepository.getMealById(item.mealLogId)
            if (meal != null) {
                val newTotal = meal.items.filter { it.id != item.id }.sumOf { it.calories }
                mealLogRepository.updateMeal(meal.meal.copy(totalCalories = newTotal))
            }
        }
    }

    fun undoDelete() {
        val itemToRestore = _recentlyDeletedItem.value ?: return
        viewModelScope.launch {
            mealLogRepository.saveFoodLogItem(itemToRestore)
            val meal = mealLogRepository.getMealById(itemToRestore.mealLogId)
            if (meal != null) {
                val newTotal = meal.items.sumOf { it.calories }
                mealLogRepository.updateMeal(meal.meal.copy(totalCalories = newTotal))
            }
            _recentlyDeletedItem.value = null
        }
    }

    fun clearUndo() {
        _recentlyDeletedItem.value = null
    }

    fun deleteMeal(mealId: Long) {
        viewModelScope.launch {
            mealLogRepository.deleteMeal(mealId)
        }
    }
}
