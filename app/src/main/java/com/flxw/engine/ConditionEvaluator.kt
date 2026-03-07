package com.flxw.engine

import com.flxw.data.model.ConditionConfig
import com.flxw.data.model.ConditionType
import com.flxw.engine.conditions.DayOfWeekCondition
import com.flxw.engine.conditions.LastRunCondition
import com.flxw.engine.conditions.TimeRangeCondition
import com.flxw.repository.RuleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConditionEvaluator @Inject constructor(
    private val ruleRepository: RuleRepository
) {
    /**
     * Returns true only if ALL conditions pass (AND logic with short-circuit).
     * Returns the name of the first failing condition, or null if all pass.
     */
    fun allPass(conditions: List<ConditionConfig>, context: EvalContext): Pair<Boolean, String?> {
        if (conditions.isEmpty()) return Pair(true, null)

        for (condition in conditions) {
            val evaluator = when (condition.type) {
                ConditionType.TIME_RANGE -> TimeRangeCondition()
                ConditionType.DAY_OF_WEEK -> DayOfWeekCondition()
                ConditionType.LAST_RUN -> LastRunCondition(ruleRepository)
            }
            if (!evaluator.evaluate(condition, context)) {
                return Pair(false, condition.type.name)
            }
        }
        return Pair(true, null)
    }
}