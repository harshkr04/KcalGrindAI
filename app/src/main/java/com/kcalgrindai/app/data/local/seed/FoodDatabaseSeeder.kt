package com.kcalgrindai.app.data.local.seed

import com.kcalgrindai.app.data.local.dao.FoodDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodDatabaseSeeder @Inject constructor(
    private val foodDao: FoodDao
) {
    private val mutex = Mutex()

    suspend fun seedIfNeeded(): Int = withContext(Dispatchers.IO) {
        mutex.withLock {
            val existingCount = foodDao.getFoodCountBySource(IndianFoodDataset.DATASET_SOURCE)
            if (existingCount == 0) {
                foodDao.insertFoods(IndianFoodDataset.foods)
                IndianFoodDataset.foods.size
            } else {
                existingCount
            }
        }
    }
}
