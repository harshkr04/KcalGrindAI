package com.kcalgrindai.app.feature.home.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kcalgrindai.app.core.designsystem.v3Colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersBottomSheet(
    onDismiss: () -> Unit,
    mealRemindersEnabled: Boolean,
    onMealRemindersChanged: (Boolean) -> Unit,
    hydrationRemindersEnabled: Boolean,
    onHydrationRemindersChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val v3 = MaterialTheme.v3Colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = v3.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = v3.emeraldAccent.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Reminders",
                        tint = v3.emeraldAccent,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Column {
                    Text(
                        text = "Reminders & Notifications",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = v3.textPrimary,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "Local device prompts to protect your streak",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = v3.textMuted,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Permission Card (Android 13+)
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Notifications are disabled on this device. Grant permission so KcalGrind can alert you at your meal and hydration times.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = v3.textPrimary,
                                fontSize = 12.sp
                            )
                        )
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            colors = ButtonDefaults.buttonColors(containerColor = v3.emeraldAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Enable Notifications", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = v3.emeraldAccent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, v3.emeraldAccent.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Allowed",
                            tint = v3.emeraldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Device notifications active",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = v3.emeraldAccent,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // Meal Reminders Switch Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = v3.surfaceElevated,
                border = BorderStroke(1.dp, v3.subtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestaurantMenu,
                            contentDescription = "Meal Reminders",
                            tint = v3.emeraldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Meal Logging Reminders",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = v3.textPrimary
                                )
                            )
                            Text(
                                text = "Prompts around breakfast, lunch, and dinner",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = v3.textMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                    Switch(
                        checked = mealRemindersEnabled,
                        onCheckedChange = onMealRemindersChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = v3.background,
                            checkedTrackColor = v3.emeraldAccent
                        )
                    )
                }
            }

            // Hydration Reminders Switch Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = v3.surfaceElevated,
                border = BorderStroke(1.dp, v3.subtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Hydration Reminders",
                            tint = v3.emeraldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Hydration Reminders",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = v3.textPrimary
                                )
                            )
                            Text(
                                text = "Gentle water reminders throughout the day (around every 2 hours)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = v3.textMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                    Switch(
                        checked = hydrationRemindersEnabled,
                        onCheckedChange = onHydrationRemindersChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = v3.background,
                            checkedTrackColor = v3.emeraldAccent
                        )
                    )
                }
            }

            // Privacy guarantee note
            Text(
                text = "Privacy Note: All reminders are scheduled strictly locally using Android WorkManager. No alarm triggers or schedules are sent to or stored on any server.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = v3.textMuted,
                    lineHeight = 16.sp
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
