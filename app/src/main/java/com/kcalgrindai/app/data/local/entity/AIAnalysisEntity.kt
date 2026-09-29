package com.kcalgrindai.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_analyses",
    indices = [
        Index(value = ["createdAt"])
    ]
)
data class AIAnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val inputType: String,
    val rawInputRef: String? = null,
    val resultJson: String,
    val overallConfidence: Float? = null,
    val createdAt: Long
)
