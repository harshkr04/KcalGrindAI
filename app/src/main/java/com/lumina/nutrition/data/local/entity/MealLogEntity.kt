package com.lumina.nutrition.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_logs",
    indices = [
        Index(value = ["date"]),
        Index(value = ["loggedAt"])
    ]
)
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: String,
    val mealType: String,
    val totalCalories: Double,
    val loggedAt: Long,
    val source: String,
    val synced: Boolean
)
