package com.kcalgrindai.app.feature.insights

import com.kcalgrindai.app.domain.model.Recipe
import com.kcalgrindai.app.domain.model.WeightEntry
import java.time.LocalDate

enum class InsightsTab {
    CALORIES,
    WEIGHT,
    RECIPES
}

enum class RecipeSortOption(val title: String) {
    ALL("All"),
    POPULAR("Popular"),
    FAVORITES("Favorites")
}

data class DayCaloriePoint(
    val date: LocalDate,
    val dayLabel: String, // e.g. "Mon", "Tue"
    val calories: Int,
    val targetCalories: Int,
    val hasLogs: Boolean
)

data class MacroAdherence(
    val name: String,
    val avgGrams: Double,
    val targetGrams: Double,
    val adherencePercent: Int
)

data class InsightsUiState(
    val isLoading: Boolean = true,
    val selectedTab: InsightsTab = InsightsTab.CALORIES,
    val startDate: LocalDate = LocalDate.now().minusDays(6),
    val endDate: LocalDate = LocalDate.now(),
    val periodLabel: String = "Last 7 Days",
    
    // Calorie stats
    val avgCaloriesPerDay: Int = 0,
    val targetCaloriesPerDay: Int = 2000,
    val calorieDelta: Int = 0, // avg - target
    val daysTrackedCount: Int = 0,
    val totalDaysCount: Int = 7,
    val trackingRatePercent: Int = 0,
    val calorieTrend: List<DayCaloriePoint> = emptyList(),
    
    // Macro stats
    val proteinAdherence: MacroAdherence = MacroAdherence("Protein", 0.0, 120.0, 0),
    val carbsAdherence: MacroAdherence = MacroAdherence("Carbs", 0.0, 200.0, 0),
    val fatAdherence: MacroAdherence = MacroAdherence("Fat", 0.0, 65.0, 0),
    
    // Hydration stats
    val avgWaterMlPerDay: Int = 0,
    val targetWaterMlPerDay: Int = 2000,
    
    // Activity / Steps stats
    val avgStepsPerDay: Long? = null,
    
    // Weight stats
    val latestWeightKg: Double? = null,
    val startWeightKg: Double? = null,
    val weightChangeKg: Double? = null,
    val goalWeightKg: Double? = null,
    val weightEntries: List<WeightEntry> = emptyList(),
    val hasWeightData: Boolean = false,

    // Real Room-backed Recipes & Multi-filter State
    val recipes: List<Recipe> = emptyList(),
    val filteredRecipes: List<Recipe> = emptyList(),
    val selectedMealTypeFilter: String? = null, // null = All; "breakfast", "lunch", "dinner", "snack", "dessert"
    val selectedDietTagFilter: String? = null,  // null = All; "High-Protein", "Low-Carb", etc.
    val selectedSortOption: RecipeSortOption = RecipeSortOption.ALL,
    val searchQuery: String = "",
    val recipeLoggedMessage: String? = null
)
