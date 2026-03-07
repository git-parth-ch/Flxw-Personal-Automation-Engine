package com.flxw.data.model

import kotlinx.serialization.Serializable

// ── Condition Types ────────────────────────────────────────────────
enum class ConditionType { TIME_RANGE, DAY_OF_WEEK, LAST_RUN }

@Serializable
data class ConditionConfig(
    val type: ConditionType,
    // TIME_RANGE params
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 18,
    val endMinute: Int = 0,
    // DAY_OF_WEEK params — 1=Sunday, 2=Monday, ... 7=Saturday
    val days: List<Int> = listOf(2, 3, 4, 5, 6), // Mon–Fri
    // LAST_RUN params
    val minMinutes: Int = 60
)