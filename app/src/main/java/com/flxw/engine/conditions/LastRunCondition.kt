package com.flxw.engine.conditions

import com.flxw.data.model.ConditionConfig
import com.flxw.engine.EvalContext
import com.flxw.repository.RuleRepository
import kotlinx.coroutines.runBlocking

class LastRunCondition(private val ruleRepository: RuleRepository) : ICondition {
    override fun evaluate(config: ConditionConfig, context: EvalContext): Boolean {
        val rule = runBlocking { ruleRepository.getById(context.ruleId) } ?: return true
        val minMillis = config.minMinutes * 60_000L
        return System.currentTimeMillis() - rule.lastRunAt > minMillis
    }
}