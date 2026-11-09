package com.kcalgrindai.app.feature.logging

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcalgrindai.app.core.designsystem.components.CircleIconButton
import com.kcalgrindai.app.core.designsystem.v3Colors
import com.kcalgrindai.app.core.util.FoodCategoryIconHelper

data class CategoryDisplayItem(
    val category: FoodCategoryIconHelper.Category,
    val examples: String
)

@Composable
fun CategoryIconsGridScreen(
    onNavigateBack: () -> Unit = {}
) {
    val v3 = MaterialTheme.v3Colors

    val items = listOf(
        CategoryDisplayItem(FoodCategoryIconHelper.Category.DAL_PULSES, "Dal Tadka, Chana, Rajma, Sambar, Moong"),
        CategoryDisplayItem(FoodCategoryIconHelper.Category.GRAINS_BREADS, "Roti, Rice, Paratha, Oats, Biryani, Idli"),
        CategoryDisplayItem(FoodCategoryIconHelper.Category.MEAT_POULTRY, "Chicken, Fish, Mutton, Meat, Kebab"),
        CategoryDisplayItem(FoodCategoryIconHelper.Category.EGGS, "Boiled Eggs, Omelet, Egg Bhurji"),
        CategoryDisplayItem(FoodCategoryIconHelper.Category.DAIRY, "Curd, Milk, Paneer, Yogurt, Lassi, Shake"),
        CategoryDisplayItem(FoodCategoryIconHelper.Category.VEGETABLES_FRUITS, "Sabzi, Salad, Palak, Bhindi, Apple, Banana"),
        CategoryDisplayItem(FoodCategoryIconHelper.Category.OTHER, "Mixed Meals, Custom Foods, Other Dishes")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(v3.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CircleIconButton(
                onClick = onNavigateBack,
                contentDescription = "Back"
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(
                    text = "Food Category Icons",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = v3.textPrimary,
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = "Bundled offline vector drawables",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = v3.textMuted,
                        fontSize = 12.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(items) { item ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = v3.surfaceElevated,
                    border = BorderStroke(1.dp, v3.subtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = v3.emeraldAccent.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, v3.emeraldAccent.copy(alpha = 0.35f)),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = item.category.iconRes),
                                    contentDescription = item.category.displayName,
                                    tint = v3.emeraldAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.category.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = v3.textPrimary,
                                fontSize = 14.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.examples,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = v3.textMuted,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
