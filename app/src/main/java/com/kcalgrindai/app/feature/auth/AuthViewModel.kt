package com.kcalgrindai.app.feature.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.domain.repository.AuthRepository
import com.kcalgrindai.app.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailError = null,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = null,
                errorMessage = null
            )
        }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                confirmPassword = value,
                confirmPasswordError = null,
                errorMessage = null
            )
        }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isSignUpMode = !it.isSignUpMode,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                errorMessage = null
            )
        }
    }

    fun toggleEmailExpanded() {
        _uiState.update { it.copy(isEmailExpanded = !it.isEmailExpanded) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        val email = state.email.trim()
        val password = state.password

        var hasError = false
        var emailError: String? = null
        var passwordError: String? = null
        var confirmPasswordError: String? = null

        if (email.isBlank()) {
            emailError = "Email is required"
            hasError = true
        } else if (!EMAIL_REGEX.matches(email)) {
            emailError = "Please enter a valid email address"
            hasError = true
        }

        if (password.isBlank()) {
            passwordError = "Password is required"
            hasError = true
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            hasError = true
        }

        if (state.isSignUpMode) {
            if (state.confirmPassword != password) {
                confirmPasswordError = "Passwords do not match"
                hasError = true
            }
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = if (state.isSignUpMode) {
                authRepository.signUpWithEmail(email, password)
            } else {
                authRepository.signInWithEmail(email, password)
            }

            result.fold(
                onSuccess = {
                    if (!state.isSignUpMode) {
                        runCatching { syncRepository?.restoreCloudBackup() }
                    }
                    _uiState.update { it.copy(isLoading = false, authSuccess = true) }
                },
                onFailure = { error ->
                    val flow = if (state.isSignUpMode) "email_signup" else "email_signin"
                    com.kcalgrindai.app.core.util.KcalGrindAICrashReporter.recordException(
                        error,
                        mapOf(
                            "auth_flow" to flow,
                            "error_type" to (error::class.simpleName ?: "UnknownException"),
                            "error_detail" to (error.message?.take(200) ?: "unknown")
                        )
                    )
                    val userMessage = resolveEmailErrorMessage(error, state.isSignUpMode)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = userMessage
                        )
                    }
                }
            )
        }
    }

    fun onGoogleSignInStarted() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
    }

    fun onGoogleSignInCancelled() {
        _uiState.update { it.copy(isLoading = false) }
    }

    fun onGoogleSignInFailure(message: String) {
        com.kcalgrindai.app.core.util.KcalGrindAICrashReporter.recordException(
            RuntimeException("Google Sign-In Failure: $message"),
            mapOf("auth_flow" to "google_client", "error_detail" to message.take(200))
        )
        val userMessage = resolveGoogleFailureMessage(message)
        _uiState.update { it.copy(isLoading = false, errorMessage = userMessage) }
    }

    fun onGoogleSignInSuccess(idToken: String) {
        if (idToken.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Google authentication failed: Empty token.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken)
            result.fold(
                onSuccess = {
                    runCatching { syncRepository?.restoreCloudBackup() }
                    _uiState.update { it.copy(isLoading = false, authSuccess = true) }
                },
                onFailure = { error ->
                    com.kcalgrindai.app.core.util.KcalGrindAICrashReporter.recordException(
                        error,
                        mapOf(
                            "auth_flow" to "google_firebase_auth",
                            "error_type" to (error::class.simpleName ?: "UnknownException"),
                            "error_detail" to (error.message?.take(200) ?: "unknown")
                        )
                    )
                    val userMessage = resolveGoogleErrorMessage(error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = userMessage
                        )
                    }
                }
            )
        }
    }

    fun continueAsGuest() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.signInAsGuest()
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, authSuccess = true) }
                },
                onFailure = { error ->
                    com.kcalgrindai.app.core.util.KcalGrindAICrashReporter.recordException(
                        error,
                        mapOf(
                            "auth_flow" to "guest_auth",
                            "error_type" to (error::class.simpleName ?: "UnknownException"),
                            "error_detail" to (error.message?.take(200) ?: "unknown")
                        )
                    )
                    val userMessage = resolveGuestErrorMessage(error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = userMessage
                        )
                    }
                }
            )
        }
    }

    companion object {
        fun resolveEmailErrorMessage(
            error: Throwable,
            isSignUpMode: Boolean,
            isDebug: Boolean = com.kcalgrindai.app.BuildConfig.DEBUG
        ): String {
            val msg = error.message.orEmpty()
            return when {
                msg.contains("password", ignoreCase = true) || msg.contains("credential", ignoreCase = true) ->
                    "Invalid email or password."
                msg.contains("collision", ignoreCase = true) || msg.contains("already in use", ignoreCase = true) ->
                    "An account with this email already exists."
                msg.contains("user-not-found", ignoreCase = true) || msg.contains("no user", ignoreCase = true) ->
                    "No account found with this email."
                msg.contains("badly formatted", ignoreCase = true) || msg.contains("invalid email", ignoreCase = true) ->
                    "Please enter a valid email address."
                msg.contains("network", ignoreCase = true) ->
                    "Network error. Please check your internet connection."
                isDebug ->
                    error.localizedMessage ?: "Authentication failed. Please try again."
                else ->
                    if (isSignUpMode) "Unable to create account right now. Please try again later."
                    else "Unable to sign in right now. Please try again later."
            }
        }

        fun resolveGoogleErrorMessage(
            error: Throwable,
            isDebug: Boolean = com.kcalgrindai.app.BuildConfig.DEBUG
        ): String {
            val msg = error.message.orEmpty()
            return when {
                msg.contains("network", ignoreCase = true) ->
                    "Network error. Please check your internet connection."
                isDebug ->
                    error.localizedMessage ?: "Google sign in failed."
                else ->
                    "Unable to sign in with Google right now. Please try again later."
            }
        }

        fun resolveGoogleFailureMessage(
            message: String,
            isDebug: Boolean = com.kcalgrindai.app.BuildConfig.DEBUG
        ): String {
            return if (isDebug) {
                message
            } else {
                "Google sign in was unsuccessful. Please try again."
            }
        }

        fun resolveGuestErrorMessage(
            error: Throwable,
            isDebug: Boolean = com.kcalgrindai.app.BuildConfig.DEBUG
        ): String {
            val msg = error.message.orEmpty()
            return when {
                msg.contains("network", ignoreCase = true) ->
                    "Network error. Please check your internet connection."
                isDebug ->
                    error.localizedMessage ?: "Failed to continue as guest."
                else ->
                    "Unable to continue as guest right now. Please try again later."
            }
        }
    }
}
