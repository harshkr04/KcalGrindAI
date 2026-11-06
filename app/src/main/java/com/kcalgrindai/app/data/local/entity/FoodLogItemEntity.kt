package com.kcalgrindai.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_log_items",
    foreignKeys = [
        ForeignKey(
            entity = MealLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealLogId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["mealLogId"]),
        Index(value = ["foodId"])
    ]
)
data class FoodLogItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val mealLogId: Long,
    val foodId: Long? = null,
    val name: String,
    val brand: String? = null,
    val servingDescription: String,
    val servingGrams: Double,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fiberG: Double,
    @ColumnInfo(defaultValue = "0.0")
    val sugarG: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0")
    val sodiumMg: Double = 0.0,
    val source: String,
    val confidence: Float? = null,
    val confirmed: Boolean
)
