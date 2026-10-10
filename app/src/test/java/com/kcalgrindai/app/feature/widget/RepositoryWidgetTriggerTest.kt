package com.kcalgrindai.app.feature.widget

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kcalgrindai.app.core.util.WidgetUpdater
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.repository.MealLogRepositoryImpl
import com.kcalgrindai.app.data.repository.NutritionGoalRepositoryImpl
import com.kcalgrindai.app.data.repository.WaterLogRepositoryImpl
import com.kcalgrindai.app.domain.model.LogSource
import com.kcalgrindai.app.domain.model.MealLog
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.model.NutritionGoal
import dagger.Lazy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RepositoryWidgetTriggerTest {

    private lateinit var db: KcalGrindDatabase
    private val fakeUpdater = object : WidgetUpdater {
        var updateRequestCount = 0
        var clearSignOutCount = 0

        override fun requestUpdate() {
            updateRequestCount++
        }

        override fun clearWidgetDataOnSignOut() {
            clearSignOutCount++
        }
    }

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KcalGrindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testMealLogRepositoryTriggersWidgetUpdate() = runTest {
        val repo = MealLogRepositoryImpl(
            mealLogDao = db.mealLogDao(),
            foodLogItemDao = db.foodLogItemDao(),
            database = db,
            widgetUpdater = Lazy { fakeUpdater }
        )

        val meal = MealLog(
            id = 0L,
            date = "2026-10-10",
            mealType = MealType.BREAKFAST,
            totalCalories = 400.0,
            loggedAt = 1000L,
            source = LogSource.MANUAL,
            synced = true
        )

        val id = repo.saveMeal(meal, emptyList())
        assertEquals(1, fakeUpdater.updateRequestCount)

        repo.deleteMeal(id)
        assertEquals(2, fakeUpdater.updateRequestCount)
    }

    @Test
    fun testWaterLogRepositoryTriggersWidgetUpdate() = runTest {
        val repo = WaterLogRepositoryImpl(
            waterLogDao = db.waterLogDao(),
            widgetUpdater = Lazy { fakeUpdater }
        )

        val id = repo.logWater(250, "2026-10-10")
        assertEquals(1, fakeUpdater.updateRequestCount)

        repo.deleteWaterLog(id)
        assertEquals(2, fakeUpdater.updateRequestCount)
    }

    @Test
    fun testNutritionGoalRepositoryTriggersWidgetUpdate() = runTest {
        val repo = NutritionGoalRepositoryImpl(
            nutritionGoalDao = db.nutritionGoalDao(),
            widgetUpdater = Lazy { fakeUpdater }
        )

        val profile = com.kcalgrindai.app.data.local.entity.UserProfileEntity(
            id = 1L,
            goal = "maintain",
            units = "metric",
            age = 25,
            heightCm = 175.0,
            weightKg = 70.0,
            goalWeightKg = 70.0,
            activityLevel = "moderate",
            dietTags = emptyList(),
            allergies = emptyList(),
            targetsSource = "recommended",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        db.userProfileDao().insertProfile(profile)

        val goal = NutritionGoal(
            id = 1L,
            userId = 1L,
            calories = 2000,
            proteinG = 150.0,
            carbsG = 200.0,
            fatG = 65.0,
            waterLiters = 2.5,
            waterGlasses = 10,
            bmr = 1700.0,
            tdee = 2300.0,
            isCustom = false,
            updatedAt = 1000L
        )

        repo.saveGoal(goal)
        assertEquals(1, fakeUpdater.updateRequestCount)

        repo.deleteGoalByUserId(1L)
        assertEquals(2, fakeUpdater.updateRequestCount)
    }
}
