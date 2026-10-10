package com.kcalgrindai.app.domain.mapper

import com.kcalgrindai.app.domain.model.ActivityLevel
import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.GoalType
import com.kcalgrindai.app.domain.model.ItemSource
import com.kcalgrindai.app.domain.model.LogSource
import com.kcalgrindai.app.domain.model.MealLog
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.model.MealWithFoodItems
import com.kcalgrindai.app.domain.model.NutritionGoal
import com.kcalgrindai.app.domain.model.SproutlingMood
import com.kcalgrindai.app.domain.model.TargetBasis
import com.kcalgrindai.app.domain.model.UnitSystem
import com.kcalgrindai.app.domain.model.UserProfile
import com.kcalgrindai.app.domain.model.WidgetStatusType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class WidgetStateMapperTest {

    private val fixedInstant = Instant.parse("2026-10-10T12:00:00Z")
    private val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
    private val mapper = WidgetStateMapper(fixedClock)

    private val testProfile = UserProfile(
        id = 1L,
        firebaseUid = "uid_123",
        email = "grinder@kcalgrind.ai",
        firstName = "Grinder",
        goal = GoalType.MAINTAIN,
        units = UnitSystem.METRIC,
        age = 26,
        heightCm = 180.0,
        weightKg = 75.0,
        goalWeightKg = 75.0,
        activityLevel = ActivityLevel.MODERATE,
        dietTags = emptyList(),
        allergies = emptyList(),
        targetsSource = TargetBasis.RECOMMENDED,
        createdAt = fixedInstant.toEpochMilli(),
        updatedAt = fixedInstant.toEpochMilli()
    )

    private val testGoal = NutritionGoal(
        id = 1L,
        userId = 1L,
        calories = 2000,
        proteinG = 150.0,
        carbsG = 200.0,
        fatG = 65.0,
        waterLiters = 2.5,
        waterGlasses = 10,
        bmr = 1750.0,
        tdee = 2400.0,
        isCustom = false,
        updatedAt = fixedInstant.toEpochMilli()
    )

    private fun createMeal(
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double
    ): MealWithFoodItems {
        val meal = MealLog(
            id = 1L,
            date = "2026-10-10",
            mealType = MealType.LUNCH,
            totalCalories = calories,
            loggedAt = fixedInstant.toEpochMilli(),
            source = LogSource.MANUAL,
            synced = true
        )
        val item = FoodLogItem(
            id = 1L,
            mealLogId = 1L,
            foodId = 10L,
            name = "Test Meal",
            brand = null,
            servingDescription = "1 serving",
            servingGrams = 200.0,
            calories = calories,
            proteinG = protein,
            carbsG = carbs,
            fatG = fat,
            fiberG = 5.0,
            source = ItemSource.MANUAL,
            confirmed = true
        )
        return MealWithFoodItems(meal = meal, items = listOf(item))
    }

    @Test
    fun testEmptyStateWhenNoMealsLogged() {
        val state = mapper.map(
            goal = testGoal,
            mealsWithItems = emptyList(),
            waterTotalMl = 0,
            streakDays = 5,
            profile = testProfile,
            isPrivacyEnabled = false
        )

        assertEquals(WidgetStatusType.EMPTY, state.statusType)
        assertEquals("2,000 kcal left", state.primaryHeadline)
        assertEquals("Ready to log today", state.subHeadline)
        assertEquals(SproutlingMood.RESTING, state.sproutlingMood)
        assertEquals(0, state.energyBlocksFilled)
        assertEquals(0, state.consumedCalories)
        assertEquals(2000, state.remainingCalories)
        assertEquals(5, state.streakDays)
    }

    @Test
    fun testNormalStateWithPartialIntake() {
        val customGoal = testGoal.copy(calories = 2100)
        val meal = createMeal(calories = 1420.0, protein = 98.0, carbs = 142.0, fat = 45.0)

        val state = mapper.map(
            goal = customGoal,
            mealsWithItems = listOf(meal),
            waterTotalMl = 500,
            streakDays = 3,
            profile = testProfile,
            isPrivacyEnabled = false
        )

        assertEquals(WidgetStatusType.NORMAL, state.statusType)
        assertEquals("680 kcal left", state.primaryHeadline)
        assertEquals("1,420 / 2,100", state.subHeadline)
        assertEquals(SproutlingMood.ACTIVE, state.sproutlingMood)
        assertEquals(1420, state.consumedCalories)
        assertEquals(680, state.remainingCalories)
        assertEquals(7, state.energyBlocksFilled) // 1420/2100 = 67.6% -> 7 blocks
        assertEquals(2, state.waterGlasses) // 500 / 250
        assertTrue(state.proteinFraction > 0.6f)
    }

    @Test
    fun testGoalReachedState() {
        val meal = createMeal(calories = 2000.0, protein = 150.0, carbs = 200.0, fat = 65.0)

        val state = mapper.map(
            goal = testGoal,
            mealsWithItems = listOf(meal),
            waterTotalMl = 2500,
            streakDays = 10,
            profile = testProfile,
            isPrivacyEnabled = false
        )

        assertEquals(WidgetStatusType.GOAL_REACHED, state.statusType)
        assertEquals("0 kcal left", state.primaryHeadline)
        assertEquals("Daily goal reached", state.subHeadline)
        assertEquals(SproutlingMood.ENERGIZED, state.sproutlingMood)
        assertEquals(10, state.energyBlocksFilled)
        assertEquals(0, state.remainingCalories)
    }

    @Test
    fun testOverGoalStateNonPunitive() {
        val meal = createMeal(calories = 2120.0, protein = 160.0, carbs = 220.0, fat = 75.0)

        val state = mapper.map(
            goal = testGoal,
            mealsWithItems = listOf(meal),
            waterTotalMl = 1000,
            streakDays = 7,
            profile = testProfile,
            isPrivacyEnabled = false
        )

        assertEquals(WidgetStatusType.OVER_GOAL, state.statusType)
        assertEquals("120 kcal over", state.primaryHeadline)
        assertEquals("Daily budget exceeded", state.subHeadline)
        assertEquals(SproutlingMood.CONTENT, state.sproutlingMood)
        assertEquals(10, state.energyBlocksFilled)
        assertEquals(-120, state.remainingCalories)
    }

    @Test
    fun testOnboardingIncompleteWhenMissingProfileOrGoal() {
        val missingProfileState = mapper.map(
            goal = testGoal,
            mealsWithItems = emptyList(),
            waterTotalMl = 0,
            streakDays = 0,
            profile = null,
            isPrivacyEnabled = false
        )
        assertEquals(WidgetStatusType.ONBOARDING_INCOMPLETE, missingProfileState.statusType)
        assertEquals("Finish setup", missingProfileState.primaryHeadline)
        assertEquals("Set your daily goals to begin", missingProfileState.subHeadline)
        assertEquals(SproutlingMood.WAVING, missingProfileState.sproutlingMood)

        val missingGoalState = mapper.map(
            goal = null,
            mealsWithItems = emptyList(),
            waterTotalMl = 0,
            streakDays = 0,
            profile = testProfile,
            isPrivacyEnabled = false
        )
        assertEquals(WidgetStatusType.ONBOARDING_INCOMPLETE, missingGoalState.statusType)
        assertEquals("Finish setup", missingGoalState.primaryHeadline)
    }

    @Test
    fun testLoadingState() {
        val state = mapper.map(
            goal = testGoal,
            mealsWithItems = emptyList(),
            waterTotalMl = 0,
            streakDays = 0,
            profile = testProfile,
            isPrivacyEnabled = false,
            isLoading = true
        )

        assertEquals(WidgetStatusType.LOADING, state.statusType)
        assertEquals("Updating...", state.primaryHeadline)
        assertEquals("Refreshing data", state.subHeadline)
        assertEquals(SproutlingMood.LOADING, state.sproutlingMood)
    }

    @Test
    fun testLoggedOutStateImmediatelyMasksData() {
        val meal = createMeal(calories = 1500.0, protein = 100.0, carbs = 150.0, fat = 50.0)
        val state = mapper.map(
            goal = testGoal,
            mealsWithItems = listOf(meal),
            waterTotalMl = 2000,
            streakDays = 15,
            profile = testProfile,
            isPrivacyEnabled = false,
            isLoggedIn = false
        )

        assertEquals(WidgetStatusType.ONBOARDING_INCOMPLETE, state.statusType)
        assertEquals("Sign in to begin", state.primaryHeadline)
        assertEquals("Connect your account", state.subHeadline)
        assertEquals(SproutlingMood.WAVING, state.sproutlingMood)
        assertEquals(0, state.consumedCalories)
    }

    @Test
    fun testPrivacyModeMasksNumericalValues() {
        val meal = createMeal(calories = 1420.0, protein = 98.0, carbs = 142.0, fat = 45.0)
        val state = mapper.map(
            goal = testGoal,
            mealsWithItems = listOf(meal),
            waterTotalMl = 750,
            streakDays = 5,
            profile = testProfile,
            isPrivacyEnabled = true
        )

        assertEquals(WidgetStatusType.PRIVACY, state.statusType)
        assertEquals("•••• kcal left", state.primaryHeadline)
        assertEquals("•••• / ••••", state.subHeadline)
        assertEquals(SproutlingMood.SECURE, state.sproutlingMood)
        assertTrue(state.isPrivacyEnabled)
        // Energy blocks remain visible proportionally per specification
        assertEquals(7, state.energyBlocksFilled)
    }

    @Test
    fun testMidnightRolloverWithInjectedClock() {
        val day1Instant = Instant.parse("2026-10-10T23:59:00Z")
        val clockDay1 = Clock.fixed(day1Instant, ZoneOffset.UTC)
        val mapperDay1 = WidgetStateMapper(clockDay1)

        assertEquals("2026-10-10", mapperDay1.getCurrentDateString())

        // Rollover to next day 00:01
        val day2Instant = Instant.parse("2026-10-11T00:01:00Z")
        val clockDay2 = Clock.fixed(day2Instant, ZoneOffset.UTC)
        val mapperDay2 = WidgetStateMapper(clockDay2)

        assertEquals("2026-10-11", mapperDay2.getCurrentDateString())

        // On new day, meals list for day 2 is empty -> returns fresh empty state
        val day2State = mapperDay2.map(
            goal = testGoal,
            mealsWithItems = emptyList(),
            waterTotalMl = 0,
            streakDays = 5,
            profile = testProfile,
            isPrivacyEnabled = false
        )
        assertEquals(WidgetStatusType.EMPTY, day2State.statusType)
        assertEquals("2,000 kcal left", day2State.primaryHeadline)
        assertEquals(SproutlingMood.RESTING, day2State.sproutlingMood)
    }

    @Test
    fun testTimezoneChangeWithInjectedClock() {
        val instantUtc = Instant.parse("2026-10-10T22:00:00Z")
        val utcClock = Clock.fixed(instantUtc, ZoneOffset.UTC)
        val tokyoClock = Clock.fixed(instantUtc, ZoneId.of("Asia/Tokyo")) // UTC+9 -> 2026-10-11 07:00

        val utcMapper = WidgetStateMapper(utcClock)
        val tokyoMapper = WidgetStateMapper(tokyoClock)

        assertEquals("2026-10-10", utcMapper.getCurrentDateString())
        assertEquals("2026-10-11", tokyoMapper.getCurrentDateString())
    }
}
