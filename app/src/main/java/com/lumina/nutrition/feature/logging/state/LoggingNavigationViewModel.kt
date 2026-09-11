package com.lumina.nutrition.feature.logging.state

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoggingNavigationViewModel @Inject constructor(
    val sessionManager: LoggingSessionManager
) : ViewModel()
