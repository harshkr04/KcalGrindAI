package com.lumina.nutrition.feature.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.nutrition.domain.repository.AuthRepository
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
    private val authRepository: AuthRepository
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

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun submit() {
        val state = _uiState.value
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
                    _uiState.update { it.copy(isLoading = false, authSuccess = true) }
                },
                onFailure = { error ->
                    val userMessage = when {
                        error.message?.contains("password", ignoreCase = true) == true ->
                            "Invalid email or password."
                        error.message?.contains("collision", ignoreCase = true) == true ||
                        error.message?.contains("already in use", ignoreCase = true) == true ->
                            "An account with this email already exists."
                        error.message?.contains("network", ignoreCase = true) == true ->
                            "Network error. Please check your internet connection."
                        else -> error.localizedMessage ?: "Authentication failed. Please try again."
                    }
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

    fun onGoogleSignInSuccess(idToken: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, authSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Google sign in failed."
                        )
                    }
                }
            )
        }
    }

    fun continueAsGuest() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.signInAsGuest()
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, authSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to continue as guest."
                        )
                    }
                }
            )
        }
    }
}
