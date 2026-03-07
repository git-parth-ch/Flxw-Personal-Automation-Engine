package com.flxw.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(tableName = "rules")
@Serializable
data class Rule(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val enabled: Boolean = true,
    val priority: Int = 5,                         // 1 (highest) – 10 (lowest)
    val triggerType: String,                        // TriggerType.name
    val triggerParamsJson: String,                  // JSON of TriggerConfig
    val conditionsJson: String = "[]",              // JSON array of ConditionConfig
    val actionsJson: String,                        // JSON array of ActionConfig
    val createdAt: Long = System.currentTimeMillis(),
    val lastRunAt: Long = 0L
)