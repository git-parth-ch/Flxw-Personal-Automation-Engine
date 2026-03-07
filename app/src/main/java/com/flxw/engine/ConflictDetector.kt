package com.flxw.engine

import com.flxw.data.model.Rule
import com.flxw.repository.RuleRepository
import javax.inject.Inject
import javax.inject.Singleton

data class Conflict(
    val triggerType: String,
    val ruleIds: List<String>,
    val ruleNames: List<String>
)

@Singleton
class ConflictDetector @Inject constructor(
    private val ruleRepository: RuleRepository
) {
    suspend fun detect(): List<Conflict> {
        val rules = ruleRepository.getEnabledRulesSortedByPriority()
        return rules
            .groupBy { it.triggerType }
            .filter { (_, rulesWithType) -> rulesWithType.size > 1 }
            .map { (triggerType, conflictingRules) ->
                Conflict(
                    triggerType = triggerType,
                    ruleIds = conflictingRules.map { it.id },
                    ruleNames = conflictingRules.map { it.name }
                )
            }
    }
}