package com.flxw.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flxw.data.model.AppVariable
import com.flxw.data.model.LogEntry
import com.flxw.data.model.Rule

@Database(
    entities = [Rule::class, LogEntry::class, AppVariable::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun logDao(): LogDao
    abstract fun variableDao(): VariableDao

    companion object {
        const val DATABASE_NAME = "flxw_db"
    }
}