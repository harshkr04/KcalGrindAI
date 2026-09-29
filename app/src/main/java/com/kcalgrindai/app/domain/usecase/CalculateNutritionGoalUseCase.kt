package com.kcalgrindai.app.domain.usecase

import com.kcalgrindai.app.domain.model.ActivityLevel
import com.kcalgrindai.app.domain.model.GoalType
import com.kcalgrindai.app.domain.model.NutritionGoal
import com.kcalgrindai.app.domain.model.UserProfile
import javax.inject.Inject

/**
 * Calculates nutrition targets (BMR, TDEE, Calories, Protein, Carbs, Fat, Water)
 * based on Mifflin–St Jeor formula and activity/goal adjustments translated
 * faithfully from the prototype nutrition engine.
 */
class CalculateNutritionGoalUseCase @Inject constructor() {

    operator fun invoke(profile: UserProfile): NutritionGoal {
        val result = try {
            NutritionCalculator.calculate(
                weightKg = profile.weightKg,
                heightCm = profile.heightCm,
                age = profile.age,
                goal = profile.goal,
                activity = profile.activityLevel,
                goalWeightKg = profile.goalWeightKg,
                dietTags = profile.dietTags
            )
        } catch (_: Exception) {
            NutritionCalculator.fallback(
                weightKg = profile.weightKg,
                goal = profile.goal,
                activity = profile.activityLevel,
                dietTags = profile.dietTags
            )
        }

        return NutritionGoal(
            userId = profile.id,
            calories = result.calories,
            proteinG = result.proteinG,
            carbsG = result.carbsG,
            fatG = result.fatG,
            waterLiters = result.waterLiters,
            waterGlasses = result.waterGlasses,
            bmr = result.bmr,
            tdee = result.tdee,
            isCustom = false,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun calculateRaw(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        goal: GoalType,
        activity: ActivityLevel,
        goalWeightKg: Double? = null,
        dietTags: List<String> = emptyList()
    ): NutritionCalculator.CalculationResult {
        return NutritionCalculator.calculate(
            weightKg = weightKg,
            heightCm = heightCm,
            age = age,
            goal = goal,
            activity = activity,
            goalWeightKg = goalWeightKg,
            dietTags = dietTags
        )
    }
}
