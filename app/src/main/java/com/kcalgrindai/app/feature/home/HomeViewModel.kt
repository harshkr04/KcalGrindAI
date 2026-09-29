package com.kcalgrindai.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.data.health.HealthConnectActivityState
import com.kcalgrindai.app.data.health.HealthConnectManager
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.model.MealWithFoodItems
import com.kcalgrindai.app.domain.repository.MealLogRepository
import com.kcalgrindai.app.domain.repository.NutritionGoalRepository
import com.kcalgrindai.app.domain.repository.UserProfileRepository
import com.kcalgrindai.app.domain.repository.WaterLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository,
    private val nutritionGoalRepository: NutritionGoalRepository,
    private val userProfileRepository: UserProfileRepository,
    private val waterLogRepository: WaterLogRepository,
    val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val today: LocalDate get() = LocalDate.now()
    private val todayIsoString: String get() = today.toString()

    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
    private val _activityState = MutableStateFlow(ActivityProgress())

    init {
        refreshActivity()
    }

    fun refreshActivity() {
        viewModelScope.launch {
            _activityState.value = _activityState.value.copy(isLoading = true)
            try {
                when (val state = healthConnectManager.getActivityState()) {
                    is HealthConnectActivityState.Available -> {
                        val formattedSteps = java.text.NumberFormat.getNumberInstance(Locale.US).format(state.stepsToday)
                        _activityState.value = ActivityProgress(
                            stepsToday = state.stepsToday,
                            isAvailable = true,
                            isConnected = true,
                            isLoading = false,
                            statusMessage = if (state.stepsToday > 0) "$formattedSteps steps today" else "0 steps recorded today"
                        )
                    }
                    is HealthConnectActivityState.PermissionRequired -> {
                        _activityState.value = ActivityProgress(
                            stepsToday = 0L,
                            isAvailable = true,
                            isConnected = false,
                            isLoading = false,
                            statusMessage = "Connect Health Connect to sync steps"
                        )
                    }
                    is HealthConnectActivityState.Unavailable -> {
                        _activityState.value = ActivityProgress(
                            stepsToday = 0L,
                            isAvailable = false,
                            isConnected = false,
                            isLoading = false,
                            statusMessage = state.reason
                        )
                    }
                }
            } catch (e: Exception) {
                _activityState.value = ActivityProgress(
                    stepsToday = 0L,
                    isAvailable = false,
                    isConnected = false,
                    isLoading = false,
                    statusMessage = "Activity unavailable"
                )
            }
        }
    }

    fun logWater(amountMl: Int = 250) {
        viewModelScope.launch {
            waterLogRepository.logWater(amountMl, todayIsoString)
        }
    }

    fun undoLatestWaterLog() {
        viewModelScope.launch {
            val logs = waterLogRepository.getWaterLogsByDate(todayIsoString)
            val latest = logs.maxWithOrNull(compareBy({ it.loggedAt }, { it.id }))
            if (latest != null) {
                waterLogRepository.deleteWaterLog(latest.id)
            }
        }
    }

    fun editWaterLog(id: Long, newAmountMl: Int) {
        viewModelScope.launch {
            val entry = waterLogRepository.getWaterLogsByDate(todayIsoString).find { it.id == id }
            if (entry != null) {
                waterLogRepository.saveWaterLog(entry.copy(amountMl = newAmountMl))
            }
        }
    }

    fun deleteWaterLog(id: Long) {
        viewModelScope.launch {
            waterLogRepository.deleteWaterLog(id)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        nutritionGoalRepository.observeLatestGoal(),
        mealLogRepository.observeMealsWithItemsByDate(todayIsoString),
        userProfileRepository.observeProfile(),
        waterLogRepository.observeWaterLogsByDate(todayIsoString),
        combine(mealLogRepository.observeStreakDays(), _activityState) { streak, act -> Pair(streak, act) }
    ) { goal, mealsWithItems, profile, waterLogs, streakAndActivity ->
        val (streakDays, activity) = streakAndActivity
        val targetCalories = goal?.calories ?: 2000
        val targetProtein = goal?.proteinG ?: 150.0
        val targetCarbs = goal?.carbsG ?: 200.0
        val targetFat = goal?.fatG ?: 65.0

        val validMealsWithItems = mealsWithItems.filter { it.items.isNotEmpty() }
        var consumedCalories = 0.0
        var consumedProtein = 0.0
        var consumedCarbs = 0.0
        var consumedFat = 0.0

        validMealsWithItems.forEach { mealWithItems ->
            consumedCalories += mealWithItems.items.sumOf { it.calories }
            mealWithItems.items.forEach { item ->
                consumedProtein += item.proteinG
                consumedCarbs += item.carbsG
                consumedFat += item.fatG
            }
        }

        val remainingCalories = targetCalories - consumedCalories.roundToInt()
        val progressPercent = if (targetCalories > 0) {
            ((consumedCalories / targetCalories.toDouble()) * 100.0).roundToInt()
        } else 0
        val calorieFraction = if (targetCalories > 0) {
            min(1.0f, max(0.0f, (consumedCalories / targetCalories).toFloat()))
        } else 0.0f

        val proteinProgress = MacroProgress(
            currentGrams = consumedProtein,
            targetGrams = targetProtein,
            progressFraction = if (targetProtein > 0) min(1.0f, max(0.0f, (consumedProtein / targetProtein).toFloat())) else 0.0f
        )

        val carbsProgress = MacroProgress(
            currentGrams = consumedCarbs,
            targetGrams = targetCarbs,
            progressFraction = if (targetCarbs > 0) min(1.0f, max(0.0f, (consumedCarbs / targetCarbs).toFloat())) else 0.0f
        )

        val fatProgress = MacroProgress(
            currentGrams = consumedFat,
            targetGrams = targetFat,
            progressFraction = if (targetFat > 0) min(1.0f, max(0.0f, (consumedFat / targetFat).toFloat())) else 0.0f
        )

        // Water calculation
        val waterTargetMl = ((goal?.waterLiters ?: 2.4) * 1000).roundToInt()
        val waterTargetGlasses = goal?.waterGlasses ?: 8
        val waterConsumedMl = waterLogs.sumOf { it.amountMl }
        val waterFraction = if (waterTargetMl > 0) min(1.0f, (waterConsumedMl.toFloat() / waterTargetMl.toFloat())) else 0.0f
        val latestLog = waterLogs.maxWithOrNull(compareBy({ it.loggedAt }, { it.id }))

        val waterProgress = WaterProgress(
            consumedMl = waterConsumedMl,
            targetMl = waterTargetMl,
            targetGlasses = waterTargetGlasses,
            progressFraction = waterFraction,
            latestLogId = latestLog?.id,
            logsCountToday = waterLogs.size,
            todayLogs = waterLogs
        )

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

        HomeUiState(
            isLoading = false,
            userName = profile?.firstName?.takeIf { it.isNotBlank() } ?: "there",
            currentDateFormatted = LocalDate.now().format(dateFormatter),
            targetCalories = targetCalories,
            consumedCalories = consumedCalories.roundToInt(),
            remainingCalories = remainingCalories,
            calorieProgressFraction = calorieFraction,
            progressPercent = progressPercent,
            protein = proteinProgress,
            carbs = carbsProgress,
            fat = fatProgress,
            water = waterProgress,
            activity = activity,
            mealsGrouped = grouped,
            hasLoggedMealsToday = validMealsWithItems.isNotEmpty(),
            streakDays = streakDays
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(
            currentDateFormatted = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
        )
    )
}
