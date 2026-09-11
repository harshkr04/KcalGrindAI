package com.lumina.nutrition.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsdaSearchResponse(
    @SerialName("totalHits") val totalHits: Int = 0,
    @SerialName("currentPage") val currentPage: Int = 1,
    @SerialName("totalPages") val totalPages: Int = 1,
    @SerialName("foods") val foods: List<UsdaFoodItem> = emptyList()
)

@Serializable
data class UsdaFoodItem(
    @SerialName("fdcId") val fdcId: Long,
    @SerialName("description") val description: String,
    @SerialName("dataType") val dataType: String? = null,
    @SerialName("brandOwner") val brandOwner: String? = null,
    @SerialName("brandName") val brandName: String? = null,
    @SerialName("servingSize") val servingSize: Double? = null,
    @SerialName("servingSizeUnit") val servingSizeUnit: String? = null,
    @SerialName("householdServingFullText") val householdServingFullText: String? = null,
    @SerialName("foodNutrients") val foodNutrients: List<UsdaNutrient> = emptyList()
)

@Serializable
data class UsdaNutrient(
    @SerialName("nutrientId") val nutrientId: Int? = null,
    @SerialName("nutrientName") val nutrientName: String? = null,
    @SerialName("nutrientNumber") val nutrientNumber: String? = null,
    @SerialName("unitName") val unitName: String? = null,
    @SerialName("value") val value: Double = 0.0
)
