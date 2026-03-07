package com.flxw.engine.actions

import com.flxw.data.model.ActionConfig
import com.flxw.engine.EvalContext

interface IAction {
    suspend fun execute(config: ActionConfig, context: EvalContext): ActionResult
}

sealed class ActionResult {
    object Success : ActionResult()
    data class Failure(val error: String) : ActionResult()
}