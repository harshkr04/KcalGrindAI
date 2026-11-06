package com.kcalgrindai.app.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.kcalgrindai.app.data.local.entity.SavedMealEntity
import com.kcalgrindai.app.data.local.entity.SavedMealItemEntity

data class SavedMealWithItemsRelation(
    @Embedded val savedMeal: SavedMealEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "savedMealId"
    )
    val items: List<SavedMealItemEntity>
)
