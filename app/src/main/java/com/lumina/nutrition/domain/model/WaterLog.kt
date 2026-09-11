package com.lumina.nutrition.domain.model

data class WaterLog(
    val id: Long = 0L,
    val date: String,
    val amountMl: Int,
    val loggedAt: Long = System.currentTimeMillis()
)
