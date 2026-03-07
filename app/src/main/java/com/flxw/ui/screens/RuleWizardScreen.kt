package com.flxw.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flxw.data.model.*
import com.flxw.ui.viewmodel.RuleViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleWizardScreen(
    editRuleId: String?,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: RuleViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 4

    // ── State for building the rule ───────────────────────────────
    var ruleName by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(5) }
    var triggerType by remember { mutableStateOf(TriggerType.TIME) }
    var timeHour by remember { mutableStateOf(9) }
    var timeMinute by remember { mutableStateOf(0) }
    var intervalMinutes by remember { mutableStateOf(30) }
    var conditions by remember { mutableStateOf<List<ConditionConfig>>(emptyList()) }
    var actions by remember { mutableStateOf<List<ActionConfig>>(emptyList()) }

    // Pre-fill when editing
    LaunchedEffect(editRuleId) {
        if (editRuleId != null) {
            viewModel.getRule(editRuleId) { rule ->
                rule?.let {
                    ruleName = it.name
                    priority = it.priority
                    triggerType = TriggerType.valueOf(it.triggerType)
                    val json = Json { ignoreUnknownKeys = true }
                    val trigger = json.decodeFromString(TriggerConfig.serializer(), it.triggerParamsJson)
                    timeHour = trigger.hour
                    timeMinute = trigger.minute
                    intervalMinutes = trigger.intervalMinutes
                    if (it.conditionsJson.isNotBlank() && it.conditionsJson != "[]") {
                        conditions = json.decodeFromString(it.conditionsJson)
                    }
                    if (it.actionsJson.isNotBlank()) {
                        actions = json.decodeFromString(it.actionsJson)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editRuleId != null) "Edit Rule" else "New Rule — Step ${currentStep + 1}/$totalSteps") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 0) currentStep-- else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Step indicator ──────────────────────────────────────
            LinearProgressIndicator(
                progress = { (currentStep + 1) / totalSteps.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            when (currentStep) {
                // ── Step 1: Choose trigger type ─────────────────────
                0 -> {
                    Text("Choose Trigger", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    TriggerTypeCard(
                        icon = "⏰",
                        title = "Time — Daily",
                        subtitle = "Fire at a specific time every day",
                        selected = triggerType == TriggerType.TIME,
                        onClick = { triggerType = TriggerType.TIME }
                    )
                    TriggerTypeCard(
                        icon = "🔁",
                        title = "Interval — Repeating",
                        subtitle = "Fire every N minutes (minimum 15)",
                        selected = triggerType == TriggerType.INTERVAL,
                        onClick = { triggerType = TriggerType.INTERVAL }
                    )
                    TriggerTypeCard(
                        icon = "👆",
                        title = "Manual — Run Now only",
                        subtitle = "Fires only when you tap Run Now",
                        selected = triggerType == TriggerType.MANUAL,
                        onClick = { triggerType = TriggerType.MANUAL }
                    )
                }

                // ── Step 2: Configure trigger params ────────────────
                1 -> {
                    Text("Configure Trigger", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    when (triggerType) {
                        TriggerType.TIME -> {
                            Text("Fire every day at:", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = timeHour.toString(),
                                    onValueChange = { timeHour = it.toIntOrNull()?.coerceIn(0, 23) ?: timeHour },
                                    label = { Text("Hour (0–23)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = timeMinute.toString(),
                                    onValueChange = { timeMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: timeMinute },
                                    label = { Text("Minute (0–59)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Rule will fire at ${timeHour.toString().padStart(2,'0')}:${timeMinute.toString().padStart(2,'0')} daily",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        TriggerType.INTERVAL -> {
                            Text("Fire every:", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = intervalMinutes.toString(),
                                onValueChange = { intervalMinutes = it.toIntOrNull()?.coerceAtLeast(15) ?: intervalMinutes },
                                label = { Text("Minutes (minimum 15)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "⚠️ Android enforces a 15-minute minimum for background tasks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TriggerType.MANUAL -> {
                            Text(
                                "Manual trigger — no schedule needed.\nRule fires only when you tap 'Run Now'.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // ── Step 3: Add conditions ───────────────────────────
                2 -> {
                    Text("Add Conditions (optional)", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "All conditions must be true for the rule to run.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))

                    // Quick-add condition buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = {
                                if (conditions.none { it.type == ConditionType.DAY_OF_WEEK }) {
                                    conditions = conditions + ConditionConfig(type = ConditionType.DAY_OF_WEEK)
                                }
                            },
                            label = { Text("+ Weekdays Only") }
                        )
                        AssistChip(
                            onClick = {
                                if (conditions.none { it.type == ConditionType.TIME_RANGE }) {
                                    conditions = conditions + ConditionConfig(type = ConditionType.TIME_RANGE)
                                }
                            },
                            label = { Text("+ Time Range") }
                        )
                    }
                    AssistChip(
                        onClick = {
                            if (conditions.none { it.type == ConditionType.LAST_RUN }) {
                                conditions = conditions + ConditionConfig(type = ConditionType.LAST_RUN, minMinutes = 60)
                            }
                        },
                        label = { Text("+ Min Interval (no duplicate run)") }
                    )

                    Spacer(Modifier.height(16.dp))

                    // Show current conditions
                    conditions.forEach { cond ->
                        ConditionChip(condition = cond, onRemove = {
                            conditions = conditions.filter { it != cond }
                        })
                    }

                    if (conditions.isEmpty()) {
                        Text(
                            "No conditions — rule will always fire on trigger",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ── Step 4: Name, priority, actions, save ────────────
                3 -> {
                    Text("Add Actions & Save", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = ruleName,
                        onValueChange = { ruleName = it },
                        label = { Text("Rule Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))

                    Text("Priority: $priority", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = priority.toFloat(),
                        onValueChange = { priority = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "1 = highest, 10 = lowest",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("Actions", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    // Add action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = {
                                actions = actions + ActionConfig(
                                    type = ActionType.SHOW_NOTIFICATION,
                                    order = actions.size
                                )
                            },
                            label = { Text("+ Notification") }
                        )
                        AssistChip(
                            onClick = {
                                actions = actions + ActionConfig(
                                    type = ActionType.LOG_MESSAGE,
                                    order = actions.size,
                                    logMessage = "$ruleName executed"
                                )
                            },
                            label = { Text("+ Log") }
                        )
                    }
                    AssistChip(
                        onClick = {
                            actions = actions + ActionConfig(
                                type = ActionType.WEBHOOK,
                                order = actions.size
                            )
                        },
                        label = { Text("+ Webhook POST") }
                    )

                    Spacer(Modifier.height(8.dp))

                    // Show + edit current actions
                    actions.forEachIndexed { index, action ->
                        ActionEditor(
                            action = action,
                            onUpdate = { updated ->
                                actions = actions.toMutableList().apply { set(index, updated) }
                            },
                            onRemove = {
                                actions = actions.filter { it != action }
                            }
                        )
                    }

                    if (actions.isEmpty()) {
                        Text(
                            "Add at least one action",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Navigation buttons ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f)
                    ) { Text("Back") }
                }

                if (currentStep < totalSteps - 1) {
                    Button(
                        onClick = { currentStep++ },
                        modifier = Modifier.weight(1f)
                    ) { Text("Next →") }
                } else {
                    val canSave = ruleName.isNotBlank() && actions.isNotEmpty()
                    Button(
                        onClick = {
                            if (canSave) {
                                val json = Json
                                val triggerConfig = TriggerConfig(
                                    type = triggerType,
                                    hour = timeHour,
                                    minute = timeMinute,
                                    intervalMinutes = intervalMinutes
                                )
                                val rule = Rule(
                                    id = editRuleId ?: java.util.UUID.randomUUID().toString(),
                                    name = ruleName,
                                    priority = priority,
                                    triggerType = triggerType.name,
                                    triggerParamsJson = json.encodeToString(TriggerConfig.serializer(), triggerConfig),
                                    conditionsJson = json.encodeToString(conditions),
                                    actionsJson = json.encodeToString(actions)
                                )
                                viewModel.saveRule(rule)
                                onSaved()
                            }
                        },
                        enabled = canSave,
                        modifier = Modifier.weight(1f)
                    ) { Text("💾 Save Rule") }
                }
            }
        }
    }
}

// ── Small helper composables ────────────────────────────────────────

@Composable
private fun TriggerTypeCard(
    icon: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        border = if (selected) CardDefaults.outlinedCardBorder() else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selected) {
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ConditionChip(condition: ConditionConfig, onRemove: () -> Unit) {
    val label = when (condition.type) {
        ConditionType.TIME_RANGE -> "Time: ${condition.startHour}:00–${condition.endHour}:00"
        ConditionType.DAY_OF_WEEK -> "Days: Mon–Fri only"
        ConditionType.LAST_RUN -> "Min interval: ${condition.minMinutes}min"
    }
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun ActionEditor(
    action: ActionConfig,
    onUpdate: (ActionConfig) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(action.type.name, style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "Remove action", modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            when (action.type) {
                ActionType.SHOW_NOTIFICATION -> {
                    OutlinedTextField(
                        value = action.notifTitle,
                        onValueChange = { onUpdate(action.copy(notifTitle = it)) },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = action.notifBody,
                        onValueChange = { onUpdate(action.copy(notifBody = it)) },
                        label = { Text("Body message") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                ActionType.LOG_MESSAGE -> {
                    OutlinedTextField(
                        value = action.logMessage,
                        onValueChange = { onUpdate(action.copy(logMessage = it)) },
                        label = { Text("Log message") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                ActionType.WEBHOOK -> {
                    OutlinedTextField(
                        value = action.webhookUrl,
                        onValueChange = { onUpdate(action.copy(webhookUrl = it)) },
                        label = { Text("Webhook URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = action.webhookPayload,
                        onValueChange = { onUpdate(action.copy(webhookPayload = it)) },
                        label = { Text("JSON Payload") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}