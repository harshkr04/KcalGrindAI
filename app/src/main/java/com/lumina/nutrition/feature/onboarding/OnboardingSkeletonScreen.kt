package com.lumina.nutrition.feature.onboarding

import androidx.compose.runtime.Composable
import com.lumina.nutrition.core.navigation.LuminaRoute
import com.lumina.nutrition.core.navigation.SkeletonScreen

@Composable
fun OnboardingSkeletonScreen(destination: LuminaRoute, onNavigate: (String) -> Unit) {
    SkeletonScreen(destination = destination, onNavigate = onNavigate)
}
