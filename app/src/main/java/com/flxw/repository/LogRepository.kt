package com.flxw.repository

import com.flxw.data.db.LogDao
import com.flxw.data.model.LogEntry
import com.flxw.data.model.LogStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor(
    private val logDao: LogDao
) {

    fun getAllLogs(): Flow<List<LogEntry>> = logDao.getAllLogs()

    fun getByRule(ruleId: String): Flow<List<LogEntry>> = logDao.getByRule(ruleId)

    fun getByStatus(status: LogStatus): Flow<List<LogEntry>> = logDao.getByStatus(status)

    fun getRecent(limit: Int = 50): Flow<List<LogEntry>> = logDao.getRecent(limit)

    suspend fun insert(entry: LogEntry) = logDao.insert(entry)

    suspend fun deleteByRule(ruleId: String) = logDao.deleteByRule(ruleId)

    suspend fun clearAll() = logDao.clearAll()
}