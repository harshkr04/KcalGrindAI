package com.kcalgrindai.app.feature.settings.widget

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kcalgrindai.app.core.designsystem.KcalGrindOnPrimary
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kcalgrindai.app.R
import com.kcalgrindai.app.core.designsystem.v3Colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WidgetSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Home Screen Widget",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics { contentDescription = "Go back" }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. STATUS CARD
            WidgetStatusCard(
                isAdded = uiState.isWidgetAdded,
                installedCount = uiState.installedWidgetCount,
                onAddWidgetClick = { viewModel.requestPinWidget() }
            )

            // 2. WIDGET PREVIEW SHOWCASE CARD
            WidgetPreviewShowcaseCard(
                selectedTab = uiState.selectedTab,
                isPrivacyEnabled = uiState.isPrivacyEnabled,
                onSelectTab = { viewModel.selectTab(it) }
            )

            // 3. PRIVACY SETTINGS CARD
            WidgetPrivacyCard(
                isPrivacyEnabled = uiState.isPrivacyEnabled,
                onTogglePrivacy = { viewModel.togglePrivacy(it) }
            )

            // 4. MANUAL SETUP INSTRUCTIONS CARD
            WidgetManualInstructionsCard()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun WidgetStatusCard(
    isAdded: Boolean,
    installedCount: Int,
    onAddWidgetClick: () -> Unit
) {
    val v3 = MaterialTheme.v3Colors
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = v3.surface,
        border = BorderStroke(1.dp, v3.subtleBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isAdded) v3.emeraldAccent.copy(alpha = 0.2f) else v3.surfaceElevated
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAdded) Icons.Default.CheckCircle else Icons.Default.Widgets,
                            contentDescription = null,
                            tint = if (isAdded) v3.emeraldAccent else v3.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isAdded) "Widget status: Added" else "Widget status: Not added",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = v3.textPrimary,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isAdded) {
                                if (installedCount == 1) "1 active on home screen" else "$installedCount active on home screen"
                            } else {
                                "Not currently placed on home screen"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = v3.textSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAdded) v3.emeraldAccent.copy(alpha = 0.2f) else v3.surfaceElevated
                ) {
                    Text(
                        text = if (isAdded) "Active" else "Inactive",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isAdded) v3.emeraldAccent else v3.textSecondary,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                onClick = onAddWidgetClick,
                shape = RoundedCornerShape(14.dp),
                color = v3.emeraldAccent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = KcalGrindOnPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAdded) "Add another widget" else "Add to home screen",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = KcalGrindOnPrimary,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun WidgetPreviewShowcaseCard(
    selectedTab: WidgetSizeTab,
    isPrivacyEnabled: Boolean,
    onSelectTab: (WidgetSizeTab) -> Unit
) {
    val v3 = MaterialTheme.v3Colors
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = v3.surface,
        border = BorderStroke(1.dp, v3.subtleBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Available Widget Sizes",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3.textPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "Live home-screen widget companion",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = v3.textSecondary,
                            fontSize = 12.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = v3.emeraldAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Android 10+",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3.emeraldAccent,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Segmented Tabs
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = v3.surfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    WidgetTabButton(
                        text = "4×2 Detailed",
                        isSelected = selectedTab == WidgetSizeTab.DETAILED_4X2,
                        onClick = { onSelectTab(WidgetSizeTab.DETAILED_4X2) },
                        modifier = Modifier.weight(1f)
                    )
                    WidgetTabButton(
                        text = "2×2 Quick Widget",
                        isSelected = selectedTab == WidgetSizeTab.QUICK_2X2,
                        onClick = { onSelectTab(WidgetSizeTab.QUICK_2X2) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Preview Surface
            if (selectedTab == WidgetSizeTab.DETAILED_4X2) {
                WidgetDetailed4x2Preview(isPrivacyEnabled = isPrivacyEnabled)
            } else {
                WidgetQuick2x2Preview(isPrivacyEnabled = isPrivacyEnabled)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = v3.surfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = v3.emeraldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Material 3 Dynamic Theming supported with system palette",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = v3.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val v3 = MaterialTheme.v3Colors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) v3.emeraldAccent else Color.Transparent,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) KcalGrindOnPrimary else v3.textSecondary,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun WidgetDetailed4x2Preview(isPrivacyEnabled: Boolean) {
    val v3 = MaterialTheme.v3Colors
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = v3.surface,
        border = BorderStroke(1.dp, v3.subtleBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = v3.emeraldAccent,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "KcalGrind",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = v3.textPrimary,
                            fontSize = 11.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = v3.surfaceElevated
                ) {
                    Text(
                        text = if (isPrivacyEnabled) "Secure Mode" else "1,420 / 2,100",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isPrivacyEnabled) v3.emeraldAccent else v3.textSecondary,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = v3.surfaceElevated,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isPrivacyEnabled) R.drawable.ic_sproutling_secure else R.drawable.ic_sproutling_active
                            ),
                            contentDescription = "Sproutling",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPrivacyEnabled) "•••• kcal left" else "680 kcal left",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = v3.textPrimary,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = if (isPrivacyEnabled) "•••• / ••••" else "Budget: 2,100 kcal",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = v3.textSecondary,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Stepped pixel energy track (Strictly 0 filled in privacy mode)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        for (i in 0 until 10) {
                            val isFilled = !isPrivacyEnabled && i < 7
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (isFilled) v3.emeraldAccent else v3.progressTrack
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Macros (Strictly 0% and •••g in privacy mode)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroSettingsPreviewPill(
                    label = if (isPrivacyEnabled) "C: •••g" else "C: 180g",
                    fraction = if (isPrivacyEnabled) 0.0f else 0.65f,
                    barColor = v3.emeraldAccent,
                    modifier = Modifier.weight(1f)
                )
                MacroSettingsPreviewPill(
                    label = if (isPrivacyEnabled) "P: •••g" else "P: 120g",
                    fraction = if (isPrivacyEnabled) 0.0f else 0.80f,
                    barColor = v3.strongEmerald,
                    modifier = Modifier.weight(1f)
                )
                MacroSettingsPreviewPill(
                    label = if (isPrivacyEnabled) "F: ••g" else "F: 55g",
                    fraction = if (isPrivacyEnabled) 0.0f else 0.50f,
                    barColor = v3.emeraldAccent.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun WidgetQuick2x2Preview(isPrivacyEnabled: Boolean) {
    val v3 = MaterialTheme.v3Colors
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = v3.surface,
        border = BorderStroke(1.dp, v3.subtleBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(v3.emeraldAccent)
                )
                Text(
                    text = "KcalGrind",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = v3.textPrimary,
                        fontSize = 11.sp
                    )
                )
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = v3.emeraldAccent,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                painter = painterResource(
                    id = if (isPrivacyEnabled) R.drawable.ic_sproutling_secure else R.drawable.ic_sproutling_active
                ),
                contentDescription = "Sproutling",
                tint = Color.Unspecified,
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isPrivacyEnabled) "•••• left" else "680 left",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = v3.textPrimary,
                    fontSize = 16.sp
                )
            )

            Text(
                text = if (isPrivacyEnabled) "Privacy active" else "Sproutling is energized",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = v3.textSecondary,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar (0% in privacy mode)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(v3.progressTrack)
            ) {
                if (!isPrivacyEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(v3.emeraldAccent)
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroSettingsPreviewPill(
    label: String,
    fraction: Float,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val v3 = MaterialTheme.v3Colors
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = v3.textSecondary
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(v3.progressTrack)
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(barColor)
                )
            }
        }
    }
}

