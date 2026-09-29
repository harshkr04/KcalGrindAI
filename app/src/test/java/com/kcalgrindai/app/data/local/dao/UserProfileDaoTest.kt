package com.kcalgrindai.app.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.entity.NutritionGoalEntity
import com.kcalgrindai.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserProfileDaoTest {

    private lateinit var db: KcalGrindDatabase
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var nutritionGoalDao: NutritionGoalDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KcalGrindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userProfileDao = db.userProfileDao()
        nutritionGoalDao = db.nutritionGoalDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetProfile() = runTest {
        val profile = UserProfileEntity(
            id = 1L,
            goal = "lose",
            units = "metric",
            age = 28,
            heightCm = 180.0,
            weightKg = 75.0,
            goalWeightKg = 70.0,
            activityLevel = "moderate",
            dietTags = listOf("high_protein", "mediterranean"),
            allergies = listOf("peanuts"),
            targetsSource = "recommended",
            createdAt = 1000L,
            updatedAt = 1000L
        )

        val id = userProfileDao.insertProfile(profile)
        assertEquals(1L, id)

        val loaded = userProfileDao.getProfile()
        assertNotNull(loaded)
        assertEquals("lose", loaded!!.goal)
        assertEquals(180.0, loaded.heightCm, 0.0)
        assertEquals(listOf("high_protein", "mediterranean"), loaded.dietTags)
        assertEquals(listOf("peanuts"), loaded.allergies)
    }

    @Test
    fun updateProfile() = runTest {
        val profile = UserProfileEntity(
            id = 1L,
            goal = "maintain",
            units = "metric",
            age = 25,
            heightCm = 170.0,
            weightKg = 65.0,
            goalWeightKg = 65.0,
            activityLevel = "light",
            dietTags = emptyList(),
            allergies = emptyList(),
            targetsSource = "recommended",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        userProfileDao.insertProfile(profile)

        val updated = profile.copy(weightKg = 67.0, goal = "muscle", updatedAt = 2000L)
        userProfileDao.updateProfile(updated)

        val loaded = userProfileDao.getProfileById(1L)
        assertNotNull(loaded)
        assertEquals(67.0, loaded!!.weightKg, 0.0)
        assertEquals("muscle", loaded.goal)
        assertEquals(2000L, loaded.updatedAt)
    }

    @Test
    fun observeProfileAndWithGoal() = runTest {
        val profile = UserProfileEntity(
            id = 1L,
            goal = "lose",
            units = "metric",
            age = 30,
            heightCm = 175.0,
            weightKg = 80.0,
            goalWeightKg = 72.0,
            activityLevel = "moderate",
            dietTags = listOf("low_carb"),
            allergies = emptyList(),
            targetsSource = "recommended",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        userProfileDao.insertProfile(profile)

        userProfileDao.observeProfile().test {
            val item = awaitItem()
            assertNotNull(item)
            assertEquals("lose", item!!.goal)
            cancelAndIgnoreRemainingEvents()
        }

        val goal = NutritionGoalEntity(
            id = 1L,
            userId = 1L,
            calories = 2100,
            proteinG = 160.0,
            carbsG = 150.0,
            fatG = 70.0,
            waterLiters = 2.5,
            waterGlasses = 8,
            bmr = 1600.0,
            tdee = 2400.0,
            isCustom = false,
            updatedAt = 1000L
        )
        nutritionGoalDao.insertGoal(goal)

        userProfileDao.observeProfileWithGoal().test {
            val relation = awaitItem()
            assertNotNull(relation)
            assertEquals("lose", relation!!.profile.goal)
            assertNotNull(relation.goal)
            assertEquals(2100, relation.goal!!.calories)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteProfile() = runTest {
        val profile = UserProfileEntity(
            id = 1L,
            goal = "maintain",
            units = "metric",
            age = 22,
            heightCm = 165.0,
            weightKg = 55.0,
            goalWeightKg = 55.0,
            activityLevel = "sedentary",
            dietTags = emptyList(),
            allergies = emptyList(),
            targetsSource = "fallback",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        userProfileDao.insertProfile(profile)
        userProfileDao.deleteAllProfiles()

        val loaded = userProfileDao.getProfile()
        assertNull(loaded)
    }
}
