package com.kcalgrindai.app.domain.repository

import com.kcalgrindai.app.domain.model.AuthState
import com.kcalgrindai.app.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>
    suspend fun getCurrentUser(): AuthUser?
    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun signUpWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>
    suspend fun signInAsGuest(): Result<AuthUser>
    suspend fun linkWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun linkWithGoogle(idToken: String): Result<AuthUser>
    suspend fun signOut()
    suspend fun getIdToken(forceRefresh: Boolean = false): String?
    suspend fun checkSessionFreshness(): Result<Unit>
    suspend fun deleteFirebaseUser(): Result<Unit>
    suspend fun reauthenticateWithPassword(password: String): Result<Unit>
}
