package com.flxw.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class LogStatus { SUCCESS, FAILED, SKIPPED, DRY_RUN }

@Entity(tableName = "log_entries")
data class LogEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val ruleId: String,
    val ruleName: String = "",
    val status: LogStatus,
    val message: String = "",
    val durationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
