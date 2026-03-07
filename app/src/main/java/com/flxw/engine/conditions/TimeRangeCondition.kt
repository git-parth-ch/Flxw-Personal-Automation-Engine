package com.flxw.engine.conditions

import com.flxw.data.model.ConditionConfig
import com.flxw.engine.EvalContext
import java.util.Calendar

class TimeRangeCondition : ICondition {
    override fun evaluate(config: ConditionConfig, context: EvalContext): Boolean {
        val cal = Calendar.getInstance()
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val startMinutes = config.startHour * 60 + config.startMinute
        val endMinutes = config.endHour * 60 + config.endMinute

        return if (startMinutes <= endMinutes) {
            // Normal range e.g. 09:00–18:00
            currentMinutes in startMinutes..endMinutes
        } else {
            // Overnight range e.g. 22:00–06:00
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }
}