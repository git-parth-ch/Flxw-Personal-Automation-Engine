package com.flxw.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flxw.data.model.LogStatus
import com.flxw.ui.components.LogEntryItem
import com.flxw.ui.viewmodel.LogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(viewModel: LogViewModel = hiltViewModel()) {
    val logs by viewModel.logs.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Execution Logs") },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Delete, "Clear all logs")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // ── Filter chips ────────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = when (statusFilter) {
                    null -> 0
                    LogStatus.SUCCESS -> 1
                    LogStatus.FAILED -> 2
                    LogStatus.SKIPPED -> 3
                    LogStatus.DRY_RUN -> 4
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(selected = statusFilter == null, onClick = { viewModel.setFilter(null) }) {
                    Text("All", modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp))
                }
                Tab(selected = statusFilter == LogStatus.SUCCESS, onClick = { viewModel.setFilter(LogStatus.SUCCESS) }) {
                    Text("✓ Success", modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp))
                }
                Tab(selected = statusFilter == LogStatus.FAILED, onClick = { viewModel.setFilter(LogStatus.FAILED) }) {
                    Text("✗ Failed", modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp))
                }
                Tab(selected = statusFilter == LogStatus.SKIPPED, onClick = { viewModel.setFilter(LogStatus.SKIPPED) }) {
                    Text("— Skipped", modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp))
                }
                Tab(selected = statusFilter == LogStatus.DRY_RUN, onClick = { viewModel.setFilter(LogStatus.DRY_RUN) }) {
                    Text("Dry Run", modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp))
                }
            }

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No logs yet. Run a rule to see entries here.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(logs, key = { it.id }) { entry ->
                        LogEntryItem(entry)
                    }
                }
            }
        }
    }
}