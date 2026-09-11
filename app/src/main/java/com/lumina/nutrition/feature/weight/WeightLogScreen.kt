package com.lumina.nutrition.feature.weight

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumina.nutrition.core.designsystem.LuminaError
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSurface
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest
import com.lumina.nutrition.core.designsystem.components.LuminaPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeightLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Log Weight",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LuminaOnSurface
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = LuminaOnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LuminaSurface
                )
            )
        },
        containerColor = LuminaSurface
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LuminaPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Date Header Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = LuminaSurfaceContainerLowest,
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "📅", fontSize = 18.sp)
                        Column {
                            Text(
                                text = "Entry Date",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = LuminaOnSurfaceVariant
                                )
                            )
                            Text(
                                text = uiState.dateFormatted,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = LuminaOnSurface
                                )
                            )
                        }
                    }
                }

                // 2. Weight Input Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = LuminaSurfaceContainerLowest,
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Current Weight",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LuminaOnSurface
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.weightInput,
                                onValueChange = { viewModel.onWeightChange(it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LuminaOnSurface
                                ),
                                placeholder = {
                                    Text(
                                        text = "0.0",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            color = LuminaOnSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LuminaPrimary,
                                    unfocusedBorderColor = LuminaOnSurfaceVariant.copy(alpha = 0.3f)
                                )
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LuminaPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = uiState.unitLabel,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LuminaPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                                )
                            }
                        }

                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage!!,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = LuminaError
                                )
                            )
                        }
                    }
                }

                // 3. Optional Note Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = LuminaSurfaceContainerLowest,
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Notes (optional)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LuminaOnSurface
                            )
                        )

                        OutlinedTextField(
                            value = uiState.note,
                            onValueChange = { viewModel.onNoteChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "e.g., Weighed after morning workout",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = LuminaOnSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                )
                            },
                            minLines = 2,
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuminaPrimary,
                                unfocusedBorderColor = LuminaOnSurfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 4. Save Button
                LuminaPrimaryButton(
                    text = if (uiState.isSaving) "Saving..." else "Save Weight",
                    onClick = {
                        viewModel.saveWeight(onSuccess = onNavigateBack)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving && uiState.weightInput.isNotBlank()
                )
            }
        }
    }
}
