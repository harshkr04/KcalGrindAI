package com.lumina.nutrition.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OffProductResponse(
    @SerialName("status") val status: Int = 0,
    @SerialName("status_verbose") val statusVerbose: String? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("product") val product: OffProduct? = null
)

@Serializable
data class OffProduct(
    @SerialName("product_name") val productName: String? = null,
    @SerialName("generic_name") val genericName: String? = null,
    @SerialName("brands") val brands: String? = null,
    @SerialName("serving_size") val servingSize: String? = null,
    @SerialName("serving_quantity") val servingQuantity: Double? = null,
    @SerialName("nutriments") val nutriments: OffNutriments? = null
)

@Serializable
data class OffNutriments(
    @SerialName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerialName("energy-kcal") val energyKcal: Double? = null,
    @SerialName("energy_100g") val energy100g: Double? = null,
    @SerialName("proteins_100g") val proteins100g: Double? = null,
    @SerialName("carbohydrates_100g") val carbohydrates100g: Double? = null,
    @SerialName("fat_100g") val fat100g: Double? = null,
    @SerialName("fiber_100g") val fiber100g: Double? = null
)
