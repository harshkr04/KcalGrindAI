package com.lumina.nutrition.domain.model

data class WeightEntry(
    val id: Long = 0L,
    val weightKg: Double,
    val date: String,
    val note: String? = null,
    val loggedAt: Long
)
