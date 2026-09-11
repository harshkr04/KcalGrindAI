package com.lumina.nutrition.feature.aicoach

import androidx.compose.runtime.Composable
import com.lumina.nutrition.core.navigation.LuminaRoute
import com.lumina.nutrition.core.navigation.SkeletonScreen

@Composable
fun AiSkeletonScreen(destination: LuminaRoute, onNavigate: (String) -> Unit) {
    SkeletonScreen(destination = destination, onNavigate = onNavigate)
}
