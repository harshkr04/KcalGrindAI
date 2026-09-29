package com.kcalgrindai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.kcalgrindai.app.data.local.entity.UserProfileEntity
import com.kcalgrindai.app.data.local.model.UserProfileWithGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert
    suspend fun insertProfile(profile: UserProfileEntity): Long

    @Upsert
    suspend fun upsertProfile(profile: UserProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfile(): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE firebaseUid = :firebaseUid LIMIT 1")
    suspend fun getProfileByFirebaseUid(firebaseUid: String): UserProfileEntity?

    @Query("UPDATE user_profile SET firebaseUid = :firebaseUid, email = :email, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateAuthDetails(id: Long, firebaseUid: String?, email: String?, updatedAt: Long)

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Transaction
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun observeProfileWithGoal(): Flow<UserProfileWithGoal?>

    @Query("DELETE FROM user_profile")
    suspend fun deleteAllProfiles()

    @Delete
    suspend fun deleteProfile(profile: UserProfileEntity)
}
