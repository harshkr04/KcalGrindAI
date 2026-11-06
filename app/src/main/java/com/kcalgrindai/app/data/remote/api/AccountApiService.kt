package com.kcalgrindai.app.data.remote.api

import kotlinx.serialization.Serializable
import retrofit2.http.DELETE

@Serializable
data class AccountDeletionResponse(
    val purged: Boolean = true,
    val uid: String? = null,
    val timestamp: String? = null
)

interface AccountApiService {

    @DELETE("account")
    suspend fun deleteAccount(): AccountDeletionResponse
}
