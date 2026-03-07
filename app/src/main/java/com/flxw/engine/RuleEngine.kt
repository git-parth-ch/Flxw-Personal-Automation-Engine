package com.flxw.engine

import android.util.Log
import com.flxw.data.model.ConditionConfig
import com.flxw.data.model.ActionConfig
import com.flxw.data.model.LogEntry
import com.flxw.data.model.LogStatus
import com.flxw.repository.LogRepository
import com.flxw.repository.RuleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleEngine @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val logRepository: LogRepository,
    private val conditionEvaluator: ConditionEvaluator,
    private val actionDispatcher: ActionDispatcher
) {
    companion object {
        private const val TAG = "RuleEngine"
        private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    }

    /**
     * Main evaluation entry point.
     * Load rule → evaluate conditions → run actions → log result.
     */
    suspend fun evaluate(ruleId: String, dryRun: Boolean = false): EvalResult =
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val context = EvalContext(ruleId = ruleId, dryRun = dryRun)

            try {
                // 1. Load rule
                val rule = ruleRepository.getById(ruleId)
                    ?: return@withContext EvalResult.Failure("Rule $ruleId not found")

                if (!rule.enabled) {
                    return@withContext EvalResult.Skipped("Rule is disabled")
                }

                Log.d(TAG, "Evaluating rule: ${rule.name} (dryRun=$dryRun)")

                // 2. Parse conditions
                val conditions: List<ConditionConfig> = try {
                    if (rule.conditionsJson == "[]" || rule.conditionsJson.isBlank()) emptyList()
                    else json.decodeFromString(
                        ListSerializer(ConditionConfig.serializer()),
                        rule.conditionsJson
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse conditions: ${e.message}")
                    emptyList()
                }

                // 3. Evaluate conditions (AND logic)
                val (pass, failedCondition) = conditionEvaluator.allPass(conditions, context)
                if (!pass) {
                    val reason = "Condition failed: $failedCondition"
                    logRepository.insert(
                        LogEntry(
                            ruleId = ruleId,
                            ruleName = rule.name,
                            status = LogStatus.SKIPPED,
                            message = reason,
                            durationMs = System.currentTimeMillis() - startTime
                        )
                    )
                    return@withContext EvalResult.Skipped(reason)
                }

                // 4. Parse actions
                val actions: List<ActionConfig> = try {
                    json.decodeFromString(
                        ListSerializer(ActionConfig.serializer()),
                        rule.actionsJson
                    )
                } catch (e: Exception) {
                    return@withContext EvalResult.Failure("Failed to parse actions: ${e.message}")
                }

                // 5. Dispatch actions
                val results = actionDispatcher.run(actions, context)
                val duration = System.currentTimeMillis() - startTime

                // 6. Check for failures
                val failures = results.filterIsInstance<com.flxw.engine.actions.ActionResult.Failure>()
                val status = when {
                    dryRun -> LogStatus.DRY_RUN
                    failures.isEmpty() -> LogStatus.SUCCESS
                    else -> LogStatus.FAILED
                }

                // 7. Write final log entry
                logRepository.insert(
                    LogEntry(
                        ruleId = ruleId,
                        ruleName = rule.name,
                        status = status,
                        message = if (failures.isEmpty()) {
                            if (dryRun) "[DRY RUN] All actions completed" else "All actions completed"
                        } else {
                            failures.joinToString("; ") { it.error }
                        },
                        durationMs = duration
                    )
                )

                // 8. Update lastRunAt (skip for dry-run)
                if (!dryRun) {
                    ruleRepository.updateLastRunAt(ruleId, System.currentTimeMillis())
                }

                Log.d(TAG, "Rule ${rule.name} → $status in ${duration}ms")

                if (failures.isEmpty()) EvalResult.Success(duration, dryRun)
                else EvalResult.Failure(failures.first().error)

            } catch (e: Exception) {
                Log.e(TAG, "RuleEngine crashed for rule $ruleId: ${e.message}", e)
                val duration = System.currentTimeMillis() - startTime
                logRepository.insert(
                    LogEntry(
                        ruleId = ruleId,
                        status = LogStatus.FAILED,
                        message = "Engine error: ${e.message}",
                        durationMs = duration
                    )
                )
                EvalResult.Failure("Engine error: ${e.message}")
            }
        }
}