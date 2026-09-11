package com.lumina.nutrition.feature.insights

import androidx.compose.runtime.Composable
import com.lumina.nutrition.core.navigation.LuminaRoute
import com.lumina.nutrition.core.navigation.SkeletonScreen

@Composable
fun InsightsSkeletonScreen(
    destination: LuminaRoute,
    onNavigate: (String) -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    SkeletonScreen(destination = destination, onNavigate = onNavigate, bottomBar = bottomBar)
}

