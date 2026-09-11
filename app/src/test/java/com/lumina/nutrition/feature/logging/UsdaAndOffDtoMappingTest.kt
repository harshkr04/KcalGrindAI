package com.lumina.nutrition.feature.logging

import com.lumina.nutrition.data.mapper.toDomain
import com.lumina.nutrition.data.remote.dto.OffNutriments
import com.lumina.nutrition.data.remote.dto.OffProduct
import com.lumina.nutrition.data.remote.dto.OffProductResponse
import com.lumina.nutrition.data.remote.dto.UsdaFoodItem
import com.lumina.nutrition.data.remote.dto.UsdaNutrient
import com.lumina.nutrition.domain.model.FoodSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UsdaAndOffDtoMappingTest {

    @Test
    fun testUsdaFoodItemToDomainMapping() {
        val usdaItem = UsdaFoodItem(
            fdcId = 173944L,
            description = "Bananas, raw",
            brandOwner = null,
            brandName = null,
            servingSize = 118.0,
            servingSizeUnit = "g",
            householdServingFullText = "1 medium banana (118g)",
            foodNutrients = listOf(
                UsdaNutrient(nutrientId = 1008, nutrientName = "Energy", value = 105.0),
                UsdaNutrient(nutrientId = 1003, nutrientName = "Protein", value = 1.29),
                UsdaNutrient(nutrientId = 1004, nutrientName = "Total lipid", value = 0.39),
                UsdaNutrient(nutrientId = 1005, nutrientName = "Carbohydrate", value = 26.95),
                UsdaNutrient(nutrientId = 1079, nutrientName = "Fiber", value = 3.07)
            )
        )

        val domain = usdaItem.toDomain()

        assertEquals("Bananas, raw", domain.name)
        assertEquals(FoodSource.USDA, domain.source)
        assertEquals("173944", domain.externalId)
        assertEquals(105.0, domain.calories, 0.0)
        assertEquals(1.3, domain.proteinG, 0.0)
        assertEquals(0.4, domain.fatG, 0.0)
        assertEquals(27.0, domain.carbsG, 0.0)
        assertEquals(3.1, domain.fiberG, 0.0)
        assertEquals("1 medium banana (118g)", domain.servingDescription)
    }

    @Test
    fun testOffProductResponseToDomainMapping() {
        val offResponse = OffProductResponse(
            status = 1,
            code = "3017620422003",
            product = OffProduct(
                productName = "Nutella",
                brands = "Ferrero",
                servingSize = "15g",
                servingQuantity = 15.0,
                nutriments = OffNutriments(
                    energyKcal100g = 539.0,
                    proteins100g = 6.3,
                    carbohydrates100g = 57.5,
                    fat100g = 30.9,
                    fiber100g = 0.0
                )
            )
        )

        val domain = offResponse.toDomain("3017620422003")

        assertNotNull(domain)
        assertEquals("Nutella", domain!!.name)
        assertEquals("Ferrero", domain.brand)
        assertEquals(FoodSource.OPEN_FOOD_FACTS, domain.source)
        assertEquals("3017620422003", domain.barcodeUpc)
        assertEquals(539.0, domain.calories, 0.0)
        assertEquals(6.3, domain.proteinG, 0.0)
        assertEquals(57.5, domain.carbsG, 0.0)
        assertEquals(30.9, domain.fatG, 0.0)
        assertEquals(15.0, domain.servingGrams, 0.0)
    }
}
