package com.flxw.engine.conditions

import com.flxw.data.model.ConditionConfig
import com.flxw.engine.EvalContext
import java.util.Calendar

class DayOfWeekCondition : ICondition {
    override fun evaluate(config: ConditionConfig, context: EvalContext): Boolean {
        // Calendar.DAY_OF_WEEK: 1=Sunday, 2=Monday, ... 7=Saturday
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return today in config.days
    }
}