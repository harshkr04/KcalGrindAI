package com.kcalgrindai.app.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.kcalgrindai.app.domain.model.AuthState
import com.kcalgrindai.app.domain.model.AuthUser
import com.kcalgrindai.app.domain.repository.AuthRepository
import com.kcalgrindai.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val userProfileRepository: UserProfileRepository,
    private val widgetUpdater: dagger.Lazy<com.kcalgrindai.app.core.util.WidgetUpdater>? = null
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
            Result.success(user)
        } catch (e: Exception) {
            com.kcalgrindai.app.core.util.KcalGrindAICrashReporter.recordException(e, mapOf("action" to "signUpWithEmail"))
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
            Result.success(user)
        } catch (e: Exception) {
            com.kcalgrindai.app.core.util.KcalGrindAICrashReporter.recordException(e, mapOf("action" to "signInWithGoogle"))
            Result.failure(e)
        }
    }

    override suspend fun signInAsGuest(): Result<AuthUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user?.toDomain() ?: throw IllegalStateException("Firebase returned null user")
            userProfileRepository.updateAuthDetails(user.uid, null)
            Result.success(user)
        } catch (e: Exception) {
            com.kcalgrindai.app.core.util.KcalGrindAICrashReporter.recordException(e, mapOf("action" to "signInAsGuest"))
            val localProfile = userProfileRepository.getProfile()
            if (localProfile != null) {
                Result.success(
                    AuthUser(
                        uid = localProfile.firebaseUid ?: "guest_local",
                        email = null,
                        isAnonymous = true,
                        displayName = localProfile.firstName ?: "Guest"
                    )
                )
            } else {
                Result.failure(e)
            }
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
        widgetUpdater?.get()?.clearWidgetDataOnSignOut()
    }

    override suspend fun checkSessionFreshness(): Result<Unit> {
        val user = auth.currentUser ?: return Result.success(Unit)
        if (user.isAnonymous) return Result.success(Unit)
        return try {
            val tokenResult = user.getIdToken(false).await()
            val authTimeSeconds = (tokenResult.claims["auth_time"] as? Number)?.toLong()
                ?: tokenResult.authTimestamp
            val nowSeconds = System.currentTimeMillis() / 1000
            if (authTimeSeconds > 0 && (nowSeconds - authTimeSeconds) > 300) {
                Result.failure(FirebaseAuthRecentLoginRequiredException("auth/requires-recent-login", "Recent login required"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFirebaseUser(): Result<Unit> {
        val user = auth.currentUser ?: return Result.success(Unit)
        return try {
            user.delete().await()
                widgetUpdater?.get()?.clearWidgetDataOnSignOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reauthenticateWithPassword(password: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("No authenticated user"))
        val email = user.email ?: return Result.failure(IllegalStateException("No email on account"))
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        return try {
            var user = auth.currentUser
            if (user == null) {
                val guestRes = signInAsGuest()
                user = if (guestRes.isSuccess) auth.currentUser else null
            }
            try {
                user?.getIdToken(forceRefresh)?.await()?.token
            } catch (e: Exception) {
                auth.signOut()
                val guestRes = signInAsGuest()
                val freshUser = if (guestRes.isSuccess) auth.currentUser else null
                freshUser?.getIdToken(true)?.await()?.token
            }
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
