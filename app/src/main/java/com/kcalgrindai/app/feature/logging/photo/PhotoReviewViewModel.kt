package com.kcalgrindai.app.feature.logging.photo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.core.util.PhotoDraftStore
import com.kcalgrindai.app.domain.model.AIAnalysisResult
import com.kcalgrindai.app.domain.usecase.AnalyzeFoodUseCase
import com.kcalgrindai.app.domain.usecase.PhotoAnalysisException
import com.kcalgrindai.app.feature.logging.state.LoggingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PhotoReviewErrorType {
    PHOTO_TOO_LARGE,
    INVALID_IMAGE,
    AI_UNAVAILABLE,
    NETWORK_ERROR,
    NO_FOOD_DETECTED
}

data class PhotoReviewUiState(
    val photoPath: String? = null,
    val isLoaded: Boolean = false,
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null,
    val errorType: PhotoReviewErrorType? = null,
    val isNetworkError: Boolean = false,
    val isNoFoodDetected: Boolean = false
)

@HiltViewModel
class PhotoReviewViewModel @Inject constructor(
    private val analyzeFoodUseCase: AnalyzeFoodUseCase,
    private val sessionManager: LoggingSessionManager,
    private val photoDraftStore: PhotoDraftStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoReviewUiState())
    val uiState: StateFlow<PhotoReviewUiState> = _uiState.asStateFlow()

    init {
        val path = sessionManager.state.value.capturedPhotoPath
        _uiState.update { it.copy(photoPath = path, isLoaded = true) }
    }

    fun setPhotoPath(path: String) {
        sessionManager.setCapturedPhotoPath(path)
        _uiState.update {
            it.copy(
                photoPath = path,
                errorMessage = null,
                errorType = null,
                isNetworkError = false,
                isNoFoodDetected = false
            )
        }
    }

    fun analyzePhoto(onSuccess: (AIAnalysisResult) -> Unit) {
        val currentPath = _uiState.value.photoPath
        if (currentPath == null) {
            _uiState.update {
                it.copy(
                    errorMessage = "No photo selected. Please retake.",
                    errorType = PhotoReviewErrorType.INVALID_IMAGE
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isAnalyzing = true,
                errorMessage = null,
                errorType = null,
                isNetworkError = false,
                isNoFoodDetected = false
            )
        }

        viewModelScope.launch {
            val result = analyzeFoodUseCase(currentPath)

            result.fold(
                onSuccess = { analysis ->
                    if (analysis.foods.isEmpty()) {
                        // Empty foods list -> defined "no food detected" state
                        // In every case keep the photo draft
                        _uiState.update {
                            it.copy(
                                isAnalyzing = false,
                                isNoFoodDetected = true,
                                errorType = PhotoReviewErrorType.NO_FOOD_DETECTED,
                                errorMessage = "No food detected in this photo. Please retake with a closer view or search manually."
                            )
                        }
                    } else {
                        // Foods detected -> save to session and invoke success callback
                        sessionManager.setAiAnalysisResult(analysis, source = "ai_photo")
                        _uiState.update { it.copy(isAnalyzing = false) }
                        onSuccess(analysis)
                    }
                },
                onFailure = { error ->
                    val (errorType, message, isNet) = when (error) {
                        is PhotoAnalysisException.PhotoTooLarge -> {
                            Triple(
                                PhotoReviewErrorType.PHOTO_TOO_LARGE,
                                "Photo too large. Please retake photo.",
                                false
                            )
                        }
                        is PhotoAnalysisException.InvalidImage -> {
                            Triple(
                                PhotoReviewErrorType.INVALID_IMAGE,
                                "Could not read this photo. Please retake with better lighting.",
                                false
                            )
                        }
                        is PhotoAnalysisException.AiUnavailable -> {
                            Triple(
                                PhotoReviewErrorType.AI_UNAVAILABLE,
                                "Meal analysis is busy right now. Please try again in a minute.",
                                false
                            )
                        }
                        is PhotoAnalysisException.NoFoodDetected -> {
                            Triple(
                                PhotoReviewErrorType.NO_FOOD_DETECTED,
                                error.customMessage,
                                false
                            )
                        }
                        is PhotoAnalysisException.NetworkError -> {
                            Triple(
                                PhotoReviewErrorType.NETWORK_ERROR,
                                "AI logging needs a connection. Unable to reach the AI server.",
                                true
                            )
                        }
                        else -> {
                            Triple(
                                PhotoReviewErrorType.NETWORK_ERROR,
                                error.message ?: "Vision analysis failed. Please check your connection.",
                                true
                            )
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            errorMessage = message,
                            errorType = errorType,
                            isNetworkError = isNet,
                            isNoFoodDetected = (errorType == PhotoReviewErrorType.NO_FOOD_DETECTED)
                        )
                    }
                }
            )
        }
    }

    fun discardDraft() {
        val path = _uiState.value.photoPath
        if (path != null) {
            photoDraftStore.deleteDraft(path)
            sessionManager.setCapturedPhotoPath(null)
            _uiState.update { it.copy(photoPath = null) }
        }
    }
}
