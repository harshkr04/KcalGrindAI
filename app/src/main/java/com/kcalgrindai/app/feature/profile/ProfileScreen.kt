package com.kcalgrindai.app.feature.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.luminance
import androidx.hilt.navigation.compose.hiltViewModel
import com.kcalgrindai.app.core.designsystem.components.KcalGrindBackButton
import com.kcalgrindai.app.domain.model.ActivityLevel
import com.kcalgrindai.app.domain.model.GoalType
import com.kcalgrindai.app.domain.model.UnitSystem
import com.kcalgrindai.app.domain.usecase.NutritionCalculator
import com.kcalgrindai.app.core.designsystem.LogoutIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTab: (String) -> Unit = {},
    onNavigateToAddFood: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val hasHealthConnectPermission by viewModel.hasHealthConnectPermission.collectAsState()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.checkHealthConnectPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = viewModel.createPermissionRequestContract()
    ) {
        viewModel.checkHealthConnectPermission()
    }

    var showEditMetricsDialog by remember { mutableStateOf(false) }
    var showPersonalInfoDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                KcalGrindBackButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is ProfileUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No profile found. Please complete onboarding.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is ProfileUiState.Content -> {
                val profile = state.profile
                val isMetric = profile.units == UnitSystem.METRIC

                // Display name prioritizes firstName, then email prefix, with honest fallback
                val displayName = remember(profile.firstName, profile.email) {
                    profile.firstName?.takeIf { it.isNotBlank() }
                        ?: profile.email?.substringBefore("@")
                            ?.split(".", "_", "-")
                            ?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                            ?.takeIf { it.isNotBlank() }
                        ?: "User"
                }

                // Subtitle email with honest fallback
                val userEmail = profile.email?.takeIf { it.isNotBlank() } ?: "Guest Account (Local Only)"

                val avatarColor = remember(state.avatarUrl) {
                    if (state.avatarUrl?.startsWith("color:") == true) {
                        try {
                            Color(android.graphics.Color.parseColor(state.avatarUrl.removePrefix("color:")))
                        } catch (_: Exception) {
                            null
                        }
                    } else null
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Avatar with Edit Badge (Exclusively edits avatar theme)
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.size(82.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = avatarColor ?: MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = displayName.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (avatarColor != null) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // Emerald Green Edit Badge Button (Exclusively edits avatar)
                        Surface(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .clickable { showAvatarDialog = true },
                            shape = CircleShape,
                            color = Color(0xFF10B981),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Change Profile Picture",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // User Name & PRO Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 21.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f) else Color(0xFFEEF2FF),
                            border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f) else Color(0xFFC7D2FE))
                        ) {
                            Text(
                                text = "PRO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) MaterialTheme.colorScheme.tertiary else Color(0xFF4F46E5),
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Subtitle Email
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // 1. HEALTH PROFILE SECTION
                    ProfileSectionCard {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SectionIconBox(
                                    icon = Icons.Default.Person,
                                    iconTint = Color(0xFF4F46E5),
                                    backgroundColor = Color(0xFFEEF2FF)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Health Profile",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp
                                    )
                                )
                            }

                            // Edit Button
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showEditMetricsDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0))
                            ) {
                                Text(
                                    text = "Edit",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2x2 Metric Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HealthMetricBox(
                                label = "HEIGHT",
                                value = if (isMetric) {
                                    "${profile.heightCm.toInt()} cm"
                                } else {
                                    val (ft, inch) = NutritionCalculator.cmToFtIn(profile.heightCm)
                                    "$ft'$inch\""
                                },
                                modifier = Modifier.weight(1f)
                            )
                            HealthMetricBox(
                                label = "WEIGHT",
                                value = if (isMetric) {
                                    String.format(Locale.US, "%.1f kg", profile.weightKg)
                                } else {
                                    "${NutritionCalculator.kgToLb(profile.weightKg)} lb"
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HealthMetricBox(
                                label = "AGE",
                                value = "${profile.age} yrs",
                                modifier = Modifier.weight(1f)
                            )
                            HealthMetricBox(
                                label = "ACTIVITY",
                                value = profile.activityLevel.label,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Goal & Weight Progress Card (Uses actual Room entity data)
                        GoalWeightProgressCard(
                            goal = profile.goal,
                            currentWeightKg = profile.weightKg,
                            goalWeightKg = profile.goalWeightKg,
                            targetCalories = state.goal?.calories ?: 2000,
                            isMetric = isMetric
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. ACCOUNT SETTINGS SECTION
                    ProfileSectionCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionIconBox(
                                icon = Icons.Default.Settings,
                                iconTint = Color(0xFF0284C7),
                                backgroundColor = Color(0xFFE0F2FE)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Account Settings",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        ProfileNavigationRow(
                            title = "Personal Information",
                            onClick = { showPersonalInfoDialog = true }
                        )

                        CardDivider()

                        ProfileNavigationRow(
                            title = "Security & Privacy",
                            onClick = { showSecurityDialog = true }
                        )

                        CardDivider()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable { onNavigateToSubscription() }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Subscription",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val isPro = state.isPro
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isPro) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (isPro) "Pro • Active" else "Free Plan • Upgrade",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isPro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. PREFERENCES SECTION
                    ProfileSectionCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionIconBox(
                                icon = Icons.Default.Tune,
                                iconTint = Color(0xFF059669),
                                backgroundColor = Color(0xFFECFDF5)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Preferences",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Units Row (Persisted to Room user_profile)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Units",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                )
                                Text(
                                    text = if (isMetric) "Metric (kg, cm, ml)" else "Imperial (lb, ft, oz)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.toggleUnitSystem() },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "Change",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                                )
                            }
                        }

                        CardDivider()

                        // Notifications Row (Persisted to UserPreferencesRepository)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notifications",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                )
                                Text(
                                    text = "Meal reminders, Insights",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }

                        CardDivider()

                        // Theme Selection (Persisted to UserPreferencesRepository: SYSTEM, LIGHT, DARK)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Theme",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                )
                                Text(
                                    text = when (themeMode) {
                                        com.kcalgrindai.app.domain.model.ThemeMode.SYSTEM -> "System default"
                                        com.kcalgrindai.app.domain.model.ThemeMode.LIGHT -> "Light Mode"
                                        com.kcalgrindai.app.domain.model.ThemeMode.DARK -> "Dark Mode"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    com.kcalgrindai.app.domain.model.ThemeMode.SYSTEM to "System",
                                    com.kcalgrindai.app.domain.model.ThemeMode.LIGHT to "Light",
                                    com.kcalgrindai.app.domain.model.ThemeMode.DARK to "Dark"
                                ).forEach { (mode, label) ->
                                    val isSelected = themeMode == mode
                                    Surface(
                                        onClick = { viewModel.setThemeMode(mode) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 12.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. INTEGRATIONS SECTION
                    ProfileSectionCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionIconBox(
                                icon = Icons.Default.Sync,
                                iconTint = Color(0xFFD97706),
                                backgroundColor = Color(0xFFFFFBEB)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Integrations",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Health Connect Row (Real integration via HealthConnectManager)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Health Connect",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                )
                                Text(
                                    text = "Sync daily step count",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            if (hasHealthConnectPermission) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDark) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color(0xFFECFDF5),
                                    border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color(0xFFA7F3D0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF047857),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Connected",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF047857),
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            permissionLauncher.launch(viewModel.requiredHealthConnectPermissions)
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "Connect",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 12.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        CardDivider()

                        // Cloud Sync Row (Honest status with no crashes or fake toasts)
                        val lastSync = viewModel.lastSyncTimestamp
                        val isSyncing = syncState is SyncState.Syncing
                        val lastSyncText = when {
                            isSyncing -> "Syncing data to cloud…"
                            lastSync > 0L -> {
                                val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                                "Last synced: ${fmt.format(Date(lastSync))}"
                            }
                            else -> "Local only • Tap to backup"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cloud Sync",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                )
                                Text(
                                    text = lastSyncText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSyncing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSyncing) FontWeight.Medium else FontWeight.Normal
                                    )
                                )
                            }

                            val syncButtonBg = if (isSyncing) {
                                if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else Color(0xFFE2E8F0)
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isSyncing) { viewModel.onSyncNow() },
                                shape = RoundedCornerShape(12.dp),
                                color = syncButtonBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Syncing…",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 12.sp
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = "Sync Now",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Sync Notification Result Banner
                        when (val s = syncState) {
                            is SyncState.Success -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                val successBg = if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color(0xFFECFDF5)
                                val successBorder = if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else Color(0xFFA7F3D0)
                                val successText = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF047857)
                                val successMsgText = if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF065F46)
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = successBg,
                                    border = BorderStroke(1.dp, successBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = successText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Sync complete. All food logs & targets updated.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = successMsgText,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                            is SyncState.Error -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.dismissSyncResult() },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = s.message,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                            else -> Unit
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    // 5. SIGN OUT BUTTON (Red Pill)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.signOut {
                                    onNavigateToAuth()
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = LogoutIcon,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign Out",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }

                // DIALOG: Comprehensive Health Profile Edit
                if (showEditMetricsDialog) {
                    var nameInput by remember { mutableStateOf(profile.firstName ?: "") }
                    var heightInput by remember {
                        mutableStateOf(
                            if (isMetric) profile.heightCm.roundToInt().toString()
                            else {
                                val (ft, inch) = NutritionCalculator.cmToFtIn(profile.heightCm)
                                "$ft.$inch"
                            }
                        )
                    }
                    var weightInput by remember {
                        mutableStateOf(
                            if (isMetric) String.format(Locale.US, "%.1f", profile.weightKg)
                            else NutritionCalculator.kgToLb(profile.weightKg).toString()
                        )
                    }
                    var ageInput by remember { mutableStateOf(profile.age.toString()) }
                    var selectedActivity by remember { mutableStateOf(profile.activityLevel) }
                    var selectedGoal by remember { mutableStateOf(profile.goal) }
                    var goalWeightInput by remember {
                        mutableStateOf(
                            if (isMetric) String.format(Locale.US, "%.1f", profile.goalWeightKg)
                            else NutritionCalculator.kgToLb(profile.goalWeightKg).toString()
                        )
                    }

                    AlertDialog(
                        onDismissRequest = { showEditMetricsDialog = false },
                        title = {
                            Text(
                                text = "Edit Health Profile",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Update your body metrics and daily routine. Daily calories and macro targets will recalculate automatically.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    label = { Text("First Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = heightInput,
                                        onValueChange = { heightInput = it },
                                        label = { Text(if (isMetric) "Height (cm)" else "Height (ft.in)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = weightInput,
                                        onValueChange = { weightInput = it },
                                        label = { Text(if (isMetric) "Weight (kg)" else "Weight (lb)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = ageInput,
                                        onValueChange = { ageInput = it },
                                        label = { Text("Age (yrs)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = goalWeightInput,
                                        onValueChange = { goalWeightInput = it },
                                        label = { Text(if (isMetric) "Target (kg)" else "Target (lb)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Text(
                                    text = "Activity Level",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    ActivityLevel.entries.forEach { act ->
                                        FilterChip(
                                            selected = selectedActivity == act,
                                            onClick = { selectedActivity = act },
                                            label = { Text(act.label, fontSize = 12.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                selectedLabelColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = "Goal",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    GoalType.entries.forEach { g ->
                                        FilterChip(
                                            selected = selectedGoal == g,
                                            onClick = { selectedGoal = g },
                                            label = { Text(g.label, fontSize = 12.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                selectedLabelColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val parsedWeight = weightInput.toDoubleOrNull() ?: profile.weightKg
                                    val finalWeightKg = if (isMetric) parsedWeight else NutritionCalculator.lbToKg(parsedWeight)

                                    val parsedHeight = heightInput.toDoubleOrNull() ?: profile.heightCm
                                    val finalHeightCm = if (isMetric) parsedHeight else {
                                        val parts = heightInput.split(".")
                                        val ft = parts.getOrNull(0)?.toIntOrNull() ?: 5
                                        val inch = parts.getOrNull(1)?.toIntOrNull() ?: 9
                                        NutritionCalculator.ftInToCm(ft, inch)
                                    }

                                    val finalAge = ageInput.toIntOrNull() ?: profile.age

                                    val parsedGoalWeight = goalWeightInput.toDoubleOrNull() ?: profile.goalWeightKg
                                    val finalGoalWeightKg = if (isMetric) parsedGoalWeight else NutritionCalculator.lbToKg(parsedGoalWeight)

                                    viewModel.updateHealthProfile(
                                        firstName = nameInput,
                                        heightCm = finalHeightCm,
                                        weightKg = finalWeightKg,
                                        age = finalAge,
                                        activityLevel = selectedActivity,
                                        goal = selectedGoal,
                                        goalWeightKg = finalGoalWeightKg
                                    )
                                    showEditMetricsDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditMetricsDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // DIALOG: Personal Information (Displays & allows editing First Name)
                if (showPersonalInfoDialog) {
                    val isGuest = profile.email.isNullOrBlank()
                    var nameEdit by remember { mutableStateOf(profile.firstName ?: "") }

                    AlertDialog(
                        onDismissRequest = { showPersonalInfoDialog = false },
                        title = {
                            Text(
                                text = "Personal Information",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = nameEdit,
                                    onValueChange = { nameEdit = it },
                                    label = { Text("First Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (isGuest) {
                                    Text(
                                        text = "Guest Account",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Text(
                                        text = "Your nutrition logs are currently stored locally on this device. Link an account to enable cloud backup across devices.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                } else {
                                    Text(
                                        text = "Email Address",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = profile.email ?: "",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                if (!profile.firebaseUid.isNullOrBlank()) {
                                    val uidDisplay = if (profile.firebaseUid.length > 14) {
                                        "${profile.firebaseUid.take(14)}…"
                                    } else {
                                        profile.firebaseUid
                                    }
                                    Text(
                                        text = "Account ID: $uidDisplay",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            if (isGuest) {
                                Button(
                                    onClick = {
                                        if (nameEdit != (profile.firstName ?: "")) {
                                            viewModel.updateFirstName(nameEdit)
                                        }
                                        showPersonalInfoDialog = false
                                        onNavigateToAuth()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Link Account")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (nameEdit != (profile.firstName ?: "")) {
                                            viewModel.updateFirstName(nameEdit)
                                        }
                                        showPersonalInfoDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Save")
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                if (isGuest && nameEdit != (profile.firstName ?: "")) {
                                    viewModel.updateFirstName(nameEdit)
                                }
                                showPersonalInfoDialog = false
                            }) {
                                Text(if (isGuest) "Save & Close" else "Cancel")
                            }
                        }
                    )
                }

                // DIALOG: Security & Privacy
                if (showSecurityDialog) {
                    AlertDialog(
                        onDismissRequest = { showSecurityDialog = false },
                        title = {
                            Text(
                                text = "Security & Privacy",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Data Encryption",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "All personal health data and nutritional logs are encrypted locally with AES-256 and synchronized over TLS 1.3.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Privacy Commitment",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Your food entries and dietary habits are never sold to third parties or used for external advertising.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showSecurityDialog = false }) {
                                Text("Close")
                            }
                        }
                    )
                }

                // DIALOG: Avatar Selection (Dedicated Profile Picture Picker)
                if (showAvatarDialog) {
                    AvatarSelectionDialog(
                        currentAvatar = state.avatarUrl,
                        displayName = displayName,
                        onDismiss = { showAvatarDialog = false },
                        onAvatarSelected = { newAvatar ->
                            viewModel.updateAvatarUrl(newAvatar)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SectionIconBox(
    icon: ImageVector,
    iconTint: Color,
    backgroundColor: Color
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        modifier = Modifier.size(32.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else backgroundColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun GoalWeightProgressCard(
    goal: GoalType,
    currentWeightKg: Double,
    goalWeightKg: Double?,
    targetCalories: Int,
    isMetric: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val currentFormatted = if (isMetric) String.format(Locale.US, "%.1f kg", currentWeightKg) else "${NutritionCalculator.kgToLb(currentWeightKg)} lb"
    val goalFormatted = if (goalWeightKg != null && goalWeightKg > 0.0) {
        if (isMetric) String.format(Locale.US, "%.1f kg", goalWeightKg) else "${NutritionCalculator.kgToLb(goalWeightKg)} lb"
    } else null

    val diffKg = if (goalWeightKg != null && goalWeightKg > 0.0) currentWeightKg - goalWeightKg else null
    val diffFormatted = if (diffKg != null) {
        val absDiff = kotlin.math.abs(diffKg)
        if (absDiff < 0.2) "Goal reached"
        else if (isMetric) String.format(Locale.US, "%.1f kg to target", absDiff)
        else "${NutritionCalculator.kgToLb(absDiff)} lb to target"
    } else null

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Goal: ${goal.label}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ) {
                    Text(
                        text = "$targetCalories kcal/day",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (goalFormatted != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CURRENT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = currentFormatted,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    if (diffFormatted != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (diffKg != null && kotlin.math.abs(diffKg) < 0.2) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = diffFormatted,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (diffKg != null && kotlin.math.abs(diffKg) < 0.2) Color(0xFF047857) else MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TARGET",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = goalFormatted,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthMetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp
                )
            )
        }
    }
}

@Composable
private fun ProfileNavigationRow(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun CardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 2.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    )
}
