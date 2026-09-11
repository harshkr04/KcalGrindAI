package com.lumina.nutrition.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.lumina.nutrition.domain.model.AuthState
import com.lumina.nutrition.domain.model.AuthUser
import com.lumina.nutrition.domain.repository.AuthRepository
import com.lumina.nutrition.domain.repository.UserProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val userProfileRepository: UserProfileRepository
) : AuthRepository {

    override fun observeAuthState(): Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                trySend(AuthState.Unauthenticated)
            } else if (user.isAnonymous) {
                trySend(AuthState.Guest(user.toDomain()))
            } else {
                trySend(AuthState.Authenticated(user.toDomain()))
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun getCurrentUser(): AuthUser? {
        return auth.currentUser?.toDomain()
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user?.toDomain() ?: throw IllegalStateException("Firebase returned null user")
            userProfileRepository.updateAuthDetails(user.uid, user.email)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<AuthUser> {
        return try {
            val currentUser = auth.currentUser
            val resultUser: FirebaseUser = if (currentUser != null && currentUser.isAnonymous) {
                val credential = EmailAuthProvider.getCredential(email.trim(), password)
                val linkResult = currentUser.linkWithCredential(credential).await()
                linkResult.user ?: currentUser
            } else {
                val createResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
                createResult.user ?: throw IllegalStateException("Firebase returned null user")
            }
            val user = resultUser.toDomain()
            userProfileRepository.updateAuthDetails(user.uid, user.email)
            com.lumina.nutrition.core.util.LuminaCrashReporter.setUserId(user.uid)
            Result.success(user)
        } catch (e: Exception) {
            com.lumina.nutrition.core.util.LuminaCrashReporter.recordException(e, mapOf("action" to "signUpWithEmail"))
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val currentUser = auth.currentUser
            val resultUser: FirebaseUser = if (currentUser != null && currentUser.isAnonymous) {
                val linkResult = currentUser.linkWithCredential(credential).await()
                linkResult.user ?: currentUser
            } else {
                val signInResult = auth.signInWithCredential(credential).await()
                signInResult.user ?: throw IllegalStateException("Firebase returned null user")
            }
            val user = resultUser.toDomain()
            userProfileRepository.updateAuthDetails(user.uid, user.email)
            com.lumina.nutrition.core.util.LuminaCrashReporter.setUserId(user.uid)
            Result.success(user)
        } catch (e: Exception) {
            com.lumina.nutrition.core.util.LuminaCrashReporter.recordException(e, mapOf("action" to "signInWithGoogle"))
            Result.failure(e)
        }
    }

    override suspend fun signInAsGuest(): Result<AuthUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user?.toDomain() ?: throw IllegalStateException("Firebase returned null user")
            userProfileRepository.updateAuthDetails(user.uid, null)
            com.lumina.nutrition.core.util.LuminaCrashReporter.setUserId(user.uid)
            Result.success(user)
        } catch (e: Exception) {
            com.lumina.nutrition.core.util.LuminaCrashReporter.recordException(e, mapOf("action" to "signInAsGuest"))
            Result.failure(e)
        }
    }

    override suspend fun linkWithEmail(email: String, password: String): Result<AuthUser> {
        return signUpWithEmail(email, password)
    }

    override suspend fun linkWithGoogle(idToken: String): Result<AuthUser> {
        return signInWithGoogle(idToken)
    }

    override suspend fun signOut() {
        auth.signOut()
        com.lumina.nutrition.core.util.LuminaCrashReporter.setUserId(null)
    }

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        return try {
            var user = auth.currentUser
            if (user == null) {
                val guestRes = signInAsGuest()
                user = if (guestRes.isSuccess) auth.currentUser else null
            }
            user?.getIdToken(forceRefresh)?.await()?.token
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to get Firebase ID token", e)
            null
        }
    }

    private fun FirebaseUser.toDomain(): AuthUser {
        return AuthUser(
            uid = uid,
            email = email,
            isAnonymous = isAnonymous,
            displayName = displayName
        )
    }
}
