package com.kcalgrindai.app.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.kcalgrindai.app.data.local.entity.FoodLogItemEntity
import com.kcalgrindai.app.data.local.entity.MealLogEntity

data class MealWithItems(
    @Embedded
    val meal: MealLogEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "mealLogId"
    )
    val items: List<FoodLogItemEntity>
)
