package com.kcalgrindai.app.feature.onboarding.breather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kcalgrindai.app.core.designsystem.KcalGrindPrimary
import com.kcalgrindai.app.core.designsystem.KcalGrindPrimaryContainer
import com.kcalgrindai.app.core.designsystem.KcalGrindSecondaryContainer
import com.kcalgrindai.app.core.designsystem.components.KcalGrindPrimaryButton
import com.kcalgrindai.app.core.designsystem.components.OnboardingTopBar
import com.kcalgrindai.app.core.designsystem.components.StickyBottomBar

@Composable
fun BreatherActivityScreen(
    onNavigateNext: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: BreatherActivityViewModel = hiltViewModel()
) {
    val draft by viewModel.draftState.collectAsState()
    val name = draft.firstName
    val headline = if (name.isNotBlank()) "Every bit of movement counts, $name!" else "Every bit of movement counts!"

    Scaffold(
        topBar = {
            OnboardingTopBar(
                currentStep = 8,
                totalSteps = 14,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            StickyBottomBar {
                KcalGrindPrimaryButton(
                    text = "Next",
                    onClick = onNavigateNext
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lightweight Kinetic Spark Hero Orb
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                KcalGrindPrimaryContainer.copy(alpha = 0.8f),
                                KcalGrindSecondaryContainer.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = KcalGrindPrimaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = KcalGrindPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = headline,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.75f)
            ) {
                Text(
                    text = "Your daily movement helps us dial in your baseline energy expenditure. Small, steady habits build compounding momentum that lasts.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
