package com.lumina.nutrition.domain.usecase

import com.lumina.nutrition.domain.model.ActivityLevel
import com.lumina.nutrition.domain.model.GoalType
import com.lumina.nutrition.domain.model.TargetBasis
import com.lumina.nutrition.domain.model.UnitSystem
import com.lumina.nutrition.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateNutritionGoalUseCaseTest {

    private lateinit var useCase: CalculateNutritionGoalUseCase

    @Before
    fun setUp() {
        useCase = CalculateNutritionGoalUseCase()
    }

    @Test
    fun testBmrCalculationMatchesMifflinStJeorMidpoint() {
        // weight: 70kg, height: 175cm, age: 30
        // 10 * 70 + 6.25 * 175 - 5 * 30 - 78 = 700 + 1093.75 - 150 - 78 = 1565.75
        val bmr = NutritionCalculator.bmr(70.0, 175.0, 30)
        assertEquals(1565.75, bmr, 0.001)
    }

    @Test
    fun testStandardWeightLossCalculation() {
        val profile = UserProfile(
            id = 1L,
            goal = GoalType.LOSE,
            units = UnitSystem.METRIC,
            age = 30,
            heightCm = 175.0,
            weightKg = 70.0,
            goalWeightKg = 65.0,
            activityLevel = ActivityLevel.MODERATE,
            dietTags = listOf("high_protein"),
            allergies = emptyList(),
            targetsSource = TargetBasis.RECOMMENDED,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        val goal = useCase(profile)

        assertEquals(1L, goal.userId)
        assertEquals(1980, goal.calories)
        assertEquals(140.0, goal.proteinG, 0.0)
        assertEquals(216.0, goal.carbsG, 0.0)
        assertEquals(62.0, goal.fatG, 0.0)
        assertEquals(2.4, goal.waterLiters, 0.01)
        assertEquals(8, goal.waterGlasses)
        assertEquals(1566.0, goal.bmr!!, 0.0)
        assertEquals(2427.0, goal.tdee!!, 0.0)
        assertFalse(goal.isCustom)
    }

    @Test
    fun testKetoMuscleGainCalculation() {
        val result = NutritionCalculator.calculate(
            weightKg = 80.0,
            heightCm = 180.0,
            age = 25,
            goal = GoalType.MUSCLE,
            activity = ActivityLevel.VERY,
            goalWeightKg = 85.0,
            dietTags = listOf("keto")
        )

        assertEquals(3220, result.calories)
        assertEquals(160.0, result.proteinG, 0.0)
        assertEquals(83.0, result.carbsG, 0.0)
        assertEquals(250.0, result.fatG, 0.0)
        assertEquals(3.0, result.waterLiters, 0.01)
        assertEquals(10, result.waterGlasses)
        assertEquals(1722.0, result.bmr!!, 0.0)
        assertEquals(2970.0, result.tdee!!, 0.0)
        assertEquals(250, result.delta)
        assertFalse(result.floored)
    }

    @Test
    fun testCalorieFloorEnforcement() {
        val result = NutritionCalculator.calculate(
            weightKg = 40.0,
            heightCm = 145.0,
            age = 60,
            goal = GoalType.LOSE,
            activity = ActivityLevel.SEDENTARY,
            goalWeightKg = 35.0,
            dietTags = emptyList()
        )

        assertTrue(result.floored)
        assertEquals(NutritionCalculator.CAL_FLOOR, result.calories)
    }

    @Test
    fun testGoalWeightAdjustmentRules() {
        // When goal is lose but goal weight is higher than current weight -> delta becomes 0 (maintain)
        val loseContradictory = NutritionCalculator.calculate(
            weightKg = 70.0,
            heightCm = 175.0,
            age = 30,
            goal = GoalType.LOSE,
            activity = ActivityLevel.MODERATE,
            goalWeightKg = 75.0
        )
        assertEquals(0, loseContradictory.delta)

        // When goal is lose and goal weight is within 2kg -> gentle deficit of -250
        val loseClose = NutritionCalculator.calculate(
            weightKg = 70.0,
            heightCm = 175.0,
            age = 30,
            goal = GoalType.LOSE,
            activity = ActivityLevel.MODERATE,
            goalWeightKg = 69.0
        )
        assertEquals(-250, loseClose.delta)
    }

    @Test
    fun testFallbackCalculation() {
        val fallback = NutritionCalculator.fallback(weightKg = 70.0)
        assertEquals(2000, fallback.calories)
        assertEquals(TargetBasis.FALLBACK, fallback.basis)
        assertEquals(0, fallback.delta)
        assertFalse(fallback.floored)
    }

    @Test
    fun testUnitConversionsAndMacroChecking() {
        val kg = NutritionCalculator.lbToKg(154.32)
        assertEquals(70.0, kg, 0.1)

        val lb = NutritionCalculator.kgToLb(70.0)
        assertEquals(154, lb)

        val (ft, inch) = NutritionCalculator.cmToFtIn(175.0)
        assertEquals(5, ft)
        assertEquals(9, inch)

        val cm = NutritionCalculator.ftInToCm(5, 9)
        assertEquals(175.3, cm, 0.1)

        val macroCheck = NutritionCalculator.checkMacros(
            calories = 2000,
            proteinG = 150.0,
            carbsG = 200.0,
            fatG = 66.0
        )
        assertTrue(macroCheck.withinTolerance)
        assertNotNull(NutritionCalculator.explain(NutritionCalculator.fallback()))
    }
}
