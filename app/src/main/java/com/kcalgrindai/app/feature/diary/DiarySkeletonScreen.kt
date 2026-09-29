package com.kcalgrindai.app.feature.diary

import androidx.compose.runtime.Composable
import com.kcalgrindai.app.core.navigation.KcalGrindRoute
import com.kcalgrindai.app.core.navigation.SkeletonScreen

@Composable
fun DiarySkeletonScreen(destination: KcalGrindRoute, onNavigate: (String) -> Unit) {
    SkeletonScreen(destination = destination, onNavigate = onNavigate)
}
