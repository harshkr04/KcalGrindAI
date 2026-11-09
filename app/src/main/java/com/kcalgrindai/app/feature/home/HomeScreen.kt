package com.kcalgrindai.app.feature.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcalgrindai.app.R
import com.kcalgrindai.app.core.designsystem.components.KcalGrindLiquidBadge
import com.kcalgrindai.app.core.designsystem.components.KcalGrindLiquidCard
import com.kcalgrindai.app.core.designsystem.v3Colors
import com.kcalgrindai.app.core.designsystem.KcalGrindChipDarkBg
import com.kcalgrindai.app.core.designsystem.KcalGrindChipLightBg
import com.kcalgrindai.app.core.designsystem.KcalGrindChipDarkBorder
import com.kcalgrindai.app.core.designsystem.KcalGrindChipLightBorder
import com.kcalgrindai.app.core.designsystem.KcalGrindHighlightDarkBg
import com.kcalgrindai.app.core.designsystem.KcalGrindHighlightLightBg
import com.kcalgrindai.app.core.designsystem.KcalGrindHighlightDarkBorder
import com.kcalgrindai.app.core.designsystem.KcalGrindHighlightLightBorder
import com.kcalgrindai.app.core.designsystem.KcalGrindOverBudgetRed
import com.kcalgrindai.app.core.designsystem.KcalGrindDarkBarUnfilled
import com.kcalgrindai.app.core.designsystem.KcalGrindLightBarUnfilled
import com.kcalgrindai.app.core.designsystem.KcalGrindDarkSecondaryBar
import com.kcalgrindai.app.core.designsystem.KcalGrindLightSecondaryBar
import com.kcalgrindai.app.core.designsystem.KcalGrindDarkMutedBar
import com.kcalgrindai.app.core.designsystem.KcalGrindLightMutedBar
import com.kcalgrindai.app.core.designsystem.KcalGrindDeepDarkSurface
import com.kcalgrindai.app.core.designsystem.KcalGrindDeepDarkBorder
import com.kcalgrindai.app.core.designsystem.KcalGrindDeepDarkBorderLight
import com.kcalgrindai.app.core.designsystem.KcalGrindSurfaceCharcoal
import com.kcalgrindai.app.core.designsystem.KcalGrindWaterBlue
import com.kcalgrindai.app.core.designsystem.KcalGrindWaterBlueDark
import com.kcalgrindai.app.core.designsystem.KcalGrindDarkWaterCardBg
import com.kcalgrindai.app.core.designsystem.KcalGrindOnPrimary
import com.kcalgrindai.app.core.designsystem.KcalGrindDarkPromoBg
import com.kcalgrindai.app.core.designsystem.KcalGrindLightPromoBg
import com.kcalgrindai.app.core.designsystem.KcalGrindDarkPromoBorder
import com.kcalgrindai.app.core.designsystem.KcalGrindLightPromoBorder
import com.kcalgrindai.app.core.designsystem.KcalGrindDarkForest
import com.kcalgrindai.app.core.designsystem.KcalGrindLightDeepText
import com.kcalgrindai.app.core.navigation.KcalGrindRoute
import com.kcalgrindai.app.domain.model.MealType
import com.kcalgrindai.app.domain.model.SproutlingMood
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onNavigateToTab: (String) -> Unit,
    onNavigateToAddFood: (MealType?) -> Unit = {},
    onNavigateToMealDetail: (Long) -> Unit,
    onNavigateToAiCoach: () -> Unit = {},
    onNavigateToProfile: () -> Unit = { onNavigateToTab(KcalGrindRoute.Profile.route) },
    onNavigateToWidgetSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val v3 = MaterialTheme.v3Colors
    var showActivityDetailsDialog by remember { mutableStateOf(false) }
    var showRemindersSheet by remember { mutableStateOf(false) }
    val mealRemindersEnabled by viewModel.mealRemindersEnabled.collectAsStateWithLifecycle()
    val hydrationRemindersEnabled by viewModel.hydrationRemindersEnabled.collectAsStateWithLifecycle()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshActivity()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = viewModel.healthConnectManager.createPermissionRequestContract()
    ) {
        viewModel.refreshActivity()
    }

    Scaffold(
        containerColor = v3.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = v3.emeraldAccent)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(v3.background)
            ) {
                // Ambient Liquid Aurora Glow in Background per V3 spec
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    v3.emeraldAccent.copy(alpha = if (v3.isDark) 0.12f else 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Top App Bar / Header
                    item {
                        HomeTopAppBar(
                            userName = uiState.userName,
                            goalLabel = uiState.goalLabel,
                            streakDays = uiState.streakDays,
                            onOpenProfile = onNavigateToProfile,
                            onOpenReminders = { showRemindersSheet = true }
                        )
                    }

                    // Launcher Widget Promo Card (One-time dismissible)
                    if (uiState.showWidgetPromoCard) {
                        item(key = "home_widget_promo_card") {
                            HomeWidgetPromoCard(
                                onAddWidget = { viewModel.pinWidget() },
                                onOpenSettings = onNavigateToWidgetSettings,
                                onDismiss = { viewModel.dismissWidgetPromo() }
                            )
                        }
                    }

                    // 2. HERO CALORIE ENERGY CARD (Liquid Glass + Stepped 10-Block Pixel Energy Meter)
                    item {
                        HomeCalorieEnergyCard(
                            consumedCalories = uiState.consumedCalories,
                            targetCalories = uiState.targetCalories,
                            remainingCalories = uiState.remainingCalories,
                            progressPercent = uiState.progressPercent,
                            protein = uiState.protein,
                            carbs = uiState.carbs,
                            fat = uiState.fat
                        )
                    }

                    // 3. SPROUTLING COMPANION INTERACTIVE CARD
                    item {
                        HomeSproutlingCard(
                            sproutlingMood = uiState.sproutlingMood,
                            consumedCalories = uiState.consumedCalories,
                            remainingCalories = uiState.remainingCalories,
                            protein = uiState.protein,
                            onAskCoach = onNavigateToAiCoach,
                            onAddMeal = { onNavigateToAddFood(null) }
                        )
                    }

                    // 4. QUICK WATER HYDRATION SECTION
                    item {
                        HomeWaterHydrationCard(
                            water = uiState.water,
                            onLogWater = { amount -> viewModel.logWater(amount) },
                            onUndoWater = { viewModel.undoLatestWaterLog() },
                            onEditWater = { id, amount -> viewModel.editWaterLog(id, amount) },
                            onDeleteWater = { id -> viewModel.deleteWaterLog(id) }
                        )
                    }

                    // 5. ACTIVITY & STEPS CARD (Health Connect)
                    item {
                        HomeActivityCard(
                            activity = uiState.activity,
                            onRequestPermission = {
                                permissionLauncher.launch(viewModel.healthConnectManager.requiredPermissions)
                            },
                            onViewDetails = {
                                showActivityDetailsDialog = true
                            }
                        )
                    }

                    // 6. TODAY'S FUEL LOG / RECENT MEALS SUMMARY
                    item {
                        HomeFuelLogSection(
                            meals = uiState.mealsGrouped,
                            totalCaloriesLogged = uiState.consumedCalories,
                            onMealClick = { mealId -> onNavigateToMealDetail(mealId) },
                            onAddMealClick = { mealType -> onNavigateToAddFood(mealType) },
                            onViewAll = { onNavigateToTab(KcalGrindRoute.Diary.route) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showActivityDetailsDialog) {
        ActivityDetailsDialog(
            activity = uiState.activity,
            targetCalories = uiState.targetCalories,
            onDismiss = { showActivityDetailsDialog = false }
        )
    }

    if (showRemindersSheet) {
        com.kcalgrindai.app.feature.home.components.RemindersBottomSheet(
            onDismiss = { showRemindersSheet = false },
            mealRemindersEnabled = mealRemindersEnabled,
            onMealRemindersChanged = { viewModel.setMealRemindersEnabled(it) },
            hydrationRemindersEnabled = hydrationRemindersEnabled,
            onHydrationRemindersChanged = { viewModel.setHydrationRemindersEnabled(it) }
        )
    }
}

// ============================================================================
// 1. Top App Bar (Header with Alex Avatar, Status Rim, & Streak Chip)
// ============================================================================

@Composable
private fun HomeTopAppBar(
    userName: String,
    goalLabel: String,
    streakDays: Int,
    onOpenProfile: () -> Unit,
    onOpenReminders: () -> Unit = onOpenProfile,
    modifier: Modifier = Modifier
) {
    val v3 = MaterialTheme.v3Colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Avatar with Emerald Rim & Greeting
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                com.kcalgrindai.app.core.designsystem.components.CircleIconButton(
                    onClick = onOpenProfile,
                    visualSize = 44.dp,
                    containerColor = v3.surfaceElevated,
                    contentColor = v3.emeraldAccent,
                    border = BorderStroke(2.dp, v3.emeraldAccent),
                    contentDescription = "Profile"
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = v3.emeraldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Online Emerald Status Dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(v3.emeraldAccent)
                        .border(2.dp, v3.background, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hey, ${if (userName.isNotBlank() && userName != "there") userName else "Alex"}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3.textPrimary,
                            fontSize = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "👋", fontSize = 14.sp)
                }
                Text(
                    text = "${if (goalLabel.isNotBlank()) goalLabel else "Hypertrophy Cut"} • Day ${if (streakDays > 0) streakDays else 1}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = v3.textMuted,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Action Chips (Streak & Profile / Settings)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Streak Capsule Chip per V3 design
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = if (v3.isDark) KcalGrindChipDarkBg else KcalGrindChipLightBg,
                border = BorderStroke(1.dp, if (v3.isDark) KcalGrindChipDarkBorder else KcalGrindChipLightBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (streakDays > 0) "🔥" else "⚡",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "$streakDays Days",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3.emeraldAccent,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Reminders / Notification Bell Button (CircleIconButton)
            com.kcalgrindai.app.core.designsystem.components.CircleIconButton(
                onClick = onOpenReminders,
                visualSize = 38.dp,
                containerColor = v3.surfaceElevated,
                contentColor = v3.textSecondary,
                border = BorderStroke(1.dp, v3.subtleBorder),
                contentDescription = "Reminders"
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Reminders",
                    tint = v3.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ============================================================================
// 2. HERO CALORIE ENERGY CARD (Liquid Glass + Stepped 10-Block Pixel Energy Meter)
// ============================================================================

@Composable
private fun HomeCalorieEnergyCard(
    consumedCalories: Int,
    targetCalories: Int,
    remainingCalories: Int,
    progressPercent: Int,
    protein: MacroProgress,
    carbs: MacroProgress,
    fat: MacroProgress,
    modifier: Modifier = Modifier
) {
    val v3 = MaterialTheme.v3Colors
    val isOver = remainingCalories < 0
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    // 10 blocks: calculate how many blocks should be active based on consumption
    val filledBlocks = if (targetCalories > 0) {
        ((consumedCalories.toFloat() / targetCalories.toFloat()) * 10f).roundToInt().coerceIn(0, 10)
    } else 0

    KcalGrindLiquidCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        contentPadding = PaddingValues(20.dp),
        backgroundColor = v3.surface,
        borderColor = v3.subtleBorder
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Header Row: Daily Energy Arc & Target Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(v3.emeraldAccent)
                    )
                    Text(
                        text = "DAILY ENERGY ARC",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3.textMuted,
                            letterSpacing = 0.8.sp,
                            fontSize = 11.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (v3.isDark) KcalGrindHighlightDarkBg else KcalGrindHighlightLightBg,
                    border = BorderStroke(1.dp, if (v3.isDark) KcalGrindHighlightDarkBorder else KcalGrindHighlightLightBorder)
                ) {
                    Text(
                        text = "Target: ${numberFormatter.format(targetCalories)} kcal",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3.emeraldAccent,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Divider Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(v3.subtleBorder.copy(alpha = 0.5f))
            )

            // Center Energy Arc: Stepped 10-Block Pixel Energy Meter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Stepped 10-Block Pixel Energy Meter per V3 HTML design
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..10) {
                        val isActive = i <= filledBlocks
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        isOver -> KcalGrindOverBudgetRed
                                        isActive -> v3.emeraldAccent
                                        else -> if (v3.isDark) KcalGrindDarkBarUnfilled else KcalGrindLightBarUnfilled
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isActive) v3.emeraldAccent else v3.subtleBorder,
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bold Numbers & Readout
                val displayCalories = if (isOver) -remainingCalories else remainingCalories
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = numberFormatter.format(displayCalories),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isOver) KcalGrindOverBudgetRed else v3.textPrimary,
                            fontSize = 44.sp,
                            lineHeight = 44.sp
                        )
                    )
                    Text(
                        text = if (isOver) "KCAL OVER" else "KCAL REMAINING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isOver) KcalGrindOverBudgetRed else v3.emeraldAccent,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${numberFormatter.format(consumedCalories)} consumed • $progressPercent% of daily quota",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = v3.textMuted,
                        fontSize = 12.sp
                    )
                )
            }

            // Divider Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(v3.subtleBorder.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Macro Progress Triplets (Bento row at bottom of hero card)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Protein Pill
                MacroPill(
                    name = "Protein",
                    current = protein.currentGrams.roundToInt(),
                    target = protein.targetGrams.roundToInt(),
                    percent = (protein.progressFraction * 100).toInt(),
                    barColor = v3.emeraldAccent,
                    modifier = Modifier.weight(1f)
                )

                // Carbs Pill
                MacroPill(
                    name = "Carbs",
                    current = carbs.currentGrams.roundToInt(),
                    target = carbs.targetGrams.roundToInt(),
                    percent = (carbs.progressFraction * 100).toInt(),
                    barColor = if (v3.isDark) KcalGrindDarkSecondaryBar else KcalGrindLightSecondaryBar,
                    modifier = Modifier.weight(1f)
                )

                // Fats Pill
                MacroPill(
                    name = "Fats",
                    current = fat.currentGrams.roundToInt(),
                    target = fat.targetGrams.roundToInt(),
                    percent = (fat.progressFraction * 100).toInt(),
                    barColor = if (v3.isDark) KcalGrindDarkMutedBar else KcalGrindLightMutedBar,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MacroPill(
    name: String,
    current: Int,
    target: Int,
    percent: Int,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val v3 = MaterialTheme.v3Colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = v3.surfaceElevated,
        border = BorderStroke(1.dp, v3.subtleBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = v3.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = barColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }

            Text(
                text = "${current}/$target" + "g",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = v3.textPrimary,
                    fontSize = 12.sp
                )
            )

            // Mini Linear Bar
            LinearProgressIndicator(
                progress = { (percent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = barColor,
                trackColor = v3.progressTrack,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

// ============================================================================
// 3. SPROUTLING COMPANION INTERACTIVE CARD (Character-Led Coaching Card)
// ============================================================================

@Composable
private fun HomeSproutlingCard(
    sproutlingMood: SproutlingMood,
    consumedCalories: Int,
    remainingCalories: Int,
    protein: MacroProgress,
    onAskCoach: () -> Unit,
    onAddMeal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val v3 = MaterialTheme.v3Colors

    // Contextual coaching dialogue based on real Room metrics
    val coachingMessage = remember(consumedCalories, remainingCalories, protein) {
        when {
            consumedCalories == 0 -> {
                "Ready to kick off your day? Log your breakfast to start fueling up! 🌱"
            }
            remainingCalories == 0 -> {
                "Bullseye! You've reached your exact calorie target for today! 🎯"
            }
            remainingCalories < 0 -> {
                "Daily calorie quota completed! Prioritize water, minerals and rest. 💧"
            }
            protein.currentGrams < protein.targetGrams * 0.7 && remainingCalories > 300 -> {
                val neededProtein = (protein.targetGrams - protein.currentGrams).roundToInt()
                "Great pace! Focus on about ${neededProtein}g more protein in your next meal to hit your daily goal."
            }
            else -> {
                "Looking solid today! You have $remainingCalories kcal remaining in your daily budget. Keep crushing it!"
            }
        }
    }

    val sproutlingDrawable = remember(sproutlingMood) {
        when (sproutlingMood) {
            SproutlingMood.RESTING -> R.drawable.ic_sproutling_resting
            SproutlingMood.ACTIVE -> R.drawable.ic_sproutling_active
            SproutlingMood.ENERGIZED -> R.drawable.ic_sproutling_energized
            SproutlingMood.CONTENT -> R.drawable.ic_sproutling_content
            SproutlingMood.WAVING -> R.drawable.ic_sproutling_waving
            SproutlingMood.LOADING -> R.drawable.ic_sproutling_loading
            SproutlingMood.SECURE -> R.drawable.ic_sproutling_secure
        }
    }

    KcalGrindLiquidCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(16.dp),
        backgroundColor = v3.surface,
        borderColor = v3.emeraldAccent.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Mascot Container with AI Coach micro badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = if (v3.isDark) KcalGrindDeepDarkSurface else KcalGrindHighlightLightBg,
                    border = BorderStroke(1.dp, if (v3.isDark) KcalGrindDeepDarkBorder else KcalGrindDeepDarkBorderLight)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(sproutlingDrawable),
                            contentDescription = "Sproutling",
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = v3.emeraldAccent
                ) {
                    Text(
                        text = "AI COACH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = KcalGrindSurfaceCharcoal,
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }

            // Dialogue Bubble & Actions
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sproutling",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3.textPrimary,
                            fontSize = 13.sp
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(v3.emeraldAccent)
                        )
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = v3.emeraldAccent,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = coachingMessage,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = v3.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onAskCoach,
                        shape = RoundedCornerShape(9999.dp),
                        color = v3.emeraldAccent.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, v3.emeraldAccent.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = v3.emeraldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Ask AI Coach",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = v3.emeraldAccent,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Surface(
                        onClick = onAddMeal,
                        shape = RoundedCornerShape(9999.dp),
                        color = v3.surfaceElevated,
                        border = BorderStroke(1.dp, v3.subtleBorder)
                    ) {
                        Text(
                            text = "Log Fuel",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = v3.textMuted,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. QUICK WATER HYDRATION SECTION
// ============================================================================

@Composable
private fun HomeWaterHydrationCard(
    water: WaterProgress,
    onLogWater: (Int) -> Unit,
    onUndoWater: () -> Unit,
    onEditWater: (Long, Int) -> Unit,
    onDeleteWater: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val v3 = MaterialTheme.v3Colors
    var showCustomDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<com.kcalgrindai.app.domain.model.WaterLog?>(null) }
    var showHistory by remember { mutableStateOf(false) }

    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val waterBlue = KcalGrindWaterBlue

    KcalGrindLiquidCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(16.dp),
        backgroundColor = v3.surface,
        borderColor = v3.subtleBorder
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = waterBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Water Hydration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3.textPrimary,
                            fontSize = 14.sp
                        )
                    )
                }

                Text(
                    text = "${numberFormatter.format(water.consumedMl)} / ${numberFormatter.format(water.targetMl)} ml",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = waterBlue,
                        fontSize = 12.sp
                    )
                )
            }

            // Fluid Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(v3.surfaceElevated)
                        .border(1.dp, v3.subtleBorder, RoundedCornerShape(5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(water.progressFraction)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(KcalGrindWaterBlueDark, waterBlue)
                                )
                            )
                    )
                }

                // Quick Add +250 ml Pill Button
                Surface(
                    onClick = { onLogWater(250) },
                    shape = RoundedCornerShape(9999.dp),
                    color = if (v3.isDark) KcalGrindDarkWaterCardBg else KcalGrindHighlightLightBg,
                    border = BorderStroke(1.dp, v3.emeraldAccent.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "+250 ml",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3.emeraldAccent,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Extra Actions: Custom, Undo, View Logs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = { onLogWater(500) },
                        shape = RoundedCornerShape(8.dp),
                        color = v3.surfaceElevated,
                        border = BorderStroke(1.dp, v3.subtleBorder)
                    ) {
                        Text(
                            text = "+500 ml",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = v3.textSecondary,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        onClick = { showCustomDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        color = v3.surfaceElevated,
                        border = BorderStroke(1.dp, v3.subtleBorder)
                    ) {
                        Text(
                            text = "+Custom",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = v3.textSecondary,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (water.todayLogs.isNotEmpty()) {
                        Surface(
                            onClick = { showHistory = !showHistory },
                            shape = RoundedCornerShape(8.dp),
                            color = v3.surfaceElevated,
                            border = BorderStroke(1.dp, v3.subtleBorder)
                        ) {
                            Text(
                                text = if (showHistory) "Hide logs" else "Logs (${water.todayLogs.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = v3.textMuted,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (water.consumedMl > 0 && water.latestLogId != null) {
                    TextButton(
                        onClick = onUndoWater,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Undo",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = v3.textMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Expandable Per-Entry Edit / Delete List
            if (showHistory && water.todayLogs.isNotEmpty()) {
                val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    water.todayLogs.reversed().forEach { logEntry ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = v3.surfaceElevated,
                            border = BorderStroke(1.dp, v3.subtleBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${logEntry.amountMl} ml",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall.copy(color = waterBlue)
                                    )
                                    Text(
                                        text = timeFormat.format(Date(logEntry.loggedAt)),
                                        style = MaterialTheme.typography.labelSmall.copy(color = v3.textMuted)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { editingEntry = logEntry },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit entry",
                                            tint = v3.textMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteWater(logEntry.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete entry",
                                            tint = KcalGrindOverBudgetRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Edit Water Entry
    if (editingEntry != null) {
        val entry = editingEntry!!
        var amountText by remember(entry.id) { mutableStateOf(entry.amountMl.toString()) }
        AlertDialog(
            onDismissRequest = { editingEntry = null },
            title = { Text("Edit Water Entry", color = v3.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Change the logged amount for this entry:", color = v3.textSecondary)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) amountText = it },
                        label = { Text("Amount (ml)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = amountText.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            onEditWater(entry.id, parsed)
                            editingEntry = null
                        }
                    }
                ) {
                    Text("Save", color = v3.emeraldAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEntry = null }) {
                    Text("Cancel", color = v3.textMuted)
                }
            }
        )
    }

    // Dialog: Add Custom Water Entry
    if (showCustomDialog) {
        var customAmountText by remember { mutableStateOf("250") }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Log Custom Water", color = v3.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the water amount in milliliters (ml):", color = v3.textSecondary)
                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) customAmountText = it },
                        label = { Text("Amount (ml)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = customAmountText.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            onLogWater(parsed)
                            showCustomDialog = false
                        }
                    }
                ) {
                    Text("Add", color = v3.emeraldAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("Cancel", color = v3.textMuted)
                }
            }
        )
    }
}

// ============================================================================
// 5. ACTIVITY & STEPS CARD (Health Connect)
// ============================================================================

@Composable
private fun HomeActivityCard(
    activity: ActivityProgress,
    onRequestPermission: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val v3 = MaterialTheme.v3Colors

    KcalGrindLiquidCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(16.dp),
        backgroundColor = v3.surface,
        borderColor = v3.subtleBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = v3.surfaceElevated,
                    border = BorderStroke(1.dp, v3.subtleBorder),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = null,
                            tint = v3.emeraldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Steps & Activity",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3.textPrimary,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = activity.statusMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = v3.textMuted,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            when {
                activity.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = v3.emeraldAccent
                    )
                }
                activity.isConnected -> {
                    Surface(
                        onClick = onViewDetails,
                        shape = RoundedCornerShape(9999.dp),
                        color = v3.emeraldAccent.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, v3.emeraldAccent.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "View details",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = v3.emeraldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                activity.isAvailable -> {
                    Surface(
                        onClick = onRequestPermission,
                        shape = RoundedCornerShape(9999.dp),
                        color = v3.emeraldAccent
                    ) {
                        Text(
                            text = "Connect",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = KcalGrindOnPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
                else -> {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = v3.surfaceElevated,
                        border = BorderStroke(1.dp, v3.subtleBorder)
                    ) {
                        Text(
                            text = "Unavailable",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = v3.textMuted,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityDetailsDialog(
    activity: ActivityProgress,
    targetCalories: Int,
    onDismiss: () -> Unit
) {
    val v3 = MaterialTheme.v3Colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = v3.emeraldAccent,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Health Connect Activity",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = v3.textPrimary
                    )
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = v3.surfaceElevated,
                    border = BorderStroke(1.dp, v3.subtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = v3.emeraldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Connected • Google Health Connect",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = v3.emeraldAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${NumberFormat.getNumberInstance(Locale.US).format(activity.stepsToday)} steps today",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = v3.textPrimary,
                                fontSize = 22.sp
                            )
                        )
                    }
                }

                Text(
                    text = "Calorie Budget Integration",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = v3.textPrimary
                    )
                )
                Text(
                    text = "Your daily target of $targetCalories kcal is calculated from your profile and baseline activity multiplier. Routine steps sync automatically from Health Connect without double-counting your standard daily burn.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = v3.textSecondary,
                        lineHeight = 18.sp
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = v3.emeraldAccent,
                    contentColor = KcalGrindOnPrimary
                )
            ) {
                Text("Got it", fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ============================================================================
// 6. TODAY'S FUEL LOG / RECENT MEALS SUMMARY (V3 Spec)
// ============================================================================

@Composable
private fun HomeFuelLogSection(
    meals: List<MealCategorySummary>,
    totalCaloriesLogged: Int,
    onMealClick: (Long) -> Unit,
    onAddMealClick: (MealType) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val v3 = MaterialTheme.v3Colors

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "TODAY'S FUEL LOG",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = v3.textMuted,
                        letterSpacing = 0.8.sp,
                        fontSize = 11.sp
                    )
                )
                if (totalCaloriesLogged > 0) {
                    Text(
                        text = "• $totalCaloriesLogged kcal",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = v3.emeraldAccent,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onViewAll)
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = v3.emeraldAccent,
                        fontSize = 11.sp
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = v3.emeraldAccent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Meal Cards for Each Category
        meals.forEach { category ->
            val firstMeal = category.meals.firstOrNull()
            val hasMeals = category.meals.isNotEmpty()
            val icon: ImageVector = when (category.mealType) {
                MealType.BREAKFAST -> Icons.Default.WbSunny
                MealType.LUNCH -> Icons.Default.Restaurant
                MealType.DINNER -> Icons.Default.DinnerDining
                MealType.SNACK -> Icons.Default.LocalCafe
            }

            val itemsSummary = remember(category.meals) {
                val names = category.meals.flatMap { it.items }.joinToString(", ") { it.name }.trim()
                if (names.isNotBlank()) names else "Not logged yet • Tap + to log"
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (hasMeals && firstMeal != null) {
                            onMealClick(firstMeal.meal.id)
                        } else {
                            onAddMealClick(category.mealType)
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                color = v3.surface,
                border = BorderStroke(1.dp, v3.subtleBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (hasMeals) v3.emeraldAccent.copy(alpha = 0.15f) else v3.surfaceElevated,
                            border = BorderStroke(1.dp, if (hasMeals) v3.emeraldAccent.copy(alpha = 0.35f) else v3.subtleBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (hasMeals) v3.emeraldAccent else v3.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.mealType.label,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = v3.textPrimary,
                                    fontSize = 13.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = itemsSummary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (hasMeals) v3.textSecondary else v3.textMuted,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (hasMeals) {
                            Text(
                                text = "${category.totalCalories.roundToInt()} kcal",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = v3.textPrimary,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        // Add (+) Button
                        Surface(
                            onClick = { onAddMealClick(category.mealType) },
                            shape = CircleShape,
                            color = v3.surfaceElevated,
                            border = BorderStroke(1.dp, v3.subtleBorder),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add ${category.mealType.label}",
                                    tint = v3.emeraldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 7. LAUNCHER WIDGET PROMO CARD (Preserved & Styled with V3 Tokens)
// ============================================================================

@Composable
fun HomeWidgetPromoCard(
    onAddWidget: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val v3 = MaterialTheme.v3Colors

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (v3.isDark) KcalGrindDarkPromoBg else KcalGrindLightPromoBg,
        border = BorderStroke(1.dp, if (v3.isDark) KcalGrindDarkPromoBorder else KcalGrindLightPromoBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            // Dismiss button in top-right
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss banner",
                    tint = if (v3.isDark) v3.emeraldAccent.copy(alpha = 0.7f) else KcalGrindDarkForest.copy(alpha = 0.75f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Sproutling Companion Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(v3.emeraldAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = "Sproutling",
                        tint = KcalGrindOnPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bring Sproutling to your home screen",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (v3.isDark) v3.textPrimary else KcalGrindDarkForest,
                            fontSize = 16.sp,
                            lineHeight = 21.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Check your calories and track water directly from your home-screen widget without opening the app.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (v3.isDark) v3.textSecondary else KcalGrindLightDeepText,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            onClick = onAddWidget,
                            shape = RoundedCornerShape(9999.dp),
                            color = v3.emeraldAccent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Widgets,
                                    contentDescription = null,
                                    tint = KcalGrindOnPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Add widget",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KcalGrindOnPrimary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenSettings() }
                                .padding(vertical = 4.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Widget settings",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (v3.isDark) v3.emeraldAccent else KcalGrindDarkForest,
                                    fontSize = 12.sp
                                )
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = if (v3.isDark) v3.emeraldAccent else KcalGrindDarkForest,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
