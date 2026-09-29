package com.kcalgrindai.app.core.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import com.kcalgrindai.app.core.designsystem.components.KcalGrindCoachIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

enum class BottomNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME(KcalGrindRoute.Home.route, "Home", Icons.Default.Home),
    DIARY(KcalGrindRoute.Diary.route, "Diary", Icons.Default.DateRange),
    INSIGHTS(KcalGrindRoute.Insights.route, "Insights", Icons.Default.Info),
    AI_COACH(KcalGrindRoute.AiChat.route, "AI Coach", Icons.Outlined.ChatBubbleOutline)
}

/**
 * Liquid Glass floating pill bottom navigation bar per design-tokens.md Section 5.
 * Features real backdrop blur and soft diffusion of content underneath.
 * Order: Home | Diary | [+] | Insights | AI Coach
 */
@Composable
fun KcalGrindBottomNavigationBar(
    currentRoute: String,
    onNavigateToTab: (String) -> Unit,
    onOpenAddFood: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val navShape = RoundedCornerShape(34.dp)
    
    // Theme-aware liquid frosted glass colors (~15% transparent / 85% opaque diffused)
    val glassColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.84f)
    } else {
        Color.White.copy(alpha = 0.84f)
    }
    
    val baseBackground = if (isDark) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.08f)
    }

    val hazeStyle = remember(isDark, glassColor, baseBackground) {
        HazeDefaults.style(
            backgroundColor = baseBackground,
            tint = HazeDefaults.tint(glassColor),
            blurRadius = 30.dp,
            noiseFactor = 0.04f
        )
    }

    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val borderStroke = remember(isDark, outlineVariant) {
        BorderStroke(
            width = 1.dp,
            color = if (isDark) outlineVariant.copy(alpha = 0.40f) else outlineVariant.copy(alpha = 0.30f)
        )
    }

    val shadowSpotColor = if (isDark) {
        Color.Black.copy(alpha = 0.50f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    val shadowAmbientColor = if (isDark) {
        Color.Black.copy(alpha = 0.40f)
    } else {
        Color.Black.copy(alpha = 0.04f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating Liquid Glass Pill with real backdrop blur (~15% transparent / 85% opaque diffused)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = navShape,
                    spotColor = shadowSpotColor,
                    ambientColor = shadowAmbientColor
                )
                .then(
                    if (hazeState != null) {
                        Modifier
                            .clip(navShape)
                            .hazeChild(
                                state = hazeState,
                                style = hazeStyle
                            )
                    } else {
                        Modifier
                    }
                ),
            shape = navShape,
            color = if (hazeState != null) Color.Transparent else glassColor,
            border = borderStroke,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Home
                NavTabItem(
                    tab = BottomNavTab.HOME,
                    isSelected = currentRoute == KcalGrindRoute.Home.route,
                    onClick = { onNavigateToTab(KcalGrindRoute.Home.route) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 2: Diary
                NavTabItem(
                    tab = BottomNavTab.DIARY,
                    isSelected = currentRoute == KcalGrindRoute.Diary.route,
                    onClick = { onNavigateToTab(KcalGrindRoute.Diary.route) },
                    modifier = Modifier.weight(1f)
                )

                // Central [+] Add Food Button with tactile scale press feedback
                val addInteractionSource = remember { MutableInteractionSource() }
                val isAddPressed by addInteractionSource.collectIsPressedAsState()
                val addScale by animateFloatAsState(
                    targetValue = if (isAddPressed) 0.93f else 1.0f,
                    animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
                    label = "addFoodScale"
                )

                Box(
                    modifier = Modifier.weight(1.1f),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        onClick = onOpenAddFood,
                        interactionSource = addInteractionSource,
                        modifier = Modifier
                            .size(52.dp)
                            .scale(addScale)
                            .shadow(
                                elevation = 6.dp,
                                shape = CircleShape,
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            ),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Food",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Tab 3: Insights
                NavTabItem(
                    tab = BottomNavTab.INSIGHTS,
                    isSelected = currentRoute == KcalGrindRoute.Insights.route,
                    onClick = { onNavigateToTab(KcalGrindRoute.Insights.route) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 4: AI Coach
                NavTabItem(
                    tab = BottomNavTab.AI_COACH,
                    isSelected = currentRoute == KcalGrindRoute.AiChat.route,
                    onClick = { onNavigateToTab(KcalGrindRoute.AiChat.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    tab: BottomNavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                if (tab == BottomNavTab.AI_COACH) {
                    KcalGrindCoachIcon(
                        tint = contentColor,
                        size = 22.dp
                    )
                } else {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
        Text(
            text = tab.label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = fontWeight,
            maxLines = 1
        )
    }
}
