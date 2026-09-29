package com.kcalgrindai.app.feature.insights

import androidx.compose.runtime.Composable
import com.kcalgrindai.app.core.navigation.KcalGrindRoute
import com.kcalgrindai.app.core.navigation.SkeletonScreen

@Composable
fun InsightsSkeletonScreen(
    destination: KcalGrindRoute,
    onNavigate: (String) -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    SkeletonScreen(destination = destination, onNavigate = onNavigate, bottomBar = bottomBar)
}

