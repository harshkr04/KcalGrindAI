package com.kcalgrindai.app.data.repository

import androidx.room.withTransaction
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.local.dao.NutritionGoalDao
import com.kcalgrindai.app.data.local.dao.UserProfileDao
import com.kcalgrindai.app.data.mapper.toDomain
import com.kcalgrindai.app.data.mapper.toEntity
import com.kcalgrindai.app.domain.model.NutritionGoal
import com.kcalgrindai.app.domain.model.UserProfile
import com.kcalgrindai.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val database: KcalGrindDatabase,
    private val userProfileDao: UserProfileDao,
    private val nutritionGoalDao: NutritionGoalDao
) : UserProfileRepository {

    override suspend fun saveProfile(profile: UserProfile): Long {
        val entity = profile.toEntity()
        return if (entity.id == 0L) {
            userProfileDao.insertProfile(entity)
        } else {
            val rowId = userProfileDao.upsertProfile(entity)
            if (rowId > 0) rowId else entity.id
        }
    }

    override suspend fun saveProfileAndGoal(profile: UserProfile, goal: NutritionGoal): Long {
        return database.withTransaction {
            val profileEntity = profile.toEntity().copy(id = if (profile.id > 0) profile.id else 0L)
            val generatedUserId = if (profileEntity.id == 0L) {
                userProfileDao.insertProfile(profileEntity)
            } else {
                val rowId = userProfileDao.upsertProfile(profileEntity)
                if (rowId > 0) rowId else profileEntity.id
            }

            val goalEntity = goal.toEntity().copy(
                id = 0L,
                userId = generatedUserId
            )
            nutritionGoalDao.insertGoal(goalEntity)
            generatedUserId
        }
    }

    override suspend fun getProfile(): UserProfile? {
        return userProfileDao.getProfile()?.toDomain()
    }

    override suspend fun getProfileByFirebaseUid(firebaseUid: String): UserProfile? {
        return userProfileDao.getProfileByFirebaseUid(firebaseUid)?.toDomain()
    }

    override suspend fun updateAuthDetails(firebaseUid: String?, email: String?) {
        val existing = userProfileDao.getProfile() ?: return
        userProfileDao.updateAuthDetails(
            id = existing.id,
            firebaseUid = firebaseUid,
            email = email,
            updatedAt = System.currentTimeMillis()
        )
    }

    override fun observeProfile(): Flow<UserProfile?> {
        return userProfileDao.observeProfile().map { it?.toDomain() }
    }

    override fun observeProfileWithGoal(): Flow<Pair<UserProfile, NutritionGoal?>?> {
        return userProfileDao.observeProfileWithGoal().map { relation ->
            relation?.let {
                Pair(it.profile.toDomain(), it.goal?.toDomain())
            }
        }
    }

    override suspend fun deleteProfile() {
        userProfileDao.deleteAllProfiles()
    }
}
