package com.kcalgrindai.app.feature.widget

import com.kcalgrindai.app.domain.mapper.WidgetStateMapper
import com.kcalgrindai.app.domain.model.ActivityLevel
import com.kcalgrindai.app.domain.model.AuthUser
import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.GoalType
import com.kcalgrindai.app.domain.model.ItemSource
import com.kcalgrindai.app.domain.model.LogSource
import com.kcalgrindai.app.domain.model.MealLog
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.model.NutritionGoal
import com.kcalgrindai.app.domain.model.SproutlingMood
import com.kcalgrindai.app.domain.model.TargetBasis
import com.kcalgrindai.app.domain.model.UnitSystem
import com.kcalgrindai.app.domain.model.UserProfile
import com.kcalgrindai.app.domain.model.WidgetStatusType
import com.kcalgrindai.app.domain.model.WidgetUiState
import com.kcalgrindai.app.fakes.FakeAuthRepository
import com.kcalgrindai.app.fakes.FakeMealLogRepository
import com.kcalgrindai.app.fakes.FakeNutritionGoalRepository
import com.kcalgrindai.app.fakes.FakeUserPreferencesRepository
import com.kcalgrindai.app.fakes.FakeUserProfileRepository
import com.kcalgrindai.app.fakes.FakeWaterLogRepository
import com.kcalgrindai.app.feature.widget.di.WidgetEntryPoint
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class KcalGrindAppWidgetSessionTest {

    private val fixedClock = Clock.fixed(Instant.parse("2026-10-11T12:00:00Z"), ZoneId.of("UTC"))
    private lateinit var mapper: WidgetStateMapper
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var nutritionGoalRepository: FakeNutritionGoalRepository
    private lateinit var mealLogRepository: FakeMealLogRepository
    private lateinit var waterLogRepository: FakeWaterLogRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var userPreferencesRepository: FakeUserPreferencesRepository

    private val widget = KcalGrindAppWidget()

    private fun createEntryPoint() = object : WidgetEntryPoint {
        override fun mealLogRepository() = mealLogRepository
        override fun waterLogRepository() = waterLogRepository
        override fun nutritionGoalRepository() = nutritionGoalRepository
        override fun userProfileRepository() = userProfileRepository
        override fun userPreferencesRepository() = userPreferencesRepository
        override fun authRepository() = authRepository
        override fun widgetStateMapper() = mapper
    }

    @Before
    fun setUp() {
        mapper = WidgetStateMapper(fixedClock)
        userProfileRepository = FakeUserProfileRepository()
        nutritionGoalRepository = FakeNutritionGoalRepository()
        mealLogRepository = FakeMealLogRepository()
        waterLogRepository = FakeWaterLogRepository()
        authRepository = FakeAuthRepository()
        userPreferencesRepository = FakeUserPreferencesRepository()
    }

    private fun createTestProfile(firebaseUid: String? = null) = UserProfile(
        id = 1L,
        firebaseUid = firebaseUid,
        email = if (firebaseUid != null) "alex@example.com" else null,
        firstName = "Alex",
        goal = GoalType.MAINTAIN,
        units = UnitSystem.METRIC,
        age = 28,
        heightCm = 175.0,
        weightKg = 70.0,
        goalWeightKg = 70.0,
        activityLevel = ActivityLevel.MODERATE,
        dietTags = emptyList(),
        allergies = emptyList(),
        targetsSource = TargetBasis.RECOMMENDED,
        createdAt = fixedClock.millis(),
        updatedAt = fixedClock.millis()
    )

    private fun createTestGoal() = NutritionGoal(
        id = 1L,
        userId = 1L,
        calories = 2440,
        proteinG = 112.0,
        carbsG = 327.0,
        fatG = 76.0,
        waterLiters = 2.4,
        waterGlasses = 8,
        bmr = 1650.0,
        tdee = 2440.0,
        isCustom = false,
        updatedAt = fixedClock.millis()
    )

    @Test
    fun testLocalGuestProfileLoadsRealNutritionStateWhenFirebaseAuthIsNull() = runTest {
        // 1. Local guest profile (firebaseUid == null) exists while Firebase currentUser == null
        val guestProfile = createTestProfile(firebaseUid = null)
        userProfileRepository.saveProfile(guestProfile)
        nutritionGoalRepository.saveGoal(createTestGoal())
        authRepository.currentUser = null

        // Log a meal and water
        val meal = MealLog(
            id = 1L,
            mealType = MealType.BREAKFAST,
            date = "2026-10-11",
            totalCalories = 340.0,
            loggedAt = fixedClock.millis(),
            source = LogSource.MANUAL,
            synced = false
        )
        val foodItem = FoodLogItem(
            id = 1L,
            mealLogId = 1L,
            foodId = 1L,
            name = "Oatmeal with berries",
            brand = null,
            servingDescription = "1 bowl",
            servingGrams = 150.0,
            calories = 340.0,
            proteinG = 12.0,
            carbsG = 58.0,
            fatG = 6.0,
            fiberG = 8.0,
            source = ItemSource.MANUAL,
            confidence = 1.0f,
            confirmed = true
        )
        mealLogRepository.saveMeal(meal, listOf(foodItem))
        waterLogRepository.logWater(500, "2026-10-11")

        val state = widget.loadWidgetState(createEntryPoint())

        // Widget must NOT return LoggedOut
        assertNotEquals(WidgetUiState.LoggedOut, state)
        assertEquals(2440, state.targetCalories)
        assertEquals(340, state.consumedCalories)
        assertEquals(2100, state.remainingCalories)
        assertEquals(12.0, state.proteinGrams, 0.1)
        assertEquals(58.0, state.carbsGrams, 0.1)
        assertEquals(6.0, state.fatGrams, 0.1)
        assertEquals(500, state.waterMl)
        assertEquals(2, state.waterGlasses)
        assertEquals(WidgetStatusType.NORMAL, state.statusType)
    }

    @Test
    fun testNoValidLocalProfileDisplaysLoggedOutState() = runTest {
        // 2. No valid local profile in repository
        userProfileRepository.deleteProfile()
        authRepository.currentUser = null

        val state = widget.loadWidgetState(createEntryPoint())

        assertEquals(WidgetUiState.LoggedOut, state)
    }

    @Test
    fun testAuthenticatedCloudUserWithValidProfileLoadsState() = runTest {
        // 3. Authenticated user with valid profile
        val cloudUid = "firebase-cloud-uid-101"
        val cloudProfile = createTestProfile(firebaseUid = cloudUid)
        userProfileRepository.saveProfile(cloudProfile)
        nutritionGoalRepository.saveGoal(createTestGoal())
        authRepository.currentUser = AuthUser(
            uid = cloudUid,
            email = "alex@example.com",
            isAnonymous = false,
            displayName = "Alex"
        )

        val state = widget.loadWidgetState(createEntryPoint())

        assertNotEquals(WidgetUiState.LoggedOut, state)
        assertEquals(2440, state.targetCalories)
        assertEquals(2440, state.remainingCalories)
    }

    @Test
    fun testSignOutAndAccountTransitionDoesNotExposePreviousUserData() = runTest {
        // 4. Cloud profile exists, but user signed out (authRepository.currentUser == null)
        val cloudProfile = createTestProfile(firebaseUid = "cloud-user-abc")
        userProfileRepository.saveProfile(cloudProfile)
        nutritionGoalRepository.saveGoal(createTestGoal())
        authRepository.currentUser = null // Signed out!

        val signedOutState = widget.loadWidgetState(createEntryPoint())
        assertEquals(WidgetUiState.LoggedOut, signedOutState)

        // Account transition: different cloud user logs in before local profile has synced
        authRepository.currentUser = AuthUser(
            uid = "different-user-xyz",
            email = "other@example.com",
            isAnonymous = false,
            displayName = "Other"
        )
        val mismatchedState = widget.loadWidgetState(createEntryPoint())
        assertEquals(WidgetUiState.LoggedOut, mismatchedState)
    }

    @Test
    fun testPrivacyModeHidesAllCaloriesMacrosAndProgressIndicators() = runTest {
        // 5. Privacy mode enabled
        val guestProfile = createTestProfile(firebaseUid = null)
        userProfileRepository.saveProfile(guestProfile)
        nutritionGoalRepository.saveGoal(createTestGoal())
        userPreferencesRepository.setWidgetPrivacyEnabled(true)

        val state = widget.loadWidgetState(createEntryPoint())

        assertTrue(state.isPrivacyEnabled)
        assertEquals(WidgetStatusType.PRIVACY, state.statusType)
        assertEquals(0, state.targetCalories)
        assertEquals(0, state.consumedCalories)
        assertEquals(0, state.remainingCalories)
        assertEquals(0, state.energyBlocksFilled)
        assertEquals(0.0, state.proteinGrams, 0.0)
        assertEquals(0.0f, state.proteinFraction, 0.0f)
        assertEquals(0.0, state.carbsGrams, 0.0)
        assertEquals(0.0f, state.carbsFraction, 0.0f)
        assertEquals(0.0, state.fatGrams, 0.0)
        assertEquals(0.0f, state.fatFraction, 0.0f)
        assertEquals(0, state.waterMl)
        assertEquals(0, state.waterGlasses)
        assertEquals("•••• kcal left", state.primaryHeadline)
        assertEquals(SproutlingMood.SECURE, state.sproutlingMood)
    }

    @Test
    fun testMissingGoalOrEmptyMealHistoryHandledGracefully() = runTest {
        // 6. Missing goal / empty meal history
        val guestProfile = createTestProfile(firebaseUid = null)
        userProfileRepository.saveProfile(guestProfile)
        // No goal saved in nutritionGoalRepository (null)

        val state = widget.loadWidgetState(createEntryPoint())

        // Mapper handles missing goal with ONBOARDING_INCOMPLETE without throwing
        assertEquals(WidgetStatusType.ONBOARDING_INCOMPLETE, state.statusType)
        assertEquals("Finish setup", state.primaryHeadline)
        assertEquals("Set your daily goals to begin", state.subHeadline)
    }
}