@Composable
fun WidgetPrivacyCard(
    isPrivacyEnabled: Boolean,
    onTogglePrivacy: (Boolean) -> Unit
) {
    val v3 = MaterialTheme.v3Colors
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = v3.surface,
        border = BorderStroke(1.dp, v3.subtleBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "Hide numbers on widget",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3.textPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hides calorie and macro numbers when device is locked or widget is visible to others.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = v3.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    )
                }

                Switch(
                    checked = isPrivacyEnabled,
                    onCheckedChange = onTogglePrivacy,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = v3.emeraldAccent,
                        checkedTrackColor = v3.emeraldAccent.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.semantics { contentDescription = "Toggle hiding numbers on widget" }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = v3.surfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Simulated display mode:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = v3.textSecondary,
                            fontSize = 12.sp
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isPrivacyEnabled) v3.emeraldAccent.copy(alpha = 0.2f) else v3.surfaceElevated
                    ) {
                        Text(
                            text = if (isPrivacyEnabled) "•••• kcal masked" else "680 kcal visible",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isPrivacyEnabled) v3.emeraldAccent else v3.textPrimary,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetManualInstructionsCard() {
    val v3 = MaterialTheme.v3Colors
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = v3.surface,
        border = BorderStroke(1.dp, v3.subtleBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Manual Setup Instructions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = v3.textPrimary,
                    fontSize = 16.sp
                )
            )
            Text(
                text = "If automatic pinning is not supported on your launcher:",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = v3.textSecondary,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            ManualStepRow(stepNumber = "1", stepText = "Long-press an empty area of the home screen.")
            Spacer(modifier = Modifier.height(10.dp))
            ManualStepRow(stepNumber = "2", stepText = "Open Widgets.")
            Spacer(modifier = Modifier.height(10.dp))
            ManualStepRow(stepNumber = "3", stepText = "Find KcalGrind.")
            Spacer(modifier = Modifier.height(10.dp))
            ManualStepRow(stepNumber = "4", stepText = "Drag the widget onto the home screen.")

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Works with Pixel Launcher, Nova, One UI, and other modern launchers",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = v3.textMuted,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun ManualStepRow(stepNumber: String, stepText: String) {
    val v3 = MaterialTheme.v3Colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(v3.surfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = v3.emeraldAccent,
                    fontSize = 12.sp
                )
            )
        }
        Text(
            text = stepText,
            style = MaterialTheme.typography.bodySmall.copy(
                color = v3.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
