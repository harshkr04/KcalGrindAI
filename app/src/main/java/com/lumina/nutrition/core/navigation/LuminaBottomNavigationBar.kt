package com.lumina.nutrition.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSurface
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainer

enum class BottomNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME("home", "Home", Icons.Default.Home),
    DIARY("diary", "Diary", Icons.Default.DateRange),
    INSIGHTS("insights", "Insights", Icons.Default.Star),
    PROFILE("profile", "Profile", Icons.Default.Person)
}

@Composable
fun LuminaBottomNavigationBar(
    currentRoute: String,
    onNavigateToTab: (String) -> Unit,
    onOpenAddFood: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bottom Navigation Surface Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(elevation = 12.dp),
            color = LuminaSurface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Home
                NavTabItem(
                    tab = BottomNavTab.HOME,
                    isSelected = currentRoute == LuminaRoute.Home.route,
                    onClick = { onNavigateToTab(LuminaRoute.Home.route) }
                )

                // Tab 2: Diary
                NavTabItem(
                    tab = BottomNavTab.DIARY,
                    isSelected = currentRoute == LuminaRoute.Diary.route,
                    onClick = { onNavigateToTab(LuminaRoute.Diary.route) }
                )

                // Spacer for Center Add Action
                Spacer(modifier = Modifier.size(56.dp))

                // Tab 3: Insights
                NavTabItem(
                    tab = BottomNavTab.INSIGHTS,
                    isSelected = currentRoute == LuminaRoute.Insights.route,
                    onClick = { onNavigateToTab(LuminaRoute.Insights.route) }
                )

                // Tab 4: Profile
                NavTabItem(
                    tab = BottomNavTab.PROFILE,
                    isSelected = currentRoute == LuminaRoute.Profile.route,
                    onClick = { onNavigateToTab(LuminaRoute.Profile.route) }
                )
            }
        }

        // Central Floating Action Button (Unclipped, perfectly circular)
        Surface(
            onClick = onOpenAddFood,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
                .size(56.dp),
            shape = CircleShape,
            color = LuminaPrimary,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Food",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    tab: BottomNavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = if (isSelected) LuminaPrimary else LuminaOnSurfaceVariant
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Column(
        modifier = modifier
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = tab.label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = fontWeight
        )
    }
}
