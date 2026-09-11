package com.lumina.nutrition.data.repository

import androidx.room.withTransaction
import com.lumina.nutrition.data.local.LuminaDatabase
import com.lumina.nutrition.data.local.dao.NutritionGoalDao
import com.lumina.nutrition.data.local.dao.UserProfileDao
import com.lumina.nutrition.data.mapper.toDomain
import com.lumina.nutrition.data.mapper.toEntity
import com.lumina.nutrition.domain.model.NutritionGoal
import com.lumina.nutrition.domain.model.UserProfile
import com.lumina.nutrition.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val database: LuminaDatabase,
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
