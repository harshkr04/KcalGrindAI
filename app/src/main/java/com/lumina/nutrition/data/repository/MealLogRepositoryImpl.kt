package com.lumina.nutrition.data.repository

import com.lumina.nutrition.data.local.dao.FoodLogItemDao
import com.lumina.nutrition.data.local.dao.MealLogDao
import com.lumina.nutrition.data.mapper.toDomain
import com.lumina.nutrition.data.mapper.toEntity
import com.lumina.nutrition.domain.model.FoodLogItem
import com.lumina.nutrition.domain.model.MealLog
import com.lumina.nutrition.domain.model.MealWithFoodItems
import com.lumina.nutrition.domain.repository.MealLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealLogRepositoryImpl @Inject constructor(
    private val mealLogDao: MealLogDao,
    private val foodLogItemDao: FoodLogItemDao
) : MealLogRepository {

    override suspend fun saveMeal(meal: MealLog, items: List<FoodLogItem>): Long {
        val calculatedCalories = if (meal.totalCalories > 0) {
            meal.totalCalories
        } else {
            items.sumOf { it.calories }
        }
        val mealToSave = meal.copy(totalCalories = calculatedCalories)
        val mealId = if (mealToSave.id == 0L) {
            mealLogDao.insertMealLog(mealToSave.toEntity())
        } else {
            mealLogDao.upsertMealLog(mealToSave.toEntity())
            mealToSave.id
        }

        // Delete existing items for update scenario or save new items
        foodLogItemDao.deleteItemsForMeal(mealId)
        val itemsWithMealId = items.map { it.copy(mealLogId = mealId).toEntity() }
        if (itemsWithMealId.isNotEmpty()) {
            foodLogItemDao.insertItems(itemsWithMealId)
        }
        return mealId
    }

    override suspend fun updateMeal(meal: MealLog) {
        mealLogDao.updateMealLog(meal.toEntity())
    }

    override suspend fun deleteMeal(mealId: Long) {
        mealLogDao.deleteMealLogById(mealId)
    }

    override suspend fun getMealById(mealId: Long): MealWithFoodItems? {
        return mealLogDao.getMealWithItemsById(mealId)?.toDomain()
    }

    override fun observeMealsWithItemsByDate(date: String): Flow<List<MealWithFoodItems>> {
        return mealLogDao.observeMealsWithItemsByDate(date).map { list -> list.map { it.toDomain() } }
    }

    override fun observeMealsWithItemsInRange(
        startDate: String,
        endDate: String
    ): Flow<List<MealWithFoodItems>> {
        return mealLogDao.observeMealsWithItemsInRange(startDate, endDate).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveFoodLogItem(item: FoodLogItem): Long {
        return foodLogItemDao.upsertItem(item.toEntity())
    }

    override suspend fun updateFoodLogItem(item: FoodLogItem) {
        foodLogItemDao.updateItem(item.toEntity())
    }

    override suspend fun deleteFoodLogItem(itemId: Long) {
        foodLogItemDao.deleteItemById(itemId)
    }

    override fun observeItemsForMeal(mealId: Long): Flow<List<FoodLogItem>> {
        return foodLogItemDao.observeItemsForMeal(mealId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getUnsyncedMeals(): List<MealLog> {
        return mealLogDao.getUnsyncedMealLogs().map { it.toDomain() }
    }
}
