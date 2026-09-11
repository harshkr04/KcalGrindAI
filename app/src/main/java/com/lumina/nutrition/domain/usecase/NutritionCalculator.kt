package com.lumina.nutrition.domain.usecase

import com.lumina.nutrition.domain.model.ActivityLevel
import com.lumina.nutrition.domain.model.GoalType
import com.lumina.nutrition.domain.model.TargetBasis
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

object NutritionCalculator {

    const val CAL_FLOOR = 1200

    object Limits {
        const val MIN_AGE = 13
        const val MAX_AGE = 100
        const val MIN_HEIGHT_CM = 100.0
        const val MAX_HEIGHT_CM = 250.0
        const val MIN_WEIGHT_KG = 30.0
        const val MAX_WEIGHT_KG = 300.0
    }

    data class MacroSplitResult(
        val protein: Int,
        val carbs: Int,
        val fat: Int
    )

    data class WaterResult(
        val waterLiters: Double,
        val glasses: Int
    )

    data class CalculationResult(
        val calories: Int,
        val proteinG: Double,
        val carbsG: Double,
        val fatG: Double,
        val waterLiters: Double,
        val waterGlasses: Int,
        val bmr: Double?,
        val tdee: Double?,
        val delta: Int,
        val floored: Boolean,
        val goal: GoalType,
        val basis: TargetBasis
    )

    data class MacroCheckResult(
        val sumCalories: Int,
        val diffCalories: Int,
        val withinTolerance: Boolean,
        val tolerance: Int,
        val proteinPct: Int,
        val carbsPct: Int,
        val fatPct: Int
    )

    private fun jsRound(n: Double): Long = Math.round(n)

    fun round1(n: Double): Double = jsRound(n * 10.0) / 10.0

    fun kgToLb(kg: Double): Int = jsRound(kg * 2.2046226).toInt()

    fun lbToKg(lb: Double): Double = round1(lb / 2.2046226)

    fun cmToFtIn(cm: Double): Pair<Int, Int> {
        val t = cm / 2.54
        val ft = floor(t / 12.0).toInt()
        val inch = jsRound(t - ft * 12.0).toInt()
        return Pair(ft, inch)
    }

    fun ftInToCm(ft: Int, inch: Int): Double = round1((ft * 12.0 + inch) * 2.54)

    /**
     * Mifflin–St Jeor formula using midpoint of male (+5) and female (-161) constants (-78)
     * as onboarding deliberately asks for minimal questions.
     */
    fun bmr(weightKg: Double, heightCm: Double, age: Int): Double {
        return 10.0 * weightKg + 6.25 * heightCm - 5.0 * age - 78.0
    }

    fun waterFor(weightKg: Double, activity: ActivityLevel): WaterResult {
        val factor = when (activity) {
            ActivityLevel.SEDENTARY -> 0.030
            ActivityLevel.LIGHT -> 0.032
            ActivityLevel.MODERATE -> 0.034
            ActivityLevel.VERY -> 0.037
            ActivityLevel.EXTREME -> 0.040
        }
        val liters = max(1.8, min(4.0, round1(weightKg * factor)))
        val glasses = max(6, jsRound((liters * 1000.0) / 300.0).toInt())
        return WaterResult(waterLiters = liters, glasses = glasses)
    }

    fun macroSplit(
        calories: Int,
        weightKg: Double,
        goal: GoalType,
        diets: List<String> = emptyList()
    ): MacroSplitResult {
        var perKg = goal.proteinPerKg
        val lowerDiets = diets.map { it.lowercase() }

        if (lowerDiets.contains("high_protein")) {
            perKg += 0.2
        }
        if (lowerDiets.contains("vegan") || lowerDiets.contains("vegetarian")) {
            perKg = min(perKg, 1.8)
        }

        var protein = jsRound(weightKg * perKg).toInt()
        var proteinKcal = protein * 4

        // Protein never eats more than 40% of the day
        if (proteinKcal > calories * 0.4) {
            protein = jsRound((calories * 0.4) / 4.0).toInt()
            proteinKcal = protein * 4
        }

        var fatPct = 0.28
        if (lowerDiets.contains("keto")) {
            fatPct = 0.70
        } else if (lowerDiets.contains("low_carb")) {
            fatPct = 0.42
        } else if (lowerDiets.contains("mediterranean")) {
            fatPct = 0.35
        }

        var fat = jsRound((calories * fatPct) / 9.0).toInt()
        var carbs = jsRound((calories - proteinKcal - fat * 9.0) / 4.0).toInt()

        if (carbs < 20) { // keto / very low carb: push remainder into fat
            carbs = 20
            fat = jsRound((calories - proteinKcal - carbs * 4.0) / 9.0).toInt()
        }

        return MacroSplitResult(
            protein = protein,
            carbs = carbs,
            fat = max(fat, 20)
        )
    }

