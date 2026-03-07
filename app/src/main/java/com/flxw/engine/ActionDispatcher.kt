package com.flxw.engine

import android.content.Context
import com.flxw.data.model.ActionConfig
import com.flxw.data.model.ActionType
import com.flxw.engine.actions.ActionResult
import com.flxw.engine.actions.LogAction
import com.flxw.engine.actions.NotifyAction
import com.flxw.engine.actions.WebhookAction
import com.flxw.repository.LogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionDispatcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logRepository: LogRepository
) {
    /**
     * Runs all actions in order field sequence.
     * Returns list of results (one per action).
     */
    suspend fun run(
        actions: List<ActionConfig>,
        evalContext: EvalContext
    ): List<ActionResult> {
        val sorted = actions.sortedBy { it.order }
        return sorted.map { actionConfig ->
            val action = when (actionConfig.type) {
                ActionType.SHOW_NOTIFICATION -> NotifyAction(context)
                ActionType.LOG_MESSAGE -> LogAction(logRepository)
                ActionType.WEBHOOK -> WebhookAction()
            }
            action.execute(actionConfig, evalContext)
        }
    }
}