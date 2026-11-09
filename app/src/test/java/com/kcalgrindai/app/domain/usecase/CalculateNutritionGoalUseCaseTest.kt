package com.kcalgrindai.app.domain.usecase

import com.kcalgrindai.app.domain.model.ActivityLevel
import com.kcalgrindai.app.domain.model.GoalType
import com.kcalgrindai.app.domain.model.TargetBasis
import com.kcalgrindai.app.domain.model.UnitSystem
import com.kcalgrindai.app.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
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

    @Test
    fun testGoalSafetyLimitsAndWeeklyPaceBounds() {
        // Enforce safe weekly weight-change rate dynamically capped at 1% of body weight per week
        // 70 kg user: max safe weekly rate is 0.70 kg/week
        assertEquals(0.70, NutritionCalculator.maxWeeklyWeightChangeKg(70.0), 0.001)
        assertTrue(NutritionCalculator.isWeeklyPaceSafe(0.5, 70.0))
        assertTrue(NutritionCalculator.isWeeklyPaceSafe(0.7, 70.0))
        assertTrue(NutritionCalculator.isWeeklyPaceSafe(-0.7, 70.0))
        assertFalse(NutritionCalculator.isWeeklyPaceSafe(0.8, 70.0))
        assertFalse(NutritionCalculator.isWeeklyPaceSafe(-1.2, 70.0))

        // Low body weight edge case (45 kg user): max safe weekly rate is 0.45 kg/week
        assertEquals(0.45, NutritionCalculator.maxWeeklyWeightChangeKg(45.0), 0.001)
        assertTrue(NutritionCalculator.isWeeklyPaceSafe(0.4, 45.0))
        assertTrue(NutritionCalculator.isWeeklyPaceSafe(0.45, 45.0))
        assertFalse(NutritionCalculator.isWeeklyPaceSafe(0.5, 45.0))
        assertFalse(NutritionCalculator.isWeeklyPaceSafe(-0.6, 45.0))

        // High body weight (100 kg user): max safe weekly rate is 1.00 kg/week
        assertEquals(1.00, NutritionCalculator.maxWeeklyWeightChangeKg(100.0), 0.001)
        assertTrue(NutritionCalculator.isWeeklyPaceSafe(1.0, 100.0))
        assertFalse(NutritionCalculator.isWeeklyPaceSafe(1.1, 100.0))

        // Daily calorie deficit/surplus limits scaled to 1% rate
        // 45 kg user: max deficit = (45 * 0.01 * 7700) / 7 = 495 kcal/day
        assertEquals(-495, NutritionCalculator.clampDailyCalorieDelta(-1000, 45.0))
        assertEquals(495, NutritionCalculator.clampDailyCalorieDelta(800, 45.0))
        // 70 kg user: max deficit = (70 * 0.01 * 7700) / 7 = 770 kcal/day
        assertEquals(-770, NutritionCalculator.clampDailyCalorieDelta(-1000, 70.0))
        assertEquals(750, NutritionCalculator.clampDailyCalorieDelta(1200, 70.0))
        assertEquals(-450, NutritionCalculator.clampDailyCalorieDelta(-450, 70.0))

        // Sex-aware conservative calorie floors
        assertEquals(1500, NutritionCalculator.calorieFloor("male"))
        assertEquals(1500, NutritionCalculator.calorieFloor("MALE"))
        assertEquals(1200, NutritionCalculator.calorieFloor("female"))
        assertEquals(1200, NutritionCalculator.calorieFloor("FEMALE"))
        assertEquals(1200, NutritionCalculator.calorieFloor(null))

        // Enforce sex-aware calorie floor in calculate()
        // Male floor (1500 kcal/day)
        val maleSevereDeficit = NutritionCalculator.calculate(
            weightKg = 50.0,
            heightCm = 150.0,
            age = 50,
            goal = GoalType.LOSE,
            activity = ActivityLevel.SEDENTARY,
            sex = "male"
        )
        assertTrue(maleSevereDeficit.calories >= NutritionCalculator.CAL_FLOOR_MALE)
        assertTrue(maleSevereDeficit.floored)
        assertEquals(1500, maleSevereDeficit.calories)

        // Female / unspecified floor (1200 kcal/day)
        val femaleSevereDeficit = NutritionCalculator.calculate(
            weightKg = 40.0,
            heightCm = 145.0,
            age = 60,
            goal = GoalType.LOSE,
            activity = ActivityLevel.SEDENTARY,
            sex = "female"
        )
        assertTrue(femaleSevereDeficit.calories >= NutritionCalculator.CAL_FLOOR_FEMALE)
        assertTrue(femaleSevereDeficit.floored)
        assertEquals(1200, femaleSevereDeficit.calories)

        // Minor safety protection: require age >= 18 for weight-loss or gain goals
        val minorLoseEx = assertThrows(IllegalArgumentException::class.java) {
            NutritionCalculator.calculate(
                weightKg = 60.0,
                heightCm = 165.0,
                age = 16,
                goal = GoalType.LOSE,
                activity = ActivityLevel.MODERATE
            )
        }
        assertEquals(NutritionCalculator.MINOR_WEIGHT_MODIFICATION_RESTRICTION_MESSAGE, minorLoseEx.message)

        val minorGainEx = assertThrows(IllegalArgumentException::class.java) {
            NutritionCalculator.calculate(
                weightKg = 60.0,
                heightCm = 165.0,
                age = 17,
                goal = GoalType.GAIN,
                activity = ActivityLevel.MODERATE
            )
        }
        assertEquals(NutritionCalculator.MINOR_WEIGHT_MODIFICATION_RESTRICTION_MESSAGE, minorGainEx.message)

        val minorMuscleEx = assertThrows(IllegalArgumentException::class.java) {
            NutritionCalculator.calculate(
                weightKg = 60.0,
                heightCm = 165.0,
                age = 15,
                goal = GoalType.MUSCLE,
                activity = ActivityLevel.MODERATE
            )
        }
        assertEquals(NutritionCalculator.MINOR_WEIGHT_MODIFICATION_RESTRICTION_MESSAGE, minorMuscleEx.message)

        // Non-weight-modification goals allowed for minors
        val minorMaintain = NutritionCalculator.calculate(
            weightKg = 60.0,
            heightCm = 165.0,
            age = 16,
            goal = GoalType.MAINTAIN,
            activity = ActivityLevel.MODERATE
        )
        assertNotNull(minorMaintain)
        assertEquals(GoalType.MAINTAIN, minorMaintain.goal)

        val minorHealthier = NutritionCalculator.calculate(
            weightKg = 60.0,
            heightCm = 165.0,
            age = 16,
            goal = GoalType.HEALTHIER,
            activity = ActivityLevel.MODERATE
        )
        assertNotNull(minorHealthier)
        assertEquals(GoalType.HEALTHIER, minorHealthier.goal)

        // Age >= 18 allowed for weight-loss
        val adultLose = NutritionCalculator.calculate(
            weightKg = 60.0,
            heightCm = 165.0,
            age = 18,
            goal = GoalType.LOSE,
            activity = ActivityLevel.MODERATE
        )
        assertNotNull(adultLose)
        assertEquals(GoalType.LOSE, adultLose.goal)
    }
}
