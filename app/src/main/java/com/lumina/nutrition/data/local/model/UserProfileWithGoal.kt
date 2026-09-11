package com.lumina.nutrition.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.lumina.nutrition.data.local.entity.NutritionGoalEntity
import com.lumina.nutrition.data.local.entity.UserProfileEntity

data class UserProfileWithGoal(
    @Embedded
    val profile: UserProfileEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val goal: NutritionGoalEntity?
)
