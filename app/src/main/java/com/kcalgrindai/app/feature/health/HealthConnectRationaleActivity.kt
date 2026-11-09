package com.kcalgrindai.app.feature.health

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalgrindai.app.core.designsystem.KcalGrindTheme
import com.kcalgrindai.app.core.designsystem.KcalGrindV3Colors
import com.kcalgrindai.app.core.designsystem.v3Colors

class HealthConnectRationaleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KcalGrindTheme {
                HealthConnectRationaleScreen(
                    onOpenPrivacyPolicy = {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://kcalgrind.app/privacy")
                        )
                        startActivity(browserIntent)
                    },
                    onContinue = {
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun HealthConnectRationaleScreen(
    onOpenPrivacyPolicy: () -> Unit,
    onContinue: () -> Unit
) {
    val v3 = MaterialTheme.v3Colors
    Scaffold(
        containerColor = v3.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Icon
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = v3.surfaceElevated),
                modifier = Modifier.size(72.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = v3.emeraldAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Health Connect & KcalGrind",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = v3.textPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "KcalGrind integrates with Android Health Connect to provide accurate, real-time energy balance tracking without manual logging.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = v3.textSecondary,
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Data Permissions Cards
            RationalePermissionCard(
                icon = Icons.Default.DirectionsRun,
                title = "Daily Step Count (Read)",
                description = "Used to monitor your baseline physical movement throughout the day and display step progress alongside your nutrition metrics.",
                v3 = v3
            )



            Spacer(modifier = Modifier.height(12.dp))

            RationalePermissionCard(
                icon = Icons.Default.Lock,
                title = "100% On-Device & Private",
                description = "Health data remains on your device. We do not sell, share, or monetize your health information with third-party data brokers or advertisers.",
                v3 = v3
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Actions
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = v3.emeraldAccent,
                    contentColor = Color.White
                )
            ) {
                Text("Continue to Permissions", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onOpenPrivacyPolicy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("View Privacy Policy", color = v3.textPrimary, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RationalePermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    v3: KcalGrindV3Colors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = v3.surfaceElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = v3.emeraldAccent,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = v3.textPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = v3.textSecondary,
                        lineHeight = 17.sp
                    )
                )
            }
        }
    }
}
