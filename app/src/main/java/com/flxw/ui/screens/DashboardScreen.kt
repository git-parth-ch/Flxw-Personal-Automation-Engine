package com.flxw.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flxw.data.model.LogStatus
import com.flxw.ui.components.LogEntryItem
import com.flxw.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onViewLogs: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val rules by viewModel.allRules.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()

    val enabledCount = rules.count { it.enabled }
    val successCount = recentLogs.count { it.status == LogStatus.SUCCESS }
    val failedCount = recentLogs.count { it.status == LogStatus.FAILED }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Stats row ───────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Total Rules", rules.size.toString(), Modifier.weight(1f))
                    StatCard("Active", enabledCount.toString(), Modifier.weight(1f))
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("✓ Success (recent)", successCount.toString(), Modifier.weight(1f))
                    StatCard("✗ Failed (recent)", failedCount.toString(), Modifier.weight(1f))
                }
            }

            // ── Recent activity ─────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Activity", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onViewLogs) { Text("View All →") }
                }
            }

            if (recentLogs.isEmpty()) {
                item {
                    Text(
                        "No activity yet. Create a rule and run it!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(recentLogs.take(10), key = { it.id }) { entry ->
                    LogEntryItem(entry)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}