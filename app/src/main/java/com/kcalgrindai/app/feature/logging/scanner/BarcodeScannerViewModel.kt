package com.kcalgrindai.app.feature.logging.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.domain.repository.FoodRepository
import com.kcalgrindai.app.feature.logging.state.LoggingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BarcodeScannerUiState(
    val isScanning: Boolean = true,
    val isLookingUp: Boolean = false,
    val scannedBarcode: String? = null,
    val showNotFoundDialog: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val loggingSessionManager: LoggingSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BarcodeScannerUiState())
    val uiState: StateFlow<BarcodeScannerUiState> = _uiState.asStateFlow()

    fun onBarcodeDetected(barcode: String, onNavigateToDetail: () -> Unit) {
        if (_uiState.value.isLookingUp) return

        _uiState.update {
            it.copy(
                isLookingUp = true,
                scannedBarcode = barcode,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = foodRepository.lookupBarcode(barcode)
            result.fold(
                onSuccess = { food ->
                    if (food != null) {
                        loggingSessionManager.setSelectedFood(food)
                        _uiState.update { it.copy(isLookingUp = false) }
                        onNavigateToDetail()
                    } else {
                        _uiState.update {
                            it.copy(
                                isLookingUp = false,
                                showNotFoundDialog = true
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLookingUp = false,
                            showNotFoundDialog = true,
                            errorMessage = error.message ?: "Failed to look up barcode"
                        )
                    }
                }
            )
        }
    }

    fun dismissNotFoundDialog() {
        _uiState.update { it.copy(showNotFoundDialog = false, isLookingUp = false) }
    }

    fun fallbackToManualSearch(onNavigateToSearch: () -> Unit) {
        val barcode = _uiState.value.scannedBarcode
        loggingSessionManager.setPrefilledSearchQuery(barcode)
        _uiState.update { it.copy(showNotFoundDialog = false, isLookingUp = false) }
        onNavigateToSearch()
    }
}
