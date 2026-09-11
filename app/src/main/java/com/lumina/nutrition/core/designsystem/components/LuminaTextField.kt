package com.lumina.nutrition.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumina.nutrition.core.designsystem.LuminaError
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaOutline
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLow

@Composable
fun LuminaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    errorMessage: String? = null,
    suffixText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    val isError = errorMessage != null

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                color = LuminaOnSurface
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            singleLine = singleLine,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            placeholder = placeholder?.let {
                { Text(it, color = LuminaOnSurfaceVariant.copy(alpha = 0.5f)) }
            },
            suffix = suffixText?.let {
                { Text(it, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = LuminaPrimary)) }
            },
            keyboardOptions = keyboardOptions,
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = LuminaOnSurface
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LuminaSurfaceContainerLow,
                unfocusedContainerColor = LuminaSurfaceContainerLow,
                disabledContainerColor = LuminaSurfaceContainerLow.copy(alpha = 0.5f),
                focusedBorderColor = LuminaPrimary,
                unfocusedBorderColor = LuminaOutline.copy(alpha = 0.3f),
                errorBorderColor = LuminaError
            )
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall.copy(color = LuminaError),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
