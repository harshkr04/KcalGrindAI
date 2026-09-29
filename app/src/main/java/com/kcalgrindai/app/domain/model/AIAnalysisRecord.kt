package com.kcalgrindai.app.domain.model

data class AIAnalysisRecord(
    val id: Long = 0L,
    val inputType: String,
    val rawInputRef: String? = null,
    val resultJson: String,
    val overallConfidence: Float? = null,
    val createdAt: Long
)
