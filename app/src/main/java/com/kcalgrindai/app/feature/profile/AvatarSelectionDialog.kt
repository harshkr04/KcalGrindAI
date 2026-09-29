package com.kcalgrindai.app.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class AvatarColorOption(
    val id: String,
    val name: String,
    val color: Color
)

@Composable
fun AvatarSelectionDialog(
    currentAvatar: String?,
    displayName: String,
    onDismiss: () -> Unit,
    onAvatarSelected: (String?) -> Unit
) {
    val colorOptions = remember {
        listOf(
            AvatarColorOption("default", "Brand Green", Color(0xFF16A34A)),
            AvatarColorOption("color:#4F46E5", "Indigo", Color(0xFF4F46E5)),
            AvatarColorOption("color:#0D9488", "Teal", Color(0xFF0D9488)),
            AvatarColorOption("color:#0284C7", "Sky Blue", Color(0xFF0284C7)),
            AvatarColorOption("color:#7C3AED", "Violet", Color(0xFF7C3AED)),
            AvatarColorOption("color:#E11D48", "Rose", Color(0xFFE11D48)),
            AvatarColorOption("color:#EA580C", "Amber", Color(0xFFEA580C)),
            AvatarColorOption("color:#475569", "Slate", Color(0xFF475569))
        )
    }

    var selectedOptionId by remember {
        mutableStateOf(
            if (currentAvatar.isNullOrBlank() || !currentAvatar.startsWith("color:")) "default"
            else currentAvatar
        )
    }

    val activeColor = remember(selectedOptionId) {
        colorOptions.firstOrNull { it.id == selectedOptionId }?.color ?: Color(0xFF16A34A)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Profile Avatar",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Select a color theme for your profile avatar.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Preview Box
                Surface(
                    modifier = Modifier.size(76.dp),
                    shape = CircleShape,
                    color = activeColor.copy(alpha = 0.18f),
                    border = BorderStroke(2.5.dp, activeColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = displayName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = activeColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Grid of avatar color palette options
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(130.dp)
                ) {
                    items(colorOptions) { option ->
                        val isSelected = selectedOptionId == option.id
                        Surface(
                            shape = CircleShape,
                            color = option.color,
                            border = if (isSelected) {
                                BorderStroke(3.dp, MaterialTheme.colorScheme.surface)
                            } else null,
                            shadowElevation = if (isSelected) 4.dp else 1.dp,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .clickable { selectedOptionId = option.id }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(
                        onClick = {
                            val result = if (selectedOptionId == "default") null else selectedOptionId
                            onAvatarSelected(result)
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
