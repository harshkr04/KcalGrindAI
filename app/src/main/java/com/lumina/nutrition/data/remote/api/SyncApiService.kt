package com.lumina.nutrition.data.remote.api

import com.lumina.nutrition.data.remote.dto.SyncPullResponse
import com.lumina.nutrition.data.remote.dto.SyncPushRequest
import com.lumina.nutrition.data.remote.dto.SyncPushResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SyncApiService {

    @POST("sync/push")
    suspend fun pushSync(
        @Body request: SyncPushRequest
    ): SyncPushResponse

    @GET("sync/pull")
    suspend fun pullSync(
        @Query("since") since: String
    ): SyncPullResponse
}
