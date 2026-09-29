package com.kcalgrindai.app.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.entity.FoodEntity
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
class FoodDaoTest {

    private lateinit var db: KcalGrindDatabase
    private lateinit var foodDao: FoodDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KcalGrindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        foodDao = db.foodDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetFood() = runTest {
        val food = FoodEntity(
            id = 1L,
            source = "usda",
            externalId = "12345",
            name = "Greek Yogurt Plain",
            brand = "Chobani",
            servingDescription = "1 cup",
            servingGrams = 170.0,
            calories = 130.0,
            proteinG = 15.0,
            carbsG = 6.0,
            fatG = 0.0,
            fiberG = 0.0,
            barcodeUpc = "012345678901",
            isUserCreated = false,
            createdAt = 1000L
        )

        foodDao.insertFood(food)

        val retrieved = foodDao.getFoodById(1L)
        assertNotNull(retrieved)
        assertEquals("Greek Yogurt Plain", retrieved!!.name)
        assertEquals("Chobani", retrieved.brand)
        assertEquals("012345678901", retrieved.barcodeUpc)

        val byBarcode = foodDao.getFoodByBarcode("012345678901")
        assertNotNull(byBarcode)
        assertEquals(1L, byBarcode!!.id)
    }

    @Test
    fun searchFoodsAndObserve() = runTest {
        val foods = listOf(
            FoodEntity(
                id = 1L,
                source = "usda",
                name = "Grilled Chicken Breast",
                servingDescription = "100g",
                servingGrams = 100.0,
                calories = 165.0,
                proteinG = 31.0,
                carbsG = 0.0,
                fatG = 3.6,
                fiberG = 0.0,
                isUserCreated = false,
                createdAt = 1000L
            ),
            FoodEntity(
                id = 2L,
                source = "custom",
                name = "Homemade Chicken Soup",
                servingDescription = "1 bowl",
                servingGrams = 250.0,
                calories = 200.0,
                proteinG = 18.0,
                carbsG = 12.0,
                fatG = 6.0,
                fiberG = 2.0,
                isUserCreated = true,
                createdAt = 2000L
            ),
            FoodEntity(
                id = 3L,
                source = "openfoodfacts",
                name = "Brown Rice",
                servingDescription = "1 cup",
                servingGrams = 195.0,
                calories = 216.0,
                proteinG = 5.0,
                carbsG = 45.0,
                fatG = 1.8,
                fiberG = 3.5,
                isUserCreated = false,
                createdAt = 3000L
            )
        )
        foodDao.insertFoods(foods)

        val searchResults = foodDao.searchFoods("Chicken")
        assertEquals(2, searchResults.size)

        foodDao.observeUserCreatedFoods().test {
            val userFoods = awaitItem()
            assertEquals(1, userFoods.size)
            assertEquals("Homemade Chicken Soup", userFoods[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteFoodById() = runTest {
        val food = FoodEntity(
            id = 1L,
            source = "custom",
            name = "Protein Shake",
            servingDescription = "1 scoop",
            servingGrams = 30.0,
            calories = 120.0,
            proteinG = 24.0,
            carbsG = 2.0,
            fatG = 1.0,
            fiberG = 1.0,
            isUserCreated = true,
            createdAt = 1000L
        )
        foodDao.insertFood(food)
        foodDao.deleteFoodById(1L)

        val retrieved = foodDao.getFoodById(1L)
        assertNull(retrieved)
    }
}
