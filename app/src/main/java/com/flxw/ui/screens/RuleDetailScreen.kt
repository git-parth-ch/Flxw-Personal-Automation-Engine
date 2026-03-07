package com.flxw.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flxw.data.model.*
import com.flxw.ui.viewmodel.LogViewModel
import com.flxw.ui.viewmodel.RuleViewModel
import com.flxw.ui.components.LogEntryItem
import kotlinx.coroutines.flow.take
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleDetailScreen(
    ruleId: String,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    ruleViewModel: RuleViewModel = hiltViewModel(),
    logViewModel: LogViewModel = hiltViewModel()
) {
    var rule by remember { mutableStateOf<Rule?>(null) }
    val ruleLogs by logViewModel.getLogsForRule(ruleId)
        .collectAsState(initial = emptyList())

    LaunchedEffect(ruleId) {
        ruleViewModel.getRule(ruleId) { rule = it }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by ruleViewModel.snackbarMessage.collectAsState(initial = "")
    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.isNotBlank()) snackbarHostState.showSnackbar(snackbarMessage)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(rule?.name ?: "Rule Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit rule")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        rule?.let { r ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // ── Status + Priority ───────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(if (r.enabled) "Enabled" else "Disabled") },
                        leadingIcon = {
                            Icon(
                                if (r.enabled) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                null,
                                Modifier.size(16.dp)
                            )
                        }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("Priority ${r.priority}") }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Trigger section ─────────────────────────────────────
                SectionHeader("TRIGGER")
                InfoCard(
                    label = r.triggerType,
                    detail = r.triggerParamsJson
                )

                Spacer(Modifier.height(12.dp))

                // ── Conditions ──────────────────────────────────────────
                SectionHeader("CONDITIONS")
                if (r.conditionsJson == "[]" || r.conditionsJson.isBlank()) {
                    Text(
                        "No conditions — rule always fires",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(r.conditionsJson, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(12.dp))

                // ── Actions ─────────────────────────────────────────────
                SectionHeader("ACTIONS")
                Text(r.actionsJson, style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(24.dp))

                // ── Run buttons ─────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { ruleViewModel.runNow(ruleId) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Run Now")
                    }
                    OutlinedButton(
                        onClick = { ruleViewModel.testRun(ruleId) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Science, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Test Run")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Recent logs ─────────────────────────────────────────
                SectionHeader("RECENT LOGS")
                if (ruleLogs.isEmpty()) {
                    Text(
                        "No logs yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    ruleLogs.take(10).forEach { entry ->
                        LogEntryItem(entry)
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun InfoCard(label: String, detail: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall)
            Text(detail, style = MaterialTheme.typography.bodySmall)
        }
    }
}