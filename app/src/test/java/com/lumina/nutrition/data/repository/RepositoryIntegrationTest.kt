package com.lumina.nutrition.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.lumina.nutrition.data.local.LuminaDatabase
import com.lumina.nutrition.domain.model.ActivityLevel
import com.lumina.nutrition.domain.model.FoodItem
import com.lumina.nutrition.domain.model.FoodLogItem
import com.lumina.nutrition.domain.model.FoodSource
import com.lumina.nutrition.domain.model.GoalType
import com.lumina.nutrition.domain.model.ItemSource
import com.lumina.nutrition.domain.model.LogSource
import com.lumina.nutrition.domain.model.MealLog
import com.lumina.nutrition.domain.model.MealType
import com.lumina.nutrition.domain.model.NutritionGoal
import com.lumina.nutrition.domain.model.TargetBasis
import com.lumina.nutrition.domain.model.UnitSystem
import com.lumina.nutrition.domain.model.UserProfile
import com.lumina.nutrition.domain.model.WeightEntry
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RepositoryIntegrationTest {

    private lateinit var db: LuminaDatabase
    private lateinit var userProfileRepository: UserProfileRepositoryImpl
    private lateinit var nutritionGoalRepository: NutritionGoalRepositoryImpl
    private lateinit var foodRepository: FoodRepositoryImpl
    private lateinit var mealLogRepository: MealLogRepositoryImpl
    private lateinit var weightRepository: WeightRepositoryImpl

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LuminaDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val dummyUsdaService = object : com.lumina.nutrition.data.remote.api.UsdaApiService {
            override suspend fun searchFoods(query: String, pageSize: Int, apiKey: String): com.lumina.nutrition.data.remote.dto.UsdaSearchResponse {
                return com.lumina.nutrition.data.remote.dto.UsdaSearchResponse()
            }
        }
        val dummyOffService = object : com.lumina.nutrition.data.remote.api.OpenFoodFactsApiService {
            override suspend fun getProductByBarcode(barcode: String): com.lumina.nutrition.data.remote.dto.OffProductResponse {
                return com.lumina.nutrition.data.remote.dto.OffProductResponse()
            }
        }

        userProfileRepository = UserProfileRepositoryImpl(db, db.userProfileDao(), db.nutritionGoalDao())
        nutritionGoalRepository = NutritionGoalRepositoryImpl(db.nutritionGoalDao())
        foodRepository = FoodRepositoryImpl(db.foodDao(), dummyUsdaService, dummyOffService)
        mealLogRepository = MealLogRepositoryImpl(db.mealLogDao(), db.foodLogItemDao())
        weightRepository = WeightRepositoryImpl(db.weightEntryDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testUserProfileAndNutritionGoalRepository() = runTest {
        val profile = UserProfile(
            id = 1L,
            goal = GoalType.LOSE,
            units = UnitSystem.METRIC,
            age = 29,
            heightCm = 178.0,
            weightKg = 82.0,
            goalWeightKg = 75.0,
            activityLevel = ActivityLevel.LIGHT,
            dietTags = listOf("balanced"),
            allergies = emptyList(),
            targetsSource = TargetBasis.RECOMMENDED,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val profileId = userProfileRepository.saveProfile(profile)
        assertEquals(1L, profileId)

        val goal = NutritionGoal(
            id = 1L,
            userId = profileId,
            calories = 2050,
            proteinG = 145.0,
            carbsG = 220.0,
            fatG = 65.0,
            waterLiters = 2.6,
            waterGlasses = 9,
            bmr = 1680.0,
            tdee = 2310.0,
            isCustom = false,
            updatedAt = 1000L
        )
        nutritionGoalRepository.saveGoal(goal)

        userProfileRepository.observeProfileWithGoal().test {
            val pair = awaitItem()
            assertNotNull(pair)
            assertEquals(GoalType.LOSE, pair!!.first.goal)
            assertNotNull(pair.second)
            assertEquals(2050, pair.second!!.calories)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testMealLogAndFoodRepositoryIntegration() = runTest {
        val food = FoodItem(
            id = 1L,
            source = FoodSource.USDA,
            name = "Oatmeal",
            servingDescription = "1 cup cooked",
            servingGrams = 234.0,
            calories = 158.0,
            proteinG = 6.0,
            carbsG = 27.0,
            fatG = 3.2,
            fiberG = 4.0,
            isUserCreated = false,
            createdAt = 1000L
        )
        foodRepository.saveFood(food)

        val meal = MealLog(
            id = 1L,
            date = "2026-09-01",
            mealType = MealType.BREAKFAST,
            totalCalories = 0.0, // calculated from items
            loggedAt = 1000L,
            source = LogSource.MANUAL,
            synced = true
        )
        val item = FoodLogItem(
            id = 1L,
            mealLogId = 1L,
            foodId = 1L,
            name = "Oatmeal",
            servingDescription = "1 cup cooked",
            servingGrams = 234.0,
            calories = 158.0,
            proteinG = 6.0,
            carbsG = 27.0,
            fatG = 3.2,
            fiberG = 4.0,
            source = ItemSource.VERIFIED,
            confidence = 1.0f,
            confirmed = true
        )

        val savedMealId = mealLogRepository.saveMeal(meal, listOf(item))
        assertEquals(1L, savedMealId)

        mealLogRepository.observeMealsWithItemsByDate("2026-09-01").test {
            val meals = awaitItem()
            assertEquals(1, meals.size)
            assertEquals(158.0, meals[0].meal.totalCalories, 0.1)
            assertEquals(1, meals[0].items.size)
            assertEquals("Oatmeal", meals[0].items[0].name)
            assertEquals(6.0, meals[0].totalProteinG, 0.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testWeightRepository() = runTest {
        val entry = WeightEntry(
            id = 1L,
            weightKg = 81.2,
            date = "2026-09-01",
            note = "Steady progress",
            loggedAt = 1000L
        )
        weightRepository.saveWeightEntry(entry)

        val latest = weightRepository.getLatestWeightEntry()
        assertNotNull(latest)
        assertEquals(81.2, latest!!.weightKg, 0.0)
    }
}
