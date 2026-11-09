package com.kcalgrindai.app.feature.logging.custom

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kcalgrindai.app.core.designsystem.components.KcalGrindBackButton
import com.kcalgrindai.app.core.designsystem.components.KcalGrindPrimaryButton
import com.kcalgrindai.app.core.designsystem.components.FoodImageBadge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.OutlinedButton
import android.net.Uri
import com.kcalgrindai.app.core.designsystem.components.KcalGrindTextField
import com.kcalgrindai.app.core.designsystem.v3Colors

@Composable
fun CustomFoodScreen(
    onNavigateBack: () -> Unit,
    onFoodSaved: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomFoodViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val v3 = MaterialTheme.v3Colors
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImageSelected(uri)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CustomFoodEvent.SaveSuccess -> onFoodSaved(event.foodId)
                is CustomFoodEvent.DeleteSuccess -> onNavigateBack()
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Custom Food?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This will permanently remove '${uiState.name}' from your custom foods database. Past diary entries will keep their logged nutrition.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteCustomFood()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KcalGrindBackButton(onClick = onNavigateBack)

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = if (uiState.isEditMode) "Edit Custom Food" else "New Custom Food",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (uiState.isEditMode) {
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        enabled = !uiState.isDeleting
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete custom food",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General error banner if any
            if (uiState.generalError != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.generalError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // 1. Food Details Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = v3.surface,
                border = BorderStroke(1.dp, v3.subtleBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "FOOD DETAILS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )

                    // Optional Food Photo Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FoodImageBadge(
                            foodName = uiState.name.ifBlank { "Custom Food" },
                            foodId = uiState.id,
                            size = 56.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (uiState.imageUri != null) "Change Photo" else "Add Photo (Optional)")
                            }
                            Text(
                                text = "Stored locally • EXIF & GPS stripped",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = v3.textMuted
                                )
                            )
                        }
                    }

                    KcalGrindTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = "Food Name *",
                        placeholder = "e.g., Mom's Chicken Curry",
                        errorMessage = uiState.nameError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    KcalGrindTextField(
                        value = uiState.brand,
                        onValueChange = viewModel::onBrandChange,
                        label = "Brand / Restaurant (Optional)",
                        placeholder = "e.g., Homemade or Local Diner",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }
            }

            // 2. Serving Information Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = v3.surface,
                border = BorderStroke(1.dp, v3.subtleBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "SERVING SIZE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KcalGrindTextField(
                            value = uiState.servingSize,
                            onValueChange = viewModel::onServingSizeChange,
                            label = "Serving Amount *",
                            placeholder = "1",
                            errorMessage = uiState.servingSizeError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        KcalGrindTextField(
                            value = uiState.servingUnit,
                            onValueChange = viewModel::onServingUnitChange,
                            label = "Unit *",
                            placeholder = "serving, cup, slice",
                            errorMessage = uiState.servingUnitError,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    KcalGrindTextField(
                        value = uiState.servingGrams,
                        onValueChange = viewModel::onServingGramsChange,
                        label = "Serving Weight (Grams)",
                        placeholder = "100",
                        suffixText = "g",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        )
                    )
                }
            }

            // 3. Nutrition & Macros Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = v3.surface,
                border = BorderStroke(1.dp, v3.subtleBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "MACRONUTRIENTS & CALORIES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )

                    KcalGrindTextField(
                        value = uiState.calories,
                        onValueChange = viewModel::onCaloriesChange,
                        label = "Calories (kcal) *",
                        placeholder = "0",
                        suffixText = "kcal",
                        errorMessage = uiState.caloriesError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KcalGrindTextField(
                            value = uiState.protein,
                            onValueChange = viewModel::onProteinChange,
                            label = "Protein *",
                            placeholder = "0",
                            suffixText = "g",
                            errorMessage = uiState.proteinError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        KcalGrindTextField(
                            value = uiState.carbs,
                            onValueChange = viewModel::onCarbsChange,
                            label = "Carbs *",
                            placeholder = "0",
                            suffixText = "g",
                            errorMessage = uiState.carbsError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        KcalGrindTextField(
                            value = uiState.fat,
                            onValueChange = viewModel::onFatChange,
                            label = "Fat *",
                            placeholder = "0",
                            suffixText = "g",
                            errorMessage = uiState.fatError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Macro consistency warning if deviation outside tolerance
                    if (uiState.macroWarning != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = uiState.macroWarning ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. Optional Micronutrients Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = v3.surface,
                border = BorderStroke(1.dp, v3.subtleBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "OPTIONAL DETAILS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KcalGrindTextField(
                            value = uiState.fiber,
                            onValueChange = viewModel::onFiberChange,
                            label = "Dietary Fiber",
                            placeholder = "0",
                            suffixText = "g",
                            errorMessage = uiState.fiberError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        KcalGrindTextField(
                            value = uiState.sugar,
                            onValueChange = viewModel::onSugarChange,
                            label = "Sugars",
                            placeholder = "0",
                            suffixText = "g",
                            errorMessage = uiState.sugarError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    KcalGrindTextField(
                        value = uiState.sodium,
                        onValueChange = viewModel::onSodiumChange,
                        label = "Sodium",
                        placeholder = "0",
                        suffixText = "mg",
                        errorMessage = uiState.sodiumError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            KcalGrindPrimaryButton(
                text = if (uiState.isEditMode) "Save Changes" else "Save Custom Food",
                onClick = viewModel::saveCustomFood,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
