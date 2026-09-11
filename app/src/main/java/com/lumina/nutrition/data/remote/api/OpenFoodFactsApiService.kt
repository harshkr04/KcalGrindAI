package com.lumina.nutrition.data.remote.api

import com.lumina.nutrition.data.remote.dto.OffProductResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApiService {

    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): OffProductResponse
}
