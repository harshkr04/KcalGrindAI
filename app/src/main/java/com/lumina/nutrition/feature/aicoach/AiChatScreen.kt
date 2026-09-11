package com.lumina.nutrition.feature.aicoach

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumina.nutrition.core.designsystem.LuminaBackground
import com.lumina.nutrition.core.designsystem.LuminaOnPrimary
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainer
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerHigh
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest
import com.lumina.nutrition.domain.model.ChatMessage
import com.lumina.nutrition.domain.model.FoodLogItem
import com.lumina.nutrition.domain.model.ItemSource
import com.lumina.nutrition.domain.model.MealType
import com.lumina.nutrition.domain.model.MessageRole
import org.json.JSONObject
import kotlin.math.roundToInt

@Composable
fun AiChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuminaBackground)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onNavigateBack),
                shape = CircleShape,
                color = LuminaSurfaceContainerHigh
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "←", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LuminaOnSurface)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Lumina AI Coach ✨",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
                Text(
                    text = "Evidence-based nutrition guidance",
                    style = MaterialTheme.typography.bodySmall.copy(color = LuminaPrimary)
                )
            }
        }

        // Action Success Banner (if any)
        AnimatedVisibility(visible = uiState.actionSuccessMessage != null) {
            val msg = uiState.actionSuccessMessage
            if (msg != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { viewModel.clearActionMessage() }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.messages.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = LuminaSurfaceContainerLowest
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Hello! I'm your Lumina AI Coach.",
                                fontWeight = FontWeight.Bold,
                                color = LuminaOnSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ask me anything about your macros, meal ideas, or ask me to log meals and water for you!",
                                color = LuminaOnSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            items(uiState.messages) { message ->
                ChatBubble(
                    message = message,
                    onConfirmFoodLog = { mealType, items ->
                        viewModel.confirmFoodLog(mealType, items)
                    }
                )
            }

            if (uiState.isSending) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = LuminaSurfaceContainerHigh
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = LuminaPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Thinking...", fontSize = 13.sp, color = LuminaOnSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Field and Send Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.inputMessage,
                onValueChange = viewModel::onInputChanged,
                placeholder = { Text("Ask your AI coach anything...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Send
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSend = {
                        if (uiState.inputMessage.isNotBlank() && !uiState.isSending) {
                            viewModel.sendMessage()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuminaPrimary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.25f),
                    focusedContainerColor = LuminaSurfaceContainerLowest,
                    unfocusedContainerColor = LuminaSurfaceContainerLowest
                )
            )

            Surface(
                onClick = { viewModel.sendMessage() },
                enabled = uiState.inputMessage.isNotBlank() && !uiState.isSending,
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = if (uiState.inputMessage.isNotBlank() && !uiState.isSending) LuminaPrimary else LuminaSurfaceContainerHigh
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "➤",
                        fontSize = 18.sp,
                        color = if (uiState.inputMessage.isNotBlank() && !uiState.isSending) LuminaOnPrimary else LuminaOnSurfaceVariant
                    )
                }
            }
        }
    }
}

sealed interface ParsedActionData {
    data class ProposedFoodLog(
        val mealType: MealType,
        val items: List<FoodLogItem>,
        val totalCalories: Int
    ) : ParsedActionData

    data class LoggedWater(
        val amountMl: Int
    ) : ParsedActionData
}

private fun parseActionData(actionJson: String?): ParsedActionData? {
    if (actionJson.isNullOrBlank()) return null
    return try {
        val jsonParser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val envelope = jsonParser.decodeFromString<com.lumina.nutrition.data.remote.dto.SuggestedActionEnvelopeDto>(actionJson)
        when (envelope.type) {
            "create_food_log" -> {
                if (envelope.data != null) {
                    val foodDto = jsonParser.decodeFromJsonElement(
                        com.lumina.nutrition.data.remote.dto.ProposedFoodActionDto.serializer(),
                        envelope.data
                    )
                    val mealType = MealType.fromId(foodDto.mealType)
                    val foodItems = foodDto.items.map { it ->
                        FoodLogItem(
                            id = 0L,
                            mealLogId = 0L,
                            name = it.name,
                            servingDescription = "${it.estimatedGrams.toInt()}g",
                            servingGrams = it.estimatedGrams,
                            calories = it.calories,
                            proteinG = it.macros?.protein ?: 0.0,
                            carbsG = it.macros?.carbs ?: 0.0,
                            fatG = it.macros?.fat ?: 0.0,
                            fiberG = 0.0,
                            source = ItemSource.AI,
                            confidence = 0.95f,
                            confirmed = true
                        )
                    }
                    val totalCals = foodItems.sumOf { it.calories }.roundToInt()
                    ParsedActionData.ProposedFoodLog(mealType, foodItems, totalCals)
                } else null
            }
            "log_water" -> {
                if (envelope.data != null) {
                    val waterDto = jsonParser.decodeFromJsonElement(
                        com.lumina.nutrition.data.remote.dto.WaterLogActionDto.serializer(),
                        envelope.data
                    )
                    ParsedActionData.LoggedWater(waterDto.amountMl)
                } else null
            }
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onConfirmFoodLog: (MealType, List<FoodLogItem>) -> Unit
) {
    val isUser = message.role == MessageRole.USER
    val actionData = remember(message.structuredDataJson) {
        if (!isUser) parseActionData(message.structuredDataJson) else null
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) LuminaPrimary else LuminaSurfaceContainerLowest,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUser) LuminaOnPrimary else LuminaOnSurface,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }

        // Render Action Cards if structuredDataJson is present on assistant message
        if (actionData != null) {
            Spacer(modifier = Modifier.height(6.dp))
            when (actionData) {
                is ParsedActionData.ProposedFoodLog -> {
                    ProposedFoodCard(
                        data = actionData,
                        onConfirmFoodLog = onConfirmFoodLog
                    )
                }
                is ParsedActionData.LoggedWater -> {
                    WaterLoggedBadge(amountMl = actionData.amountMl)
                }
            }
        }
    }
}

@Composable
private fun ProposedFoodCard(
    data: ParsedActionData.ProposedFoodLog,
    onConfirmFoodLog: (MealType, List<FoodLogItem>) -> Unit
) {
    var isConfirmed by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = LuminaSurfaceContainerLowest,
        tonalElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(start = 4.dp, top = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Proposed ${data.mealType.label}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
                Text(
                    text = "${data.totalCalories} kcal",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            data.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "• ${item.name} (${item.servingGrams.toInt()}g)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LuminaOnSurfaceVariant
                        )
                    )
                    Text(
                        text = "${item.calories.toInt()} kcal",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = LuminaOnSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isConfirmed) {
                Button(
                    onClick = {
                        isConfirmed = true
                        onConfirmFoodLog(data.mealType, data.items)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Confirm & Log ${data.mealType.label}", fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Logged to Diary",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WaterLoggedBadge(amountMl: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE1F5FE),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(start = 4.dp, top = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "💧", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Auto-logged ${amountMl}ml water to daily diary",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0288D1)
                )
            )
        }
    }
}

