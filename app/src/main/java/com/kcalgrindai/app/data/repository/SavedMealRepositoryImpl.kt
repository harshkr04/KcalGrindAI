package com.kcalgrindai.app.data.repository

import androidx.room.withTransaction
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.dao.SavedMealDao
import com.kcalgrindai.app.data.local.entity.SavedMealEntity
import com.kcalgrindai.app.data.local.entity.SavedMealItemEntity
import com.kcalgrindai.app.data.mapper.toDomain
import com.kcalgrindai.app.domain.model.FoodLogItem
import com.kcalgrindai.app.domain.model.LogSource
import com.kcalgrindai.app.domain.model.MealLog
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.model.SavedMealWithItems
import com.kcalgrindai.app.domain.repository.MealLogRepository
import com.kcalgrindai.app.domain.repository.SavedMealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedMealRepositoryImpl @Inject constructor(
    private val savedMealDao: SavedMealDao,
    private val mealLogRepository: MealLogRepository,
    private val database: KcalGrindDatabase? = null
) : SavedMealRepository {

    override suspend fun saveMealAsTemplate(
        name: String,
        mealType: MealType,
        items: List<FoodLogItem>
    ): Long {
        val savedMealEntity = SavedMealEntity(
            name = name.trim().ifEmpty { "${mealType.name.lowercase().replaceFirstChar { it.uppercase() }} Combo" },
            defaultMealType = mealType.name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val executeSave: suspend () -> Long = {
            val savedMealId = savedMealDao.insertSavedMeal(savedMealEntity)
            val savedMealItemEntities = items.map { item ->
                SavedMealItemEntity(
                    savedMealId = savedMealId,
                    foodId = item.foodId,
                    name = item.name,
                    brand = item.brand,
                    servingDescription = item.servingDescription,
                    servingGrams = item.servingGrams,
                    calories = item.calories,
                    proteinG = item.proteinG,
                    carbsG = item.carbsG,
                    fatG = item.fatG,
                    fiberG = item.fiberG,
                    sugarG = item.sugarG,
                    sodiumMg = item.sodiumMg,
                    source = item.source.id
                )
            }
            if (savedMealItemEntities.isNotEmpty()) {
                savedMealDao.insertSavedMealItems(savedMealItemEntities)
            }
            savedMealId
        }

        return if (database != null) {
            database.withTransaction { executeSave() }
        } else {
            executeSave()
        }
    }

    override suspend fun renameSavedMeal(id: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotEmpty()) {
            savedMealDao.renameSavedMeal(id, trimmed, System.currentTimeMillis())
        }
    }

    override suspend fun deleteSavedMeal(id: Long) {
        savedMealDao.deleteSavedMealById(id)
    }

    override fun observeSavedMeals(): Flow<List<SavedMealWithItems>> {
        return savedMealDao.observeSavedMealsWithItems().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getSavedMealById(id: Long): SavedMealWithItems? {
        return savedMealDao.getSavedMealWithItemsById(id)?.toDomain()
    }

    override suspend fun logSavedMeal(
        savedMealId: Long,
        targetDate: String,
        targetMealType: MealType
    ): Long {
        val savedMealWithItems = getSavedMealById(savedMealId)
            ?: throw IllegalArgumentException("Saved meal with id $savedMealId not found")

        val newMeal = MealLog(
            id = 0L,
            date = targetDate,
            mealType = targetMealType,
            totalCalories = savedMealWithItems.totalCalories,
            loggedAt = System.currentTimeMillis(),
            source = LogSource.MANUAL,
            synced = false
        )

        val newItems = savedMealWithItems.items.map { item ->
            FoodLogItem(
                mealLogId = 0L,
                foodId = item.foodId,
                name = item.name,
                brand = item.brand,
                servingDescription = item.servingDescription,
                servingGrams = item.servingGrams,
                calories = item.calories,
                proteinG = item.proteinG,
                carbsG = item.carbsG,
                fatG = item.fatG,
                fiberG = item.fiberG,
                sugarG = item.sugarG,
                sodiumMg = item.sodiumMg,
                source = item.source,
                confirmed = true
            )
        }

        return mealLogRepository.saveMeal(newMeal, newItems)
    }
}
