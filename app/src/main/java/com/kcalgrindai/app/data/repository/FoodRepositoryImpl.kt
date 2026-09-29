package com.kcalgrindai.app.data.repository

import com.kcalgrindai.app.BuildConfig
import com.kcalgrindai.app.data.local.dao.FoodDao
import com.kcalgrindai.app.data.mapper.toDomain
import com.kcalgrindai.app.data.mapper.toEntity
import com.kcalgrindai.app.data.remote.api.OpenFoodFactsApiService
import com.kcalgrindai.app.data.remote.api.UsdaApiService
import com.kcalgrindai.app.domain.model.FoodItem
import com.kcalgrindai.app.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao,
    private val usdaApiService: UsdaApiService,
    private val openFoodFactsApiService: OpenFoodFactsApiService,
    private val foodDatabaseSeeder: com.kcalgrindai.app.data.local.seed.FoodDatabaseSeeder? = null
) : FoodRepository {

    override suspend fun saveFood(food: FoodItem): Long {
        return foodDao.upsertFood(food.toEntity())
    }

    override suspend fun saveFoods(foods: List<FoodItem>): List<Long> {
        return foodDao.insertFoods(foods.map { it.toEntity() })
    }

    override suspend fun getFoodById(id: Long): FoodItem? {
        return foodDao.getFoodById(id)?.toDomain()
    }

    override suspend fun getFoodByBarcode(barcode: String): FoodItem? {
        return foodDao.getFoodByBarcode(barcode)?.toDomain()
    }

    override suspend fun searchFoods(query: String, limit: Int): List<FoodItem> {
        foodDatabaseSeeder?.seedIfNeeded()
        return foodDao.searchFoods(query, limit).map { it.toDomain() }
    }

    override suspend fun searchFoodsOnline(query: String): Result<List<FoodItem>> {
        foodDatabaseSeeder?.seedIfNeeded()
        val localMatches = foodDao.searchFoods(query, 30).map { it.toDomain() }

        return try {
            val response = usdaApiService.searchFoods(
                query = query,
                pageSize = 25,
                apiKey = BuildConfig.USDA_API_KEY
            )
            val domainItems = response.foods.map { it.toDomain() }
            if (domainItems.isNotEmpty()) {
                val entityList = domainItems.map { it.toEntity() }
                foodDao.insertFoods(entityList)
            }
            // Merge local matches first (e.g. IFCT Indian foods), then remote USDA items, deduplicating by name
            val combined = (localMatches + domainItems).distinctBy { it.name.lowercase().trim() }
            Result.success(combined)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun lookupBarcode(barcode: String): Result<FoodItem?> {
        // 1. Check local Room cache first
        val cached = foodDao.getFoodByBarcode(barcode)
        if (cached != null) {
            return Result.success(cached.toDomain())
        }

        // 2. Fetch from Open Food Facts API
        return try {
            val response = openFoodFactsApiService.getProductByBarcode(barcode)
            if (response.status == 1 && response.product != null) {
                val domainFood = response.toDomain(barcode)
                if (domainFood != null) {
                    val id = foodDao.upsertFood(domainFood.toEntity())
                    Result.success(domainFood.copy(id = id))
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeFoodsByName(query: String): Flow<List<FoodItem>> {
        return foodDao.observeFoodsByName(query).map { list -> list.map { it.toDomain() } }
    }

    override fun observeUserCreatedFoods(): Flow<List<FoodItem>> {
        return foodDao.observeUserCreatedFoods().map { list -> list.map { it.toDomain() } }
    }

    override fun observeAllFoods(): Flow<List<FoodItem>> {
        return foodDao.observeAllFoods().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun deleteFood(food: FoodItem) {
        foodDao.deleteFood(food.toEntity())
    }

    override suspend fun deleteFoodById(id: Long) {
        foodDao.deleteFoodById(id)
    }
}
