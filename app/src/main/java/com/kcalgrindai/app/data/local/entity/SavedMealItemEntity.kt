package com.kcalgrindai.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_meal_items",
    foreignKeys = [
        ForeignKey(
            entity = SavedMealEntity::class,
            parentColumns = ["id"],
            childColumns = ["savedMealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["savedMealId"])]
)
data class SavedMealItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val savedMealId: Long,
    val foodId: Long? = null,
    val name: String,
    val brand: String? = null,
    val servingDescription: String,
    val servingGrams: Double,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fiberG: Double = 0.0,
    val sugarG: Double = 0.0,
    val sodiumMg: Double = 0.0,
    val source: String = "USER_ENTERED"
)
