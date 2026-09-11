package com.lumina.nutrition.feature.onboarding.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumina.nutrition.core.designsystem.LuminaBackground
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSecondaryContainer
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest
import com.lumina.nutrition.core.designsystem.components.LuminaPrimaryButton
import com.lumina.nutrition.core.designsystem.components.OnboardingTopBar
import com.lumina.nutrition.core.designsystem.components.StickyBottomBar

@Composable
fun PermissionSetupScreen(
    onNavigateHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PermissionSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Launchers for system permissions
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateCameraPermission(isGranted)
    }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateMicPermission(isGranted)
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateNotificationPermission(isGranted)
    }

    // Sync initial permission statuses
    LaunchedEffect(Unit) {
        val cam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val mic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val notif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        viewModel.updateCameraPermission(cam)
        viewModel.updateMicPermission(mic)
        viewModel.updateNotificationPermission(notif)
    }

    Scaffold(
        topBar = {
            OnboardingTopBar(
                currentStep = 10,
                totalSteps = 10,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            StickyBottomBar {
                LuminaPrimaryButton(
                    text = if (uiState.isSaving) "Setting up your plan..." else "Let's Go",
                    enabled = !uiState.isSaving,
                    onClick = {
                        viewModel.completeOnboarding(onSuccess = onNavigateHome)
                    }
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
                text = "Enable permissions",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuminaOnSurface
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Allow access to unlock instant camera AI food recognition, voice logging, and meal reminders.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LuminaOnSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Camera Permission Card
            PermissionCard(
                icon = "📷",
                title = "Camera",
                description = "Scan barcodes and snap meal photos for instant AI nutrient analysis.",
                granted = uiState.cameraGranted,
                onToggle = { enable ->
                    if (enable) {
                        cameraLauncher.launch(Manifest.permission.CAMERA)
                    } else {
                        viewModel.updateCameraPermission(false)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Microphone Permission Card
            PermissionCard(
                icon = "🎙️",
                title = "Microphone",
                description = "Speak meals naturally to log complex ingredients effortlessly in seconds.",
                granted = uiState.micGranted,
                onToggle = { enable ->
                    if (enable) {
                        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        viewModel.updateMicPermission(false)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications Permission Card
            PermissionCard(
                icon = "🔔",
                title = "Reminders & Alerts",
                description = "Stay consistent with timely hydration reminders and daily summaries.",
                granted = uiState.notificationGranted,
                onToggle = { enable ->
                    if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.updateNotificationPermission(enable)
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PermissionCard(
    icon: String,
    title: String,
    description: String,
    granted: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LuminaSurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = LuminaSecondaryContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = icon, fontSize = 22.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = LuminaOnSurface
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LuminaOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = granted,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = LuminaPrimary,
                    checkedTrackColor = LuminaSecondaryContainer
                )
            )
        }
    }
}
