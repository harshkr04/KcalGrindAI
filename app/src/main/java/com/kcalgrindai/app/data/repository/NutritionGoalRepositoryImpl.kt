package com.kcalgrindai.app.data.repository

import com.kcalgrindai.app.data.local.dao.NutritionGoalDao
import com.kcalgrindai.app.data.mapper.toDomain
import com.kcalgrindai.app.data.mapper.toEntity
import com.kcalgrindai.app.domain.model.NutritionGoal
import com.kcalgrindai.app.domain.repository.NutritionGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NutritionGoalRepositoryImpl @Inject constructor(
    private val nutritionGoalDao: NutritionGoalDao
) : NutritionGoalRepository {

    override suspend fun saveGoal(goal: NutritionGoal): Long {
        return nutritionGoalDao.upsertGoal(goal.toEntity())
    }

    override suspend fun getGoalByUserId(userId: Long): NutritionGoal? {
        return nutritionGoalDao.getGoalByUserId(userId)?.toDomain()
    }

    override suspend fun getLatestGoal(): NutritionGoal? {
        return nutritionGoalDao.getLatestGoal()?.toDomain()
    }

    override fun observeGoalByUserId(userId: Long): Flow<NutritionGoal?> {
        return nutritionGoalDao.observeGoalByUserId(userId).map { it?.toDomain() }
    }

    override fun observeLatestGoal(): Flow<NutritionGoal?> {
        return nutritionGoalDao.observeLatestGoal().map { it?.toDomain() }
    }

    override suspend fun deleteGoalByUserId(userId: Long) {
        nutritionGoalDao.deleteGoalByUserId(userId)
    }
}
