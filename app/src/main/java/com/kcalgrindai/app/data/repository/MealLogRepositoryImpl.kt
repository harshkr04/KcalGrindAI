package com.kcalgrindai.app.data.repository

import com.kcalgrindai.app.data.local.dao.FoodLogItemDao
import com.kcalgrindai.app.data.local.dao.MealLogDao
import com.kcalgrindai.app.data.mapper.toDomain
import com.kcalgrindai.app.data.mapper.toEntity
import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.MealLog
import com.kcalgrindai.app.domain.model.MealWithFoodItems
import com.kcalgrindai.app.domain.repository.MealLogRepository
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

    override fun observeStreakDays(): Flow<Int> {
        return mealLogDao.observeLoggedDates().map { dates ->
            calculateStreak(dates)
        }
    }

    override suspend fun getStreakDays(): Int {
        return calculateStreak(mealLogDao.getLoggedDates())
    }

    companion object {
        fun calculateStreak(dates: List<String>, today: java.time.LocalDate = java.time.LocalDate.now()): Int {
            val dateSet = dates.mapNotNull {
                try { java.time.LocalDate.parse(it) } catch (_: Exception) { null }
            }.toSet()

            var checkDate = if (dateSet.contains(today)) {
                today
            } else if (dateSet.contains(today.minusDays(1))) {
                today.minusDays(1)
            } else {
                return 0
            }

            var streak = 0
            while (dateSet.contains(checkDate)) {
                streak++
                checkDate = checkDate.minusDays(1)
            }
            return streak
        }
    }
}
