package com.flxw.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flxw.data.model.LogEntry
import com.flxw.data.model.LogStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogEntryItem(entry: LogEntry, modifier: Modifier = Modifier) {
    val dateFormat = remember { SimpleDateFormat("MMM dd HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status chip
            StatusChip(entry.status)
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.ruleName.ifBlank { entry.ruleId.take(8) },
                    style = MaterialTheme.typography.labelMedium
                )
                if (entry.message.isNotBlank()) {
                    Text(
                        text = entry.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = dateFormat.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${entry.durationMs}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: LogStatus) {
    val (color, label) = when (status) {
        LogStatus.SUCCESS -> Pair(Color(0xFF4CAF50), "✓")
        LogStatus.FAILED -> Pair(MaterialTheme.colorScheme.error, "✗")
        LogStatus.SKIPPED -> Pair(Color(0xFFFF9800), "—")
        LogStatus.DRY_RUN -> Pair(Color(0xFF2196F3), "DRY")
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}