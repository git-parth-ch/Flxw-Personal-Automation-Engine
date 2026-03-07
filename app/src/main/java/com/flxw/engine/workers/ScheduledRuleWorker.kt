package com.flxw.engine.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.flxw.engine.RuleEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ScheduledRuleWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val ruleEngine: RuleEngine
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_RULE_ID = "rule_id"
        private const val TAG = "ScheduledRuleWorker"
    }

    override suspend fun doWork(): Result {
        val ruleId = inputData.getString(KEY_RULE_ID)
            ?: return Result.failure().also { Log.e(TAG, "No rule_id in work data") }

        Log.d(TAG, "Worker firing for rule: $ruleId")

        return try {
            val result = ruleEngine.evaluate(ruleId)
            Log.d(TAG, "Rule $ruleId result: $result")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed for rule $ruleId: ${e.message}", e)
            Result.retry()
        }
    }
}