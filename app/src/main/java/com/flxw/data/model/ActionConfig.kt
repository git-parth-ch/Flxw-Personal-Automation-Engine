package com.flxw.data.model

import kotlinx.serialization.Serializable

// ── Action Types ───────────────────────────────────────────────────
enum class ActionType { SHOW_NOTIFICATION, LOG_MESSAGE, WEBHOOK }

@Serializable
data class ActionConfig(
    val type: ActionType,
    val order: Int = 0,
    // SHOW_NOTIFICATION params
    val notifTitle: String = "Automation Alert",
    val notifBody: String = "Your rule fired!",
    // LOG_MESSAGE params
    val logMessage: String = "Rule executed",
    // WEBHOOK params
    val webhookUrl: String = "",
    val webhookPayload: String = "{\"source\":\"automation-engine\"}"
)