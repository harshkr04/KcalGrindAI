package com.lumina.nutrition.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.nutrition.domain.model.MealWithFoodItems
import com.lumina.nutrition.domain.model.NutritionGoal
import com.lumina.nutrition.domain.model.UserProfile
import com.lumina.nutrition.domain.model.WaterLog
import com.lumina.nutrition.domain.model.WeightEntry
import com.lumina.nutrition.domain.repository.MealLogRepository
import com.lumina.nutrition.domain.repository.NutritionGoalRepository
import com.lumina.nutrition.domain.repository.UserProfileRepository
import com.lumina.nutrition.domain.repository.WaterLogRepository
import com.lumina.nutrition.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository,
    private val nutritionGoalRepository: NutritionGoalRepository,
    private val userProfileRepository: UserProfileRepository,
    private val weightRepository: WeightRepository,
    private val waterLogRepository: WaterLogRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(InsightsTab.CALORIES)
    private val startDate = LocalDate.now().minusDays(6)
    private val endDate = LocalDate.now()

    private val dataFlow: Flow<InsightsUiState> = combine(
        mealLogRepository.observeMealsWithItemsInRange(startDate.toString(), endDate.toString()),
        nutritionGoalRepository.observeLatestGoal(),
        userProfileRepository.observeProfile(),
        weightRepository.observeAllWeightEntries(),
        waterLogRepository.observeWaterLogsInRange(startDate.toString(), endDate.toString())
    ) { mealsWithItems: List<MealWithFoodItems>, goal: NutritionGoal?, profile: UserProfile?, weights: List<WeightEntry>, waterLogs: List<WaterLog> ->

        val targetCalories = goal?.calories ?: 2000
        val targetProtein = goal?.proteinG ?: 120.0
        val targetCarbs = goal?.carbsG ?: 200.0
        val targetFat = goal?.fatG ?: 65.0
        val targetWaterMl = ((goal?.waterLiters ?: 2.0) * 1000).toInt()

        // 1. Compute daily points for the last 7 days
        val dayPoints = mutableListOf<DayCaloriePoint>()
        var sumCaloriesAcrossDays = 0.0
        var sumProteinAcrossDays = 0.0
        var sumCarbsAcrossDays = 0.0
        var sumFatAcrossDays = 0.0
        var trackedDaysCount = 0

        val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())

        for (i in 0..6) {
            val date = startDate.plusDays(i.toLong())
            val dateStr = date.toString()
            val dayMeals = mealsWithItems.filter { it.meal.date == dateStr && it.items.isNotEmpty() }
            
            var dayCalories = 0.0
            dayMeals.forEach { m ->
                dayCalories += m.items.sumOf { it.calories }
                sumProteinAcrossDays += m.items.sumOf { it.proteinG }
                sumCarbsAcrossDays += m.items.sumOf { it.carbsG }
                sumFatAcrossDays += m.items.sumOf { it.fatG }
            }

            val hasLogs = dayMeals.isNotEmpty()
            if (hasLogs) {
                trackedDaysCount++
                sumCaloriesAcrossDays += dayCalories
            }

            dayPoints.add(
                DayCaloriePoint(
                    date = date,
                    dayLabel = date.format(dayFormatter),
                    calories = dayCalories.roundToInt(),
                    targetCalories = targetCalories,
                    hasLogs = hasLogs
                )
            )
        }

        val avgCalories = if (trackedDaysCount > 0) (sumCaloriesAcrossDays / trackedDaysCount).roundToInt() else 0
        val trackingRate = ((trackedDaysCount.toDouble() / 7.0) * 100.0).roundToInt()
        val calorieDelta = avgCalories - targetCalories

        // 2. Macro adherence
        val divisor = if (trackedDaysCount > 0) trackedDaysCount.toDouble() else 1.0
        val avgProtein = sumProteinAcrossDays / divisor
        val avgCarbs = sumCarbsAcrossDays / divisor
        val avgFat = sumFatAcrossDays / divisor

        val proteinAdherence = MacroAdherence(
            name = "Protein",
            avgGrams = avgProtein,
            targetGrams = targetProtein,
            adherencePercent = if (targetProtein > 0) ((avgProtein / targetProtein) * 100).roundToInt().coerceIn(0, 100) else 0
        )

        val carbsAdherence = MacroAdherence(
            name = "Carbs",
            avgGrams = avgCarbs,
            targetGrams = targetCarbs,
            adherencePercent = if (targetCarbs > 0) ((avgCarbs / targetCarbs) * 100).roundToInt().coerceIn(0, 100) else 0
        )

        val fatAdherence = MacroAdherence(
            name = "Fat",
            avgGrams = avgFat,
            targetGrams = targetFat,
            adherencePercent = if (targetFat > 0) ((avgFat / targetFat) * 100).roundToInt().coerceIn(0, 100) else 0
        )

        // 3. Hydration
        val totalWater = waterLogs.sumOf { it.amountMl }
        val avgWater = if (trackedDaysCount > 0) totalWater / trackedDaysCount else totalWater / 7

        // 4. Weight stats
        val sortedWeights = weights.sortedBy { it.loggedAt }
        val latestWeight = sortedWeights.lastOrNull()?.weightKg ?: profile?.weightKg
        val startWeight = sortedWeights.firstOrNull()?.weightKg ?: profile?.weightKg
        val weightChange = if (latestWeight != null && startWeight != null) {
            ((latestWeight - startWeight) * 10.0).roundToInt() / 10.0
        } else null

        InsightsUiState(
            isLoading = false,
            selectedTab = InsightsTab.CALORIES,
            startDate = startDate,
            endDate = endDate,
            periodLabel = "Last 7 Days",
            avgCaloriesPerDay = avgCalories,
            targetCaloriesPerDay = targetCalories,
            calorieDelta = calorieDelta,
            daysTrackedCount = trackedDaysCount,
            totalDaysCount = 7,
            trackingRatePercent = trackingRate,
            calorieTrend = dayPoints,
            proteinAdherence = proteinAdherence,
            carbsAdherence = carbsAdherence,
            fatAdherence = fatAdherence,
            avgWaterMlPerDay = avgWater,
            targetWaterMlPerDay = targetWaterMl,
            latestWeightKg = latestWeight,
            startWeightKg = startWeight,
            weightChangeKg = weightChange,
            goalWeightKg = profile?.goalWeightKg,
            weightEntries = sortedWeights.reversed(),
            hasWeightData = sortedWeights.isNotEmpty()
        )
    }

    val uiState: StateFlow<InsightsUiState> = combine(_selectedTab, dataFlow) { tab, data ->
        data.copy(selectedTab = tab)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState()
    )

    fun selectTab(tab: InsightsTab) {
        _selectedTab.update { tab }
    }

    fun logWeight(weightKg: Double, date: String = LocalDate.now().toString()) {
        viewModelScope.launch {
            weightRepository.saveWeightEntry(
                WeightEntry(
                    id = 0L,
                    weightKg = weightKg,
                    date = date,
                    loggedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
