package com.lumina.nutrition.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.lumina.nutrition.data.local.entity.FoodLogItemEntity
import com.lumina.nutrition.data.local.entity.MealLogEntity

data class MealWithItems(
    @Embedded
    val meal: MealLogEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "mealLogId"
    )
    val items: List<FoodLogItemEntity>
)
