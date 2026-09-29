package com.kcalgrindai.app.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.domain.model.MealWithFoodItems
import com.kcalgrindai.app.domain.model.NutritionGoal
import com.kcalgrindai.app.domain.model.Recipe
import com.kcalgrindai.app.domain.model.UserProfile
import com.kcalgrindai.app.domain.model.WaterLog
import com.kcalgrindai.app.domain.model.WeightEntry
import com.kcalgrindai.app.domain.repository.MealLogRepository
import com.kcalgrindai.app.domain.repository.NutritionGoalRepository
import com.kcalgrindai.app.domain.repository.RecipeRepository
import com.kcalgrindai.app.domain.repository.UserProfileRepository
import com.kcalgrindai.app.domain.repository.WaterLogRepository
import com.kcalgrindai.app.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
    private val waterLogRepository: WaterLogRepository,
    private val recipeRepository: RecipeRepository,
    private val healthConnectManager: com.kcalgrindai.app.data.health.HealthConnectManager? = null,
    private val calculateNutritionGoalUseCase: com.kcalgrindai.app.domain.usecase.CalculateNutritionGoalUseCase? = null
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(InsightsTab.CALORIES)
    private val startDate = LocalDate.now().minusDays(6)
    private val endDate = LocalDate.now()

    init {
        viewModelScope.launch {
            recipeRepository.seedRecipesIfEmpty()
        }
    }

    private val baseDataFlow: Flow<InsightsUiState> = combine(
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

        // 5. Steps / Activity stats from Health Connect
        val startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endInstant = Instant.now()
        val totalStepsWeek = healthConnectManager?.readStepsInRange(startInstant, endInstant) ?: 0L
        val avgSteps = if (totalStepsWeek > 0L) totalStepsWeek / 7L else null

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
            avgStepsPerDay = avgSteps,
            latestWeightKg = latestWeight,
            startWeightKg = startWeight,
            weightChangeKg = weightChange,
            goalWeightKg = profile?.goalWeightKg,
            weightEntries = sortedWeights.reversed(),
            hasWeightData = sortedWeights.isNotEmpty()
        )
    }

    private val _selectedMealTypeFilter = MutableStateFlow<String?>(null)
    private val _selectedDietTagFilter = MutableStateFlow<String?>(null)
    private val _selectedSortOption = MutableStateFlow(RecipeSortOption.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _recipeMessage = MutableStateFlow<String?>(null)

    private data class RecipeFilterState(
        val mealType: String?,
        val dietTag: String?,
        val sort: RecipeSortOption,
        val searchQuery: String,
        val msg: String?
    )

    private val recipeFlow: Flow<List<Recipe>> = recipeRepository.observeAllRecipes()

    private val filteredRecipesFlow: Flow<List<Recipe>> = combine(
        recipeFlow,
        _selectedMealTypeFilter,
        _selectedDietTagFilter,
        _selectedSortOption,
        _searchQuery
    ) { allRecipes, mealType, dietTag, sort, query ->
        var filtered = allRecipes
        filtered = when (sort) {
            RecipeSortOption.ALL -> filtered
            RecipeSortOption.POPULAR -> filtered.sortedByDescending { it.popularityScore }
            RecipeSortOption.FAVORITES -> filtered.filter { it.isFavorite }
        }
        if (!mealType.isNullOrBlank()) {
            filtered = filtered.filter { it.mealType.equals(mealType, ignoreCase = true) }
        }
        if (!dietTag.isNullOrBlank()) {
            filtered = filtered.filter { recipe ->
                recipe.dietTags.any { it.equals(dietTag, ignoreCase = true) }
            }
        }
        if (query.isNotBlank()) {
            val q = query.trim()
            filtered = filtered.filter { recipe ->
                recipe.name.contains(q, ignoreCase = true) ||
                recipe.description.contains(q, ignoreCase = true) ||
                recipe.ingredients.any { it.name.contains(q, ignoreCase = true) } ||
                recipe.dietTags.any { it.contains(q, ignoreCase = true) } ||
                recipe.mealType.contains(q, ignoreCase = true)
            }
        }
        filtered
    }

    private val filterStateFlow: Flow<RecipeFilterState> = combine(
        _selectedMealTypeFilter,
        _selectedDietTagFilter,
        _selectedSortOption,
        _searchQuery,
        _recipeMessage
    ) { mealType: String?, dietTag: String?, sort: RecipeSortOption, query: String, msg: String? ->
        RecipeFilterState(mealType, dietTag, sort, query, msg)
    }

    val uiState: StateFlow<InsightsUiState> = combine(
        baseDataFlow,
        recipeFlow,
        filteredRecipesFlow,
        _selectedTab,
        filterStateFlow
    ) { base, allRecipes, filtered, tab, filterState ->
        base.copy(
            selectedTab = tab,
            recipes = allRecipes,
            filteredRecipes = filtered,
            selectedMealTypeFilter = filterState.mealType,
            selectedDietTagFilter = filterState.dietTag,
            selectedSortOption = filterState.sort,
            searchQuery = filterState.searchQuery,
            recipeLoggedMessage = filterState.msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectTab(tab: InsightsTab) {
        _selectedTab.update { tab }
    }

    fun setMealTypeFilter(mealType: String?) {
        _selectedMealTypeFilter.update { current ->
            if (current == mealType) null else mealType
        }
    }

    fun setDietTagFilter(dietTag: String?) {
        _selectedDietTagFilter.update { current ->
            if (current == dietTag) null else dietTag
        }
    }

    fun setSortOption(sort: RecipeSortOption) {
        _selectedSortOption.update { sort }
    }

    fun toggleFavorite(recipeId: String) {
        viewModelScope.launch {
            recipeRepository.toggleFavorite(recipeId)
        }
    }

    fun clearRecipeMessage() {
        _recipeMessage.value = null
    }

    fun logWeight(weightKg: Double, date: String = LocalDate.now().toString()) {
        viewModelScope.launch {
            val roundedKg = (weightKg * 10.0).roundToInt() / 10.0
            weightRepository.saveWeightEntry(
                WeightEntry(
                    id = 0L,
                    weightKg = roundedKg,
                    date = date,
                    loggedAt = System.currentTimeMillis()
                )
            )

            val profile = userProfileRepository.observeProfile().firstOrNull()
            if (profile != null) {
                val updated = profile.copy(weightKg = roundedKg, updatedAt = System.currentTimeMillis())
                userProfileRepository.saveProfile(updated)

                val currentGoal = nutritionGoalRepository.getLatestGoal()
                if ((currentGoal == null || !currentGoal.isCustom) && calculateNutritionGoalUseCase != null) {
                    val newGoal = calculateNutritionGoalUseCase(updated)
                    nutritionGoalRepository.saveGoal(newGoal.copy(id = currentGoal?.id ?: 0L))
                }
            }
        }
    }
}
