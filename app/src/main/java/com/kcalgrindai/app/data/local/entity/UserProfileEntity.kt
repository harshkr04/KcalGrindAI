package com.kcalgrindai.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val firebaseUid: String? = null,
    val email: String? = null,
    val firstName: String? = null,
    val goal: String,
    val units: String,
    val age: Int,
    val heightCm: Double,
    val weightKg: Double,
    val goalWeightKg: Double,
    val activityLevel: String,
    val dietTags: List<String>,
    val allergies: List<String>,
    val targetsSource: String,
    val createdAt: Long,
    val updatedAt: Long
)
