package com.lumina.nutrition.feature.logging.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumina.nutrition.core.designsystem.LuminaBackground
import com.lumina.nutrition.core.designsystem.LuminaOnPrimary
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSecondaryContainer
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLow
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest

@Composable
fun AddFoodSheetScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToBarcode: () -> Unit,
    onNavigateToPhoto: () -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToText: () -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onNavigateBack),
                    shape = CircleShape,
                    color = LuminaSurfaceContainerLowest,
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "✕", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LuminaOnSurface)
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "Add to Food Log",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LuminaBackground)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Choose how you'd like to log your meal",
                style = MaterialTheme.typography.bodyMedium.copy(color = LuminaOnSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "DATABASE & SCANNING",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = LuminaPrimary,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Search Database Card
            LogOptionCard(
                icon = "🔍",
                title = "Search Database",
                subtitle = "Look up 300,000+ USDA foods & verified brands",
                badge = "Fast",
                badgeColor = LuminaSecondaryContainer,
                badgeTextColor = LuminaPrimary,
                onClick = onNavigateToSearch
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Barcode Scanner Card
            LogOptionCard(
                icon = "📷",
                title = "Barcode Scanner",
                subtitle = "Scan packaged foods with instant Open Food Facts lookup",
                badge = "Live UPC",
                badgeColor = LuminaSecondaryContainer,
                badgeTextColor = LuminaPrimary,
                onClick = onNavigateToBarcode
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AI LOGGING (PHASE 5)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = LuminaOnSurfaceVariant,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Photo Card (Phase 5 skeleton)
            LogOptionCard(
                icon = "📸",
                title = "Snap a Meal Photo",
                subtitle = "Multi-item visual breakdown with confidence scoring",
                badge = "Phase 5",
                badgeColor = LuminaSurfaceContainerLow,
                badgeTextColor = LuminaOnSurfaceVariant,
                onClick = onNavigateToPhoto
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Voice Card (Phase 5 skeleton)
            LogOptionCard(
                icon = "🎙️",
                title = "Voice Log",
                subtitle = "Speak naturally, e.g. \"2 eggs, whole wheat toast with butter\"",
                badge = "Phase 5",
                badgeColor = LuminaSurfaceContainerLow,
                badgeTextColor = LuminaOnSurfaceVariant,
                onClick = onNavigateToVoice
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Text Card (Phase 5 skeleton)
            LogOptionCard(
                icon = "✍️",
                title = "Describe Meal",
                subtitle = "Type out what you ate for structured AI extraction",
                badge = "Phase 5",
                badgeColor = LuminaSurfaceContainerLow,
                badgeTextColor = LuminaOnSurfaceVariant,
                onClick = onNavigateToText
            )

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
fun LogOptionCard(
    icon: String,
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    badgeTextColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = LuminaSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = LuminaSecondaryContainer.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 22.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaOnSurface
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = badgeTextColor,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LuminaOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }

            Text(
                text = "›",
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = LuminaOnSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
