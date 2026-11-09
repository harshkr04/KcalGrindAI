package com.kcalgrindai.app.domain.usecase

import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.kcalgrindai.app.core.util.NetworkMonitor
import com.kcalgrindai.app.data.local.KcalGrindDatabase
import com.kcalgrindai.app.data.remote.api.AccountApiService
import com.kcalgrindai.app.domain.repository.AuthRepository
import com.kcalgrindai.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

sealed interface AccountDeletionResult {
    data object Success : AccountDeletionResult
    data object RequiresRecentLogin : AccountDeletionResult
    data class NetworkError(val message: String) : AccountDeletionResult
    data class BackendError(val message: String) : AccountDeletionResult
    data class Failure(val message: String) : AccountDeletionResult
}

open class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val accountApiService: AccountApiService,
    private val database: KcalGrindDatabase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val networkMonitor: NetworkMonitor? = null,
    private val foodImageStorageManager: com.kcalgrindai.app.core.util.FoodImageStorageManager? = null
) {

    open suspend fun execute(): AccountDeletionResult = withContext(Dispatchers.IO) {
        val currentUser = authRepository.getCurrentUser()

        // Pure offline guest with no Firebase user at all
        if (currentUser == null) {
            wipeLocalState()
            return@withContext AccountDeletionResult.Success
        }

        // Check network if monitor available
        if (networkMonitor != null && !networkMonitor.isCurrentlyOnline()) {
            return@withContext AccountDeletionResult.NetworkError(
                "Network unavailable. Please connect to the internet to delete your account."
            )
        }

        // 1. Fresh-session check BEFORE any destructive remote call
        val freshnessResult = authRepository.checkSessionFreshness()
        if (freshnessResult.isFailure) {
            val ex = freshnessResult.exceptionOrNull()
            if (ex is FirebaseAuthRecentLoginRequiredException ||
                ex?.message?.contains("recent-login", ignoreCase = true) == true
            ) {
                return@withContext AccountDeletionResult.RequiresRecentLogin
            }
            if (ex is UnknownHostException || ex is ConnectException || ex is SocketTimeoutException) {
                return@withContext AccountDeletionResult.NetworkError(
                    "Network error while verifying session. Please reconnect."
                )
            }
            return@withContext AccountDeletionResult.Failure(
                ex?.message ?: "Session verification failed."
            )
        }

        // 2. Call backend authenticated purge route (DELETE /account)
        try {
            accountApiService.deleteAccount()
        } catch (e: Exception) {
            when (e) {
                is UnknownHostException, is ConnectException, is SocketTimeoutException -> {
                    return@withContext AccountDeletionResult.NetworkError(
                        "Cloud server unreachable. Please check your internet connection."
                    )
                }
                is retrofit2.HttpException -> {
                    when (e.code()) {
                        401 -> {
                            return@withContext AccountDeletionResult.RequiresRecentLogin
                        }
                        403 -> {
                            return@withContext AccountDeletionResult.Failure(
                                "Account deletion forbidden (403). Your session is not authorized to delete this account."
                            )
                        }
                        in 500..599 -> {
                            return@withContext AccountDeletionResult.BackendError(
                                "Cloud server error (${e.code()}). Could not purge account data. Please try again."
                            )
                        }
                        else -> {
                            return@withContext AccountDeletionResult.Failure(
                                "Server rejected deletion request (${e.code()}): ${e.message()}"
                            )
                        }
                    }
                }
                is IOException -> {
                    return@withContext AccountDeletionResult.NetworkError(
                        "Network error: ${e.message ?: "Could not reach cloud server"}"
                    )
                }
                else -> {
                    return@withContext AccountDeletionResult.BackendError(
                        "Failed to communicate with cloud server: ${e.message}"
                    )
                }
            }
        }

        // 3. Delete Firebase Auth user
        val deleteAuthResult = authRepository.deleteFirebaseUser()
        if (deleteAuthResult.isFailure) {
            val ex = deleteAuthResult.exceptionOrNull()
            if (ex is FirebaseAuthRecentLoginRequiredException ||
                ex?.message?.contains("recent-login", ignoreCase = true) == true
            ) {
                return@withContext AccountDeletionResult.RequiresRecentLogin
            }
            if (ex is UnknownHostException || ex is ConnectException || ex is SocketTimeoutException) {
                return@withContext AccountDeletionResult.NetworkError(
                    "Network error while removing authentication. Please reconnect."
                )
            }
            return@withContext AccountDeletionResult.Failure(
                ex?.message ?: "Failed to remove user authentication."
            )
        }

        // 4. Wipe local Room SQLite database and local preferences
        wipeLocalState()

        AccountDeletionResult.Success
    }

    private suspend fun wipeLocalState() {
        database.clearAllTables()
        userPreferencesRepository.clearAllPreferences()
        foodImageStorageManager?.deleteAllFoodImages()
    }
}
