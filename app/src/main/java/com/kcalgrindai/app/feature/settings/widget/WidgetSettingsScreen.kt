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
import com.kcalgrindai.app.core.designsystem.KcalGrindPrimary
import com.kcalgrindai.app.core.designsystem.KcalGrindSecondaryContainer

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
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
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
                                if (isAdded) Color(0xFFB0F1CC) else MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAdded) Icons.Default.CheckCircle else Icons.Default.Widgets,
                            contentDescription = null,
                            tint = if (isAdded) Color(0xFF0F5238) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isAdded) "Widget status: Added" else "Widget status: Not added",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAdded) Color(0xFFB0F1CC).copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = if (isAdded) "Active" else "Inactive",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isAdded) Color(0xFF0F5238) else MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = MaterialTheme.colorScheme.primary,
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
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAdded) "Add another widget" else "Add to home screen",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
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
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
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
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "Live Android dynamic Glance companion",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFB0F1CC).copy(alpha = 0.4f)
                ) {
                    Text(
                        text = "Android 12+ Glance",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F5238),
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
                color = MaterialTheme.colorScheme.surfaceContainerLow,
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
                        text = "2×2 Quick Glance",
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
                color = MaterialTheme.colorScheme.surfaceContainerLow,
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Material 3 Dynamic Theming supported with system palette",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
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
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun WidgetDetailed4x2Preview(isPrivacyEnabled: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFB0F1CC),
        border = BorderStroke(1.dp, Color(0xFF9AE3B9)),
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
                        tint = Color(0xFF0F5238),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "KCAL GRIND AI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F5238),
                            fontSize = 11.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F5238).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (isPrivacyEnabled) "Secure Mode" else "1,420 / 2,100",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F5238),
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
                    color = Color.White.copy(alpha = 0.6f),
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
                            color = Color(0xFF191C1C),
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = if (isPrivacyEnabled) "•••• / ••••" else "Budget: 2,100 kcal",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF404943),
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
                                        if (isFilled) Color(0xFF0F5238) else Color(0xFFC8F7DC)
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
                    barColor = Color(0xFF2D6A4F),
                    modifier = Modifier.weight(1f)
                )
                MacroSettingsPreviewPill(
                    label = if (isPrivacyEnabled) "P: •••g" else "P: 120g",
                    fraction = if (isPrivacyEnabled) 0.0f else 0.80f,
                    barColor = Color(0xFF195E3E),
                    modifier = Modifier.weight(1f)
                )
                MacroSettingsPreviewPill(
                    label = if (isPrivacyEnabled) "F: ••g" else "F: 55g",
                    fraction = if (isPrivacyEnabled) 0.0f else 0.50f,
                    barColor = Color(0xFF407855),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun WidgetQuick2x2Preview(isPrivacyEnabled: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFB0F1CC),
        border = BorderStroke(1.dp, Color(0xFF9AE3B9)),
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
                        .background(Color(0xFF0F5238))
                )
                Text(
                    text = "KCAL GRIND",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F5238),
                        fontSize = 11.sp
                    )
                )
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF0F5238),
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
                    color = Color(0xFF191C1C),
                    fontSize = 16.sp
                )
            )

            Text(
                text = if (isPrivacyEnabled) "Privacy active" else "Sproutling is energized",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF404943),
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
                    .background(Color(0xFFC8F7DC))
            ) {
                if (!isPrivacyEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF0F5238))
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
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF404943)
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.6f))
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
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
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
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hides calorie and macro numbers when device is locked or widget is visible to others.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    )
                }

                Switch(
                    checked = isPrivacyEnabled,
                    onCheckedChange = onTogglePrivacy,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = KcalGrindPrimary,
                        checkedTrackColor = KcalGrindSecondaryContainer
                    ),
                    modifier = Modifier.semantics { contentDescription = "Toggle hiding numbers on widget" }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isPrivacyEnabled) Color(0xFFB0F1CC) else MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = if (isPrivacyEnabled) "•••• kcal masked" else "680 kcal visible",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isPrivacyEnabled) Color(0xFF0F5238) else MaterialTheme.colorScheme.onSurface,
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
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Manual Setup Instructions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            )
            Text(
                text = "If automatic pinning is not supported on your launcher:",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            ManualStepRow(stepNumber = "1", stepText = "Long-press an empty area of the home screen.")
            Spacer(modifier = Modifier.height(10.dp))
            ManualStepRow(stepNumber = "2", stepText = "Open Widgets.")
            Spacer(modifier = Modifier.height(10.dp))
            ManualStepRow(stepNumber = "3", stepText = "Find Kcal Grind AI.")
            Spacer(modifier = Modifier.height(10.dp))
            ManualStepRow(stepNumber = "4", stepText = "Drag the widget onto the home screen.")

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Works with Pixel Launcher, Nova, One UI, and other modern launchers",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun ManualStepRow(stepNumber: String, stepText: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            )
        }
        Text(
            text = stepText,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
