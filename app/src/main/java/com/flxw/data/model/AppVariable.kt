package com.flxw.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_variables")
data class AppVariable(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)