package com.kcalgrindai.app.feature.onboarding.welcome

import androidx.lifecycle.ViewModel
import com.kcalgrindai.app.feature.onboarding.state.OnboardingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class WelcomeUiState(
    val title: String = "Kcal Grind AI",
    val subtitle: String = "Intelligent nutrition tracking tailored to your biology and lifestyle."
)

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val sessionManager: OnboardingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    fun onGetStarted() {
        sessionManager.resetDraft()
    }
}
