package com.kcalgrindai.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_entries",
    indices = [
        Index(value = ["date"]),
        Index(value = ["loggedAt"])
    ]
)
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val weightKg: Double,
    val date: String,
    val note: String? = null,
    val loggedAt: Long
)
