package com.kcalgrindai.app.feature.onboarding.breather

import androidx.lifecycle.ViewModel
import com.kcalgrindai.app.feature.onboarding.state.OnboardingDraftState
import com.kcalgrindai.app.feature.onboarding.state.OnboardingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class BreatherActivityViewModel @Inject constructor(
    sessionManager: OnboardingSessionManager
) : ViewModel() {
    val draftState: StateFlow<OnboardingDraftState> = sessionManager.draftState
}
