package com.kcalgrindai.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "water_logs",
    indices = [
        Index(value = ["date"])
    ]
)
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: String, // YYYY-MM-DD
    val amountMl: Int,
    val loggedAt: Long = System.currentTimeMillis()
)
