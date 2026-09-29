package com.kcalgrindai.app.feature.logging.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.domain.repository.AIAnalysisRepository
import com.kcalgrindai.app.domain.repository.UserProfileRepository
import com.kcalgrindai.app.feature.logging.state.LoggingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceInputUiState(
    val isListening: Boolean = false,
    val transcript: String = "",
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false
)

@HiltViewModel
class VoiceInputViewModel @Inject constructor(
    private val aiAnalysisRepository: AIAnalysisRepository,
    private val userProfileRepository: UserProfileRepository,
    private val sessionManager: LoggingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceInputUiState())
    val uiState: StateFlow<VoiceInputUiState> = _uiState.asStateFlow()

    fun setListening(listening: Boolean) {
        _uiState.update { it.copy(isListening = listening, errorMessage = null) }
    }

    fun onTranscriptReceived(transcript: String) {
        _uiState.update { it.copy(transcript = transcript, isListening = false, errorMessage = null) }
    }

    fun simulateTranscript(sample: String) {
        _uiState.update { it.copy(transcript = sample, isListening = false, errorMessage = null) }
    }

    fun analyzeTranscript(onSuccess: () -> Unit) {
        val query = _uiState.value.transcript.trim()
        if (query.isBlank()) return

        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null, isNetworkError = false) }

        viewModelScope.launch {
            val profile = userProfileRepository.getProfile()
            val result = aiAnalysisRepository.transcribeAndAnalyze(
                transcript = query,
                dietTags = profile?.dietTags ?: emptyList(),
                allergies = profile?.allergies ?: emptyList()
            )

            result.fold(
                onSuccess = { analysis ->
                    sessionManager.setAiAnalysisResult(analysis, source = "ai_voice")
                    _uiState.update { it.copy(isAnalyzing = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            errorMessage = error.message ?: "Voice analysis failed. Please check your connection.",
                            isNetworkError = true
                        )
                    }
                }
            )
        }
    }
}
