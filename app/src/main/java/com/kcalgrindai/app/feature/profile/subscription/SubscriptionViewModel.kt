package com.kcalgrindai.app.feature.profile.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalgrindai.app.domain.model.SubscriptionPlan
import com.kcalgrindai.app.domain.repository.BillingRepository
import com.kcalgrindai.app.domain.repository.BillingStatus
import com.kcalgrindai.app.domain.repository.PurchaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val plans: List<SubscriptionPlan> = emptyList(),
    val selectedPlanId: String = "kcal_grind_pro_annual",
    val isPro: Boolean = false,
    val activePlanId: String? = null,
    val activeExpiresAtMs: Long? = null,
    val isLoading: Boolean = false,
    val billingStatus: BillingStatus = BillingStatus.Ready,
    val alertMessage: String? = null,
    val isAlertSuccess: Boolean = false
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SubscriptionUiState(
            plans = billingRepository.getAvailablePlans(),
            activePlanId = billingRepository.getActivePlanId(),
            activeExpiresAtMs = billingRepository.getActiveExpiresAt()
        )
    )
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            billingRepository.observeIsPro().collect { isPro ->
                _uiState.update {
                    it.copy(
                        isPro = isPro,
                        activePlanId = if (isPro) billingRepository.getActivePlanId() else null,
                        activeExpiresAtMs = if (isPro) billingRepository.getActiveExpiresAt() else null
                    )
                }
            }
        }
        viewModelScope.launch {
            billingRepository.observeBillingStatus().collect { status ->
                _uiState.update { it.copy(billingStatus = status) }
            }
        }
    }

    fun selectPlan(planId: String) {
        _uiState.update { it.copy(selectedPlanId = planId) }
    }

    fun onSubscribeClicked() {
        val planId = _uiState.value.selectedPlanId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, alertMessage = null) }
            val result = billingRepository.subscribe(planId)
            when (result) {
                is PurchaseResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPro = true,
                            activePlanId = result.planId,
                            activeExpiresAtMs = result.expiresAtMs,
                            alertMessage = "Successfully subscribed to Kcal Grind Pro! All features unlocked.",
                            isAlertSuccess = true
                        )
                    }
                }
                is PurchaseResult.Pending -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alertMessage = result.message,
                            isAlertSuccess = false
                        )
                    }
                }
                is PurchaseResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alertMessage = result.message,
                            isAlertSuccess = false
                        )
                    }
                }
            }
        }
    }

    fun onRestoreClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, alertMessage = null) }
            val result = billingRepository.restorePurchases()
            when (result) {
                is PurchaseResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPro = true,
                            activePlanId = result.planId,
                            activeExpiresAtMs = result.expiresAtMs,
                            alertMessage = "Purchases restored successfully! Kcal Grind Pro is active.",
                            isAlertSuccess = true
                        )
                    }
                }
                is PurchaseResult.Pending -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alertMessage = result.message,
                            isAlertSuccess = false
                        )
                    }
                }
                is PurchaseResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            alertMessage = result.message,
                            isAlertSuccess = false
                        )
                    }
                }
            }
        }
    }

    fun onDowngradeToFree() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, alertMessage = null) }
            billingRepository.setPro(false)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isPro = false,
                    activePlanId = null,
                    activeExpiresAtMs = null,
                    alertMessage = "Switched back to Free plan (5 AI Coach messages/day).",
                    isAlertSuccess = true
                )
            }
        }
    }

    fun dismissAlert() {
        _uiState.update { it.copy(alertMessage = null) }
    }
}
