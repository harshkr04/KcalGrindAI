package com.kcalgrindai.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "foods",
    indices = [
        Index(value = ["barcodeUpc"]),
        Index(value = ["name"]),
        Index(value = ["source", "externalId"])
    ]
)
data class FoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val source: String,
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
