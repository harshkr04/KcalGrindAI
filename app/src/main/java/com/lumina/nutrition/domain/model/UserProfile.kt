package com.lumina.nutrition.domain.model

data class UserProfile(
    val id: Long = 0L,
    val firebaseUid: String? = null,
    val email: String? = null,
    val goal: GoalType,
    val units: UnitSystem,
    val age: Int,
    val heightCm: Double,
    val weightKg: Double,
    val goalWeightKg: Double,
    val activityLevel: ActivityLevel,
    val dietTags: List<String>,
    val allergies: List<String>,
    val targetsSource: TargetBasis,
    val createdAt: Long,
    val updatedAt: Long
)
