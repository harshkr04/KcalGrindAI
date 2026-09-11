package com.lumina.nutrition.feature.logging.photo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.nutrition.domain.model.AIAnalysisResult
import com.lumina.nutrition.domain.repository.AIAnalysisRepository
import com.lumina.nutrition.domain.repository.UserProfileRepository
import com.lumina.nutrition.feature.logging.state.LoggingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhotoReviewUiState(
    val imageBase64: String? = null,
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false
)

@HiltViewModel
class PhotoReviewViewModel @Inject constructor(
    private val aiAnalysisRepository: AIAnalysisRepository,
    private val userProfileRepository: UserProfileRepository,
    private val sessionManager: LoggingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoReviewUiState())
    val uiState: StateFlow<PhotoReviewUiState> = _uiState.asStateFlow()

    init {
        val captured = sessionManager.state.value.capturedPhotoBase64
        if (captured != null) {
            _uiState.update { it.copy(imageBase64 = captured) }
        }
    }

    fun setPhoto(base64: String) {
        sessionManager.setCapturedPhoto(base64)
        _uiState.update { it.copy(imageBase64 = base64, errorMessage = null, isNetworkError = false) }
    }

    fun analyzePhoto(onSuccess: (AIAnalysisResult) -> Unit) {
        val base64 = _uiState.value.imageBase64 ?: "sample_meal_base64_photo"
        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null, isNetworkError = false) }

        viewModelScope.launch {
            val profile = userProfileRepository.getProfile()
            val result = aiAnalysisRepository.analyzePhoto(
                imageBase64 = "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDH/wAALCAABAAEBAREA/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/9oACAEBAAA/AH8A/9k=",
                dietTags = profile?.dietTags ?: emptyList(),
                allergies = profile?.allergies ?: emptyList()
            )

            result.fold(
                onSuccess = { analysis ->
                    sessionManager.setAiAnalysisResult(analysis, source = "ai_photo")
                    _uiState.update { it.copy(isAnalyzing = false) }
                    onSuccess(analysis)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            errorMessage = error.message ?: "Vision analysis failed. Please check your connection.",
                            isNetworkError = true
                        )
                    }
                }
            )
        }
    }
}
