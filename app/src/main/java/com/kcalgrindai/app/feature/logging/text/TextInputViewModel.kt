package com.kcalgrindai.app.feature.logging.text

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.domain.model.AIAnalysisResult
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

data class TextInputUiState(
    val queryText: String = "",
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false
)

@HiltViewModel
class TextInputViewModel @Inject constructor(
    private val aiAnalysisRepository: AIAnalysisRepository,
    private val userProfileRepository: UserProfileRepository,
    private val sessionManager: LoggingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TextInputUiState())
    val uiState: StateFlow<TextInputUiState> = _uiState.asStateFlow()

    fun onTextChanged(newText: String) {
        _uiState.update { it.copy(queryText = newText, errorMessage = null, isNetworkError = false) }
    }

    fun selectExample(example: String) {
        _uiState.update { it.copy(queryText = example, errorMessage = null, isNetworkError = false) }
    }

    fun analyzeMeal(onSuccess: () -> Unit) {
        val query = _uiState.value.queryText.trim()
        if (query.isBlank()) return

        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null, isNetworkError = false) }

        viewModelScope.launch {
            val profile = userProfileRepository.getProfile()
            val result = aiAnalysisRepository.analyzeText(
                text = query,
                dietTags = profile?.dietTags ?: emptyList(),
                allergies = profile?.allergies ?: emptyList()
            )

            result.fold(
                onSuccess = { analysis ->
                    sessionManager.setAiAnalysisResult(analysis, source = "ai_text")
                    _uiState.update { it.copy(isAnalyzing = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            errorMessage = error.message ?: "AI analysis failed. Please check your connection.",
                            isNetworkError = true
                        )
                    }
                }
            )
        }
    }
}
