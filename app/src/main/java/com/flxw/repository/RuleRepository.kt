package com.flxw.repository

import com.flxw.data.db.RuleDao
import com.flxw.data.model.Rule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleRepository @Inject constructor(
    private val ruleDao: RuleDao
) {

    fun getAllRules(): Flow<List<Rule>> = ruleDao.getAllRules()

    fun getEnabledRules(): Flow<List<Rule>> = ruleDao.getEnabledRules()

    suspend fun getById(id: String): Rule? = ruleDao.getById(id)

    suspend fun upsert(rule: Rule) = ruleDao.upsert(rule)

    suspend fun delete(rule: Rule) = ruleDao.delete(rule)

    suspend fun deleteById(id: String) = ruleDao.deleteById(id)

    suspend fun setEnabled(id: String, enabled: Boolean) = ruleDao.setEnabled(id, enabled)

    suspend fun updateLastRunAt(id: String, time: Long) = ruleDao.updateLastRunAt(id, time)

    suspend fun getEnabledRulesSortedByPriority(): List<Rule> =
        ruleDao.getEnabledRulesSortedByPriority()

    suspend fun count(): Int = ruleDao.count()
}