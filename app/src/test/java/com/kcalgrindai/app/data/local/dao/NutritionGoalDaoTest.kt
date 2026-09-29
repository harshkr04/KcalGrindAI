package com.kcalgrindai.app.data.local.dao

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.entity.NutritionGoalEntity
import com.kcalgrindai.app.data.local.entity.UserProfileEntity
import com.kcalgrindai.app.data.repository.UserProfileRepositoryImpl
import com.kcalgrindai.app.domain.model.ActivityLevel
import com.kcalgrindai.app.domain.model.GoalType
import com.kcalgrindai.app.domain.model.NutritionGoal
import com.kcalgrindai.app.domain.model.TargetBasis
import com.kcalgrindai.app.domain.model.UnitSystem
import com.kcalgrindai.app.domain.model.UserProfile
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NutritionGoalDaoTest {

    private lateinit var db: KcalGrindDatabase
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var nutritionGoalDao: NutritionGoalDao
    private lateinit var userProfileRepository: UserProfileRepositoryImpl

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KcalGrindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userProfileDao = db.userProfileDao()
        nutritionGoalDao = db.nutritionGoalDao()
        userProfileRepository = UserProfileRepositoryImpl(db, userProfileDao, nutritionGoalDao)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetNutritionGoal() = runTest {
        val profile = UserProfileEntity(
            id = 1L,
            goal = "lose",
            units = "metric",
            age = 30,
            heightCm = 175.0,
            weightKg = 70.0,
            goalWeightKg = 65.0,
            activityLevel = "moderate",
            dietTags = emptyList(),
            allergies = emptyList(),
            targetsSource = "recommended",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        userProfileDao.insertProfile(profile)

        val goal = NutritionGoalEntity(
            id = 1L,
            userId = 1L,
            calories = 1980,
            proteinG = 140.0,
            carbsG = 216.0,
            fatG = 62.0,
            waterLiters = 2.4,
            waterGlasses = 8,
            bmr = 1566.0,
            tdee = 2427.0,
            isCustom = false,
            updatedAt = 1500L
        )
        nutritionGoalDao.insertGoal(goal)

        val retrieved = nutritionGoalDao.getGoalByUserId(1L)
        assertNotNull(retrieved)
        assertEquals(1980, retrieved!!.calories)
        assertEquals(140.0, retrieved.proteinG, 0.0)
        assertEquals(2.4, retrieved.waterLiters, 0.0)
        assertEquals(8, retrieved.waterGlasses)
    }

    @Test
    fun insertGoalWithNonExistentUserIdThrowsForeignKeyConstraintException() = runTest {
        val nonExistentUserId = 9999L
        val goal = NutritionGoalEntity(
            id = 0L,
            userId = nonExistentUserId,
            calories = 2000,
            proteinG = 140.0,
            carbsG = 220.0,
            fatG = 60.0,
            waterLiters = 2.5,
            waterGlasses = 8,
            bmr = 1500.0,
            tdee = 2200.0,
            isCustom = false,
            updatedAt = 1000L
        )

        assertThrows(SQLiteConstraintException::class.java) {
            kotlinx.coroutines.runBlocking {
                nutritionGoalDao.insertGoal(goal)
            }
        }
    }

    @Test
    fun atomicSaveProfileAndGoalSucceedsEndToEnd() = runTest {
        val profile = UserProfile(
            id = 0L,
            goal = GoalType.LOSE,
            units = UnitSystem.METRIC,
            age = 28,
            heightCm = 178.0,
            weightKg = 75.0,
            goalWeightKg = 70.0,
            activityLevel = ActivityLevel.VERY,
            dietTags = listOf("keto"),
            allergies = listOf("peanuts"),
            targetsSource = TargetBasis.RECOMMENDED,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        val goal = NutritionGoal(
            id = 0L,
            userId = 0L,
            calories = 2270,
            proteinG = 150.0,
            carbsG = 50.0,
            fatG = 110.0,
            waterLiters = 3.0,
            waterGlasses = 10,
            bmr = 1600.0,
            tdee = 2720.0,
            isCustom = false,
            updatedAt = 1000L
        )

        val savedUserId = userProfileRepository.saveProfileAndGoal(profile, goal)
        assertTrue("Saved user ID should be > 0", savedUserId > 0L)

        val savedProfile = userProfileRepository.getProfile()
        assertNotNull(savedProfile)
        assertEquals(savedUserId, savedProfile!!.id)

        val savedGoal = nutritionGoalDao.getGoalByUserId(savedUserId)
        assertNotNull(savedGoal)
        assertEquals(savedUserId, savedGoal!!.userId)
        assertEquals(2270, savedGoal.calories)
    }

    @Test
    fun observeLatestGoal() = runTest {
        val profile = UserProfileEntity(
            id = 1L,
            goal = "muscle",
            units = "metric",
            age = 24,
            heightCm = 182.0,
            weightKg = 78.0,
            goalWeightKg = 82.0,
            activityLevel = "very",
            dietTags = emptyList(),
            allergies = emptyList(),
            targetsSource = "recommended",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        userProfileDao.insertProfile(profile)

        val goal1 = NutritionGoalEntity(
            id = 1L,
            userId = 1L,
            calories = 2500,
            proteinG = 150.0,
            carbsG = 300.0,
            fatG = 75.0,
            waterLiters = 3.0,
            waterGlasses = 10,
            bmr = 1700.0,
            tdee = 2600.0,
            isCustom = false,
            updatedAt = 1000L
        )
        nutritionGoalDao.insertGoal(goal1)

        val goal2 = NutritionGoalEntity(
            id = 2L,
            userId = 1L,
            calories = 2800,
            proteinG = 170.0,
            carbsG = 320.0,
            fatG = 80.0,
            waterLiters = 3.2,
            waterGlasses = 11,
            bmr = 1700.0,
            tdee = 2600.0,
            isCustom = true,
            updatedAt = 2000L
        )
        nutritionGoalDao.insertGoal(goal2)

        nutritionGoalDao.observeLatestGoal().test {
            val latest = awaitItem()
            assertNotNull(latest)
            assertEquals(2800, latest!!.calories)
            assertEquals(2L, latest.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteGoalByUserId() = runTest {
        val profile = UserProfileEntity(
            id = 1L,
            goal = "maintain",
            units = "metric",
            age = 25,
            heightCm = 170.0,
            weightKg = 60.0,
            goalWeightKg = 60.0,
            activityLevel = "moderate",
            dietTags = emptyList(),
            allergies = emptyList(),
            targetsSource = "recommended",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        userProfileDao.insertProfile(profile)

        val goal = NutritionGoalEntity(
            id = 1L,
            userId = 1L,
            calories = 2000,
            proteinG = 120.0,
            carbsG = 250.0,
            fatG = 60.0,
            waterLiters = 2.0,
            waterGlasses = 7,
            bmr = 1400.0,
            tdee = 2000.0,
            isCustom = false,
            updatedAt = 1000L
        )
        nutritionGoalDao.insertGoal(goal)
        nutritionGoalDao.deleteGoalByUserId(1L)

        val retrieved = nutritionGoalDao.getGoalByUserId(1L)
        assertNull(retrieved)
    }
}
