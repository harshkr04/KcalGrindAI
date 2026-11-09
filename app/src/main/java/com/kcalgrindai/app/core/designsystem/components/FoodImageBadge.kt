package com.kcalgrindai.app.core.designsystem.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kcalgrindai.app.core.designsystem.v3Colors
import com.kcalgrindai.app.core.util.FoodCategoryIconHelper
import java.io.File

/**
 * Reusable food visual badge:
 * - Checks app-private storage for optional user-taken custom food photo.
 * - Otherwise gracefully falls back to bundled vector category icons.
 * - 100% offline-first: no network requests or external image URLs.
 */
@Composable
fun FoodImageBadge(
    foodName: String?,
    foodId: Long? = null,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    shape: Shape = RoundedCornerShape(10.dp),
    tintColor: Color? = null
) {
    val context = LocalContext.current
    val v3 = MaterialTheme.v3Colors
    val resolvedTint = tintColor ?: v3.emeraldAccent

    val localBitmap = remember(foodId) {
        if (foodId != null && foodId > 0L) {
            val file = File(context.filesDir, "food_images/custom_food_${foodId}.jpg")
            if (file.exists() && file.length() > 0) {
                try {
                    BitmapFactory.decodeFile(file.absolutePath)
                } catch (_: Exception) {
                    null
                }
            } else null
        } else null
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(v3.surfaceElevated),
        contentAlignment = Alignment.Center
    ) {
        if (localBitmap != null) {
            Image(
                bitmap = localBitmap.asImageBitmap(),
                contentDescription = foodName ?: "Food image",
                modifier = Modifier.size(size),
                contentScale = ContentScale.Crop
            )
        } else {
            val iconRes = FoodCategoryIconHelper.getCategoryIconRes(foodName)
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = foodName ?: "Food category",
                tint = resolvedTint,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}
