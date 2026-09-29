package com.kcalgrindai.app.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.kcalgrindai.app.data.local.entity.NutritionGoalEntity
import com.kcalgrindai.app.data.local.entity.UserProfileEntity

data class UserProfileWithGoal(
    @Embedded
    val profile: UserProfileEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val goal: NutritionGoalEntity?
)
