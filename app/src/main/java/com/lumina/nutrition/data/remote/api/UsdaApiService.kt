package com.lumina.nutrition.data.remote.api

import com.lumina.nutrition.data.remote.dto.UsdaSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UsdaApiService {

    @GET("fdc/v1/foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("pageSize") pageSize: Int = 25,
        @Query("api_key") apiKey: String
    ): UsdaSearchResponse
}
