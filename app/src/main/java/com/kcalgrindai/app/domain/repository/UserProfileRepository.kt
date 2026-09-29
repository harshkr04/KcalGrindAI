package com.kcalgrindai.app.domain.repository

import com.kcalgrindai.app.domain.model.NutritionGoal
import com.kcalgrindai.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    suspend fun saveProfile(profile: UserProfile): Long
    suspend fun saveProfileAndGoal(profile: UserProfile, goal: NutritionGoal): Long
    suspend fun getProfile(): UserProfile?
    suspend fun getProfileByFirebaseUid(firebaseUid: String): UserProfile?
    suspend fun updateAuthDetails(firebaseUid: String?, email: String?)
    fun observeProfile(): Flow<UserProfile?>
    fun observeProfileWithGoal(): Flow<Pair<UserProfile, NutritionGoal?>?>
    suspend fun deleteProfile()
}