    fun calculate(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        goal: GoalType,
        activity: ActivityLevel,
        goalWeightKg: Double? = null,
        dietTags: List<String> = emptyList()
    ): CalculationResult {
        if (weightKg <= 0 || heightCm <= 0 || age <= 0) {
            throw IllegalArgumentException("missing_profile: weight, height, and age must be positive")
        }

        val baseBmr = bmr(weightKg, heightCm, age)
        val tdee = baseBmr * activity.factor

        var delta = goal.delta
        if (goal == GoalType.LOSE && goalWeightKg != null && goalWeightKg >= weightKg) {
            delta = 0
        }
        if ((goal == GoalType.GAIN || goal == GoalType.MUSCLE) && goalWeightKg != null && goalWeightKg < weightKg - 0.5) {
            delta = -300
        }
        if (goal == GoalType.LOSE && goalWeightKg != null && abs(weightKg - goalWeightKg) < 2.0) {
            delta = -250
        }

        var calories = (jsRound((tdee + delta) / 10.0) * 10).toInt()
        var floored = false
        val floor = max(CAL_FLOOR, (jsRound(baseBmr * 0.95 / 10.0) * 10).toInt())
        if (calories < floor) {
            calories = floor
            floored = true
        }

        val macros = macroSplit(calories, weightKg, goal, dietTags)
        val water = waterFor(weightKg, activity)

        return CalculationResult(
            calories = calories,
            proteinG = macros.protein.toDouble(),
            carbsG = macros.carbs.toDouble(),
            fatG = macros.fat.toDouble(),
            waterLiters = water.waterLiters,
            waterGlasses = water.glasses,
            bmr = jsRound(baseBmr).toDouble(),
            tdee = jsRound(tdee).toDouble(),
            delta = delta,
            floored = floored,
            goal = goal,
            basis = TargetBasis.RECOMMENDED
        )
    }

    fun fallback(
        weightKg: Double? = null,
        goal: GoalType = GoalType.MAINTAIN,
        activity: ActivityLevel = ActivityLevel.MODERATE,
        dietTags: List<String> = emptyList()
    ): CalculationResult {
        val w = weightKg ?: 70.0
        val calories = 2000
        val macros = macroSplit(calories, w, goal, dietTags)
        val water = waterFor(w, activity)

        return CalculationResult(
            calories = calories,
            proteinG = macros.protein.toDouble(),
            carbsG = macros.carbs.toDouble(),
            fatG = macros.fat.toDouble(),
            waterLiters = water.waterLiters,
            waterGlasses = water.glasses,
            bmr = null,
            tdee = null,
            delta = 0,
            floored = false,
            goal = goal,
            basis = TargetBasis.FALLBACK
        )
    }

    fun goalWeightNote(goal: GoalType, weightKg: Double?, goalWeightKg: Double?): String? {
        if (weightKg == null || goalWeightKg == null) return null
        val delta = round1(goalWeightKg - weightKg)
        if (goal == GoalType.LOSE && delta >= 0) {
            return "Your goal weight isn’t lower than your current weight. We’ll aim to maintain instead — you can change either value."
        }
        if ((goal == GoalType.GAIN || goal == GoalType.MUSCLE) && delta <= -0.5) {
            return "Your goal weight is lower than your current weight. We’ll plan a gentle deficit — adjust if that isn’t what you meant."
        }
        if (abs(delta) > weightKg * 0.25) {
            return "That’s a big change. We’ll pace it gradually and you can revisit it any time."
        }
        return null
    }

    fun checkMacros(calories: Int, proteinG: Double, carbsG: Double, fatG: Double): MacroCheckResult {
        val sum = (proteinG.toInt() * 4) + (carbsG.toInt() * 4) + (fatG.toInt() * 9)
        val diff = sum - calories
        val tolerance = max(40, jsRound(calories * 0.03).toInt())
        val proteinPct = if (sum > 0) jsRound((proteinG * 4.0 / sum) * 100.0).toInt() else 0
        val carbsPct = if (sum > 0) jsRound((carbsG * 4.0 / sum) * 100.0).toInt() else 0
        val fatPct = if (sum > 0) jsRound((fatG * 9.0 / sum) * 100.0).toInt() else 0

        return MacroCheckResult(
            sumCalories = sum,
            diffCalories = diff,
            withinTolerance = abs(diff) <= tolerance,
            tolerance = tolerance,
            proteinPct = proteinPct,
            carbsPct = carbsPct,
            fatPct = fatPct
        )
    }

    fun explain(result: CalculationResult): String {
        if (result.basis == TargetBasis.FALLBACK) {
            return "This is a safe starting plan. Add your details any time and we'll refine it."
        }
        if (result.floored) {
            return "We kept your target at a healthy minimum rather than going lower, while still moving toward ${result.goal.label.lowercase()}."
        }
        if (result.delta < 0) {
            return "Your target sits about ${abs(result.delta)} kcal under your estimated daily burn — designed for gradual, steady progress."
        }
        if (result.delta > 0) {
            return "Your target sits about ${result.delta} kcal above your estimated daily burn — enough to build without unnecessary excess."
        }
        return "Your target matches your estimated daily burn, which keeps things stable while you get into a rhythm."
    }
}
