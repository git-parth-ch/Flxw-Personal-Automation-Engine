package com.flxw.data.model

import kotlinx.serialization.Serializable

// ── Trigger Types ──────────────────────────────────────────────────
enum class TriggerType { TIME, INTERVAL, MANUAL }

@Serializable
data class TriggerConfig(
    val type: TriggerType,
    val hour: Int = 9,          // For TIME trigger
    val minute: Int = 0,        // For TIME trigger
    val intervalMinutes: Int = 15 // For INTERVAL trigger
)