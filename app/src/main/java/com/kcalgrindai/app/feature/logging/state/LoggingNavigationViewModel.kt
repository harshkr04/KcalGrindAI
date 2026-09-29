package com.kcalgrindai.app.feature.logging.state

import androidx.lifecycle.ViewModel
import com.kcalgrindai.app.core.util.PhotoDraftStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoggingNavigationViewModel @Inject constructor(
    val sessionManager: LoggingSessionManager,
    val photoDraftStore: PhotoDraftStore
) : ViewModel()
