package com.flxw.engine.actions

import com.flxw.data.model.ActionConfig
import com.flxw.data.model.LogEntry
import com.flxw.data.model.LogStatus
import com.flxw.engine.EvalContext
import com.flxw.repository.LogRepository

class LogAction(private val logRepository: LogRepository) : IAction {
    override suspend fun execute(config: ActionConfig, context: EvalContext): ActionResult {
        return try {
            val message = if (context.dryRun) "[DRY RUN] ${config.logMessage}" else config.logMessage
            logRepository.insert(
                LogEntry(
                    ruleId = context.ruleId,
                    status = if (context.dryRun) LogStatus.DRY_RUN else LogStatus.SUCCESS,
                    message = message,
                    timestamp = System.currentTimeMillis()
                )
            )
            ActionResult.Success
        } catch (e: Exception) {
            ActionResult.Failure("Log write failed: ${e.message}")
        }
    }
}