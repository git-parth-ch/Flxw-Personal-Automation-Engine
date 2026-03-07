package com.flxw.engine.conditions

import com.flxw.data.model.ConditionConfig
import com.flxw.engine.EvalContext

interface ICondition {
    fun evaluate(config: ConditionConfig, context: EvalContext): Boolean
}