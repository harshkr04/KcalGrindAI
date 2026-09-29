package com.kcalgrindai.app.data.remote.api

import com.kcalgrindai.app.data.remote.dto.SyncPullResponse
import com.kcalgrindai.app.data.remote.dto.SyncPushRequest
import com.kcalgrindai.app.data.remote.dto.SyncPushResponse
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
