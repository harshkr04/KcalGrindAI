package com.lumina.nutrition.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lumina.nutrition.core.designsystem.LuminaSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkeletonScreen(
    destination: LuminaRoute,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {}
) {
    Scaffold(
        bottomBar = bottomBar,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(destination.title, style = MaterialTheme.typography.titleLarge)
                        Text(destination.source, style = MaterialTheme.typography.labelSmall)
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(LuminaSpacing.containerPadding),
            verticalArrangement = Arrangement.spacedBy(LuminaSpacing.stackMd),
        ) {
            AssistChip(onClick = {}, label = { Text(destination.flow) })
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(LuminaSpacing.containerPadding),
                    verticalArrangement = Arrangement.spacedBy(LuminaSpacing.stackSm),
                ) {
                    Text("Compose skeleton", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        "Placeholder destination created from Docs/screen-inventory.md. Later phases can replace this with the production UI while keeping the route stable.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            if (destination.nextRoutes.isNotEmpty()) {
                Text("Navigate", style = MaterialTheme.typography.titleLarge)
                destination.nextRoutes.forEach { route ->
                    Button(
                        onClick = { onNavigate(route) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(route)
                    }
                }
            }

            Spacer(Modifier.height(64.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(LuminaSpacing.stackSm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(onClick = { onNavigate(LuminaRoute.Home.route) }, modifier = Modifier.weight(1f)) {
                    Text("Home")
                }
                Button(onClick = { onNavigate(LuminaRoute.Welcome.route) }, modifier = Modifier.weight(1f)) {
                    Text("Start")
                }
            }
        }
    }
}
