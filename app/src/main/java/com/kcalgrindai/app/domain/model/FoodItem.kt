package com.kcalgrindai.app.domain.model

data class FoodItem(
    val id: Long = 0L,
    val source: FoodSource,
    val externalId: String? = null,
    val name: String,
    val brand: String? = null,
    val servingDescription: String,
    val servingGrams: Double,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fiberG: Double,
    val barcodeUpc: String? = null,
    val isUserCreated: Boolean,
    val createdAt: Long
)
