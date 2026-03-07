package com.flxw.data.db

import androidx.room.*
import com.flxw.data.model.Rule
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {

    @Query("SELECT * FROM rules ORDER BY priority ASC")
    fun getAllRules(): Flow<List<Rule>>

    @Query("SELECT * FROM rules WHERE enabled = 1 ORDER BY priority ASC")
    fun getEnabledRules(): Flow<List<Rule>>

    @Query("SELECT * FROM rules WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Rule?

    @Upsert
    suspend fun upsert(rule: Rule)

    @Delete
    suspend fun delete(rule: Rule)

    @Query("DELETE FROM rules WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE rules SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("UPDATE rules SET lastRunAt = :time WHERE id = :id")
    suspend fun updateLastRunAt(id: String, time: Long)

    @Query("SELECT COUNT(*) FROM rules")
    suspend fun count(): Int

    @Query("SELECT * FROM rules WHERE enabled = 1 ORDER BY priority ASC")
    suspend fun getEnabledRulesSortedByPriority(): List<Rule>
}