package com.kcalgrindai.app.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.dao.FoodDao
import com.kcalgrindai.app.data.local.seed.FoodDatabaseSeeder
import com.kcalgrindai.app.data.local.seed.IndianFoodDataset
import com.kcalgrindai.app.data.remote.api.OpenFoodFactsApiService
import com.kcalgrindai.app.data.remote.api.UsdaApiService
import com.kcalgrindai.app.data.remote.dto.OffProductResponse
import com.kcalgrindai.app.data.remote.dto.UsdaFoodItem
import com.kcalgrindai.app.data.remote.dto.UsdaSearchResponse
import com.kcalgrindai.app.domain.model.FoodItem
import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.FoodSource
import com.kcalgrindai.app.domain.model.ItemSource
import com.kcalgrindai.app.domain.model.LogSource
import com.kcalgrindai.app.domain.model.MealLog
import com.kcalgrindai.app.domain.model.MealType
import org.junit.Assert.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

private class FakeTestUsdaApiService(var shouldFail: Boolean = false) : UsdaApiService {
    override suspend fun searchFoods(query: String, pageSize: Int, apiKey: String): UsdaSearchResponse {
        if (shouldFail) {
            throw IOException("Network unavailable (offline test)")
        }
        return UsdaSearchResponse(
            totalHits = 1,
            currentPage = 1,
            totalPages = 1,
            foods = listOf(
                UsdaFoodItem(
                    fdcId = 99999L,
                    description = "Commercial Canned Lentil Dal",
                    brandOwner = "US Brand",
                    servingSize = 200.0,
                    servingSizeUnit = "g",
                    foodNutrients = emptyList()
                )
            )
        )
    }
}

private class FakeTestOffApiService : OpenFoodFactsApiService {
    override suspend fun getProductByBarcode(barcode: String): OffProductResponse {
        return OffProductResponse(status = 0, product = null)
    }
}

@RunWith(RobolectricTestRunner::class)
class FoodRepositoryOfflineSearchTest {

    private lateinit var db: KcalGrindDatabase
    private lateinit var foodDao: FoodDao
    private lateinit var seeder: FoodDatabaseSeeder
    private lateinit var fakeUsdaApi: FakeTestUsdaApiService
    private lateinit var fakeOffApi: FakeTestOffApiService
    private lateinit var foodRepository: FoodRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KcalGrindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        foodDao = db.foodDao()
        seeder = FoodDatabaseSeeder(foodDao)
        fakeUsdaApi = FakeTestUsdaApiService(shouldFail = false)
        fakeOffApi = FakeTestOffApiService()

