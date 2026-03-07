package com.flxw.data.db

import androidx.room.*
import com.flxw.data.model.LogEntry
import com.flxw.data.model.LogStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries WHERE ruleId = :ruleId ORDER BY timestamp DESC")
    fun getByRule(ruleId: String): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries WHERE status = :status ORDER BY timestamp DESC")
    fun getByStatus(status: LogStatus): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<LogEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LogEntry)

    @Query("DELETE FROM log_entries WHERE ruleId = :ruleId")
    suspend fun deleteByRule(ruleId: String)

    @Query("DELETE FROM log_entries")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM log_entries")
    suspend fun count(): Int
}