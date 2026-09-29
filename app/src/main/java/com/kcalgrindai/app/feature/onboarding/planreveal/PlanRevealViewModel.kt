package com.kcalgrindai.app.feature.onboarding.planreveal

import androidx.lifecycle.ViewModel
import com.kcalgrindai.app.domain.usecase.CalculateNutritionGoalUseCase
import com.kcalgrindai.app.domain.usecase.NutritionCalculator
import com.kcalgrindai.app.feature.onboarding.state.OnboardingDraftState
import com.kcalgrindai.app.feature.onboarding.state.OnboardingSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlanRevealViewModel @Inject constructor(
    val sessionManager: OnboardingSessionManager,
    private val calculateUseCase: CalculateNutritionGoalUseCase
) : ViewModel() {
    val draftState: StateFlow<OnboardingDraftState> = sessionManager.draftState

    fun calculatePlan(draft: OnboardingDraftState): NutritionCalculator.CalculationResult {
        return calculateUseCase.calculateRaw(
            weightKg = draft.weightKg,
            heightCm = draft.heightCm,
            age = draft.age,
            goal = draft.goal,
            activity = draft.activityLevel,
            goalWeightKg = draft.goalWeightKg,
            dietTags = draft.dietTags
        )
    }
}