        foodRepository = FoodRepositoryImpl(
            foodDao = foodDao,
            usdaApiService = fakeUsdaApi,
            openFoodFactsApiService = fakeOffApi,
            foodDatabaseSeeder = seeder
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `seeder populates Indian food dataset accurately and is idempotent`() = runTest {
        // Initial count is 0
        assertEquals(0, foodDao.getFoodCountBySource("ifct") + foodDao.getFoodCountBySource("curated_recipe"))

        // First seed
        val seededCount = seeder.seedIfNeeded()
        assertEquals(IndianFoodDataset.foods.size, seededCount)
        assertTrue(seededCount >= 100)
        assertEquals(seededCount, foodDao.getFoodCountBySource("ifct") + foodDao.getFoodCountBySource("curated_recipe"))

        // Second seed does not duplicate rows
        val secondSeedCount = seeder.seedIfNeeded()
        assertEquals(seededCount, secondSeedCount)
        assertEquals(seededCount, foodDao.getFoodCountBySource("ifct") + foodDao.getFoodCountBySource("curated_recipe"))
    }

    @Test
    fun `offline search returns verified results for dal, roti, biryani, idli, and paneer`() = runTest {
        // 1. Search "dal"
        val dalItems = foodRepository.searchFoods("dal")
        assertTrue("Expected non-empty dal results", dalItems.isNotEmpty())
        assertTrue("Expected curated recipe dal items", dalItems.any { it.source == FoodSource.CURATED_RECIPE })

        // 2. Search "roti"
        val rotiItems = foodRepository.searchFoods("roti")
        assertTrue("Expected non-empty roti results", rotiItems.isNotEmpty())
        assertTrue("Expected curated recipe roti items", rotiItems.any { it.source == FoodSource.CURATED_RECIPE })

        // 3. Search "biryani"
        val biryaniItems = foodRepository.searchFoods("biryani")
        assertTrue("Expected non-empty biryani results", biryaniItems.isNotEmpty())
        assertTrue("Expected Chicken Biryani", biryaniItems.any { it.name.contains("Biryani", ignoreCase = true) })

        // 4. Search "idli"
        val idliItems = foodRepository.searchFoods("idli")
        assertTrue("Expected non-empty idli results", idliItems.isNotEmpty())
        assertTrue("Expected Plain Idli", idliItems.any { it.name.contains("Idli", ignoreCase = true) })

        // 5. Search "paneer"
        val paneerItems = foodRepository.searchFoods("paneer")
        assertTrue("Expected non-empty paneer results", paneerItems.isNotEmpty())
        assertTrue("Expected Paneer Butter Masala", paneerItems.any { it.name.contains("Paneer", ignoreCase = true) })
        assertTrue("Expected Raw Paneer from IFCT", paneerItems.any { it.source == FoodSource.IFCT })
    }

    @Test
    fun `searchFoodsOnline propagates network failure when offline`() = runTest {
        fakeUsdaApi.shouldFail = true
        val result = foodRepository.searchFoodsOnline("dal")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `online search merges local IFCT foods with live USDA results`() = runTest {
        fakeUsdaApi.shouldFail = false

        val result = foodRepository.searchFoodsOnline("dal")
        assertTrue(result.isSuccess)
        val items = result.getOrThrow()

        // Should contain local IFCT items AND the USDA item
        assertTrue("Should contain local curated dal items", items.any { it.source == FoodSource.CURATED_RECIPE })
        assertTrue("Should contain USDA item", items.any { it.source == FoodSource.USDA })
    }

    @Test
    fun customFoodSavedWithUserEnteredSource_appearsInOfflineSearch_andCanBeLoggedToDiary() = runTest {
        val customFood = FoodItem(
            id = 0L,
            source = FoodSource.USER_ENTERED,
            name = "Harsh Homemade Khichdi",
            brand = "Home",
            servingDescription = "1 big bowl",
            servingGrams = 300.0,
            calories = 380.0,
            proteinG = 14.0,
            carbsG = 65.0,
            fatG = 7.0,
            fiberG = 6.0,
            sugarG = 2.0,
            sodiumMg = 300.0,
            isUserCreated = true,
            createdAt = System.currentTimeMillis()
        )

        val savedId = foodRepository.saveFood(customFood)
        assertTrue(savedId > 0)

        val searchResults = foodRepository.searchFoods("Khichdi")
        assertTrue(searchResults.any { it.name == "Harsh Homemade Khichdi" && it.source == FoodSource.USER_ENTERED && it.isUserCreated })

        val mealLogDao = db.mealLogDao()
        val foodLogItemDao = db.foodLogItemDao()
        val mealRepo = MealLogRepositoryImpl(mealLogDao, foodLogItemDao)

        val meal = MealLog(
            id = 0L,
            date = "2026-11-04",
            mealType = MealType.DINNER,
            totalCalories = 380.0,
            loggedAt = System.currentTimeMillis(),
            source = LogSource.MANUAL,
            synced = false
        )
        val logItem = FoodLogItem(
            id = 0L,
            mealLogId = 0L,
            foodId = savedId,
            name = customFood.name,
            brand = customFood.brand,
            servingDescription = customFood.servingDescription,
            servingGrams = customFood.servingGrams,
            calories = customFood.calories,
            proteinG = customFood.proteinG,
            carbsG = customFood.carbsG,
            fatG = customFood.fatG,
            fiberG = customFood.fiberG,
            sugarG = customFood.sugarG,
            sodiumMg = customFood.sodiumMg,
            source = ItemSource.USER_ENTERED,
            confidence = 1.0f,
            confirmed = true
        )

        val mealId = mealRepo.saveMeal(meal, listOf(logItem))
        val loggedMeal = mealRepo.getMealById(mealId)
        assertNotNull(loggedMeal)
        assertEquals(380.0, loggedMeal!!.meal.totalCalories, 0.001)
        assertEquals(1, loggedMeal.items.size)
        assertEquals(ItemSource.USER_ENTERED, loggedMeal.items[0].source)
        assertEquals(380.0, loggedMeal.items[0].calories, 0.001)
        assertEquals(14.0, loggedMeal.items[0].proteinG, 0.001)
    }
}
