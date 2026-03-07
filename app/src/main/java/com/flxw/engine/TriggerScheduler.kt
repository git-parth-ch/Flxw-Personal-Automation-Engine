package com.flxw.engine


import android.content.Context
import android.util.Log
import androidx.work.*
import com.flxw.data.model.Rule
import com.flxw.data.model.TriggerConfig
import com.flxw.engine.workers.ScheduledRuleWorker
import com.flxw.repository.RuleRepository
import com.flxw.data.model.TriggerType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TriggerScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ruleRepository: RuleRepository
) {
    companion object {
        private const val TAG = "TriggerScheduler"
        private val json = Json { ignoreUnknownKeys = true }
        const val WORK_TAG_PREFIX = "rule_"
    }

    fun schedule(rule: Rule) {
        if (!rule.enabled) {
            cancel(rule.id)
            return
        }

        val trigger = try {
            json.decodeFromString(TriggerConfig.serializer(), rule.triggerParamsJson)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse trigger config for rule ${rule.id}: ${e.message}")
            return
        }

        when (trigger.type) {
            TriggerType.TIME -> scheduleTimeRule(rule, trigger)
            TriggerType.INTERVAL -> scheduleIntervalRule(rule, trigger)
            TriggerType.MANUAL -> { /* Manual rules are not scheduled */ }
        }
    }

    private fun scheduleTimeRule(rule: Rule, trigger: TriggerConfig) {
        val initialDelay = calculateInitialDelay(trigger.hour, trigger.minute)

        val request = PeriodicWorkRequestBuilder<ScheduledRuleWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ScheduledRuleWorker.KEY_RULE_ID to rule.id))
            .addTag("${WORK_TAG_PREFIX}${rule.id}")
            .setConstraints(Constraints.Builder().build())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "${WORK_TAG_PREFIX}${rule.id}",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )

        Log.d(TAG, "Scheduled TIME rule '${rule.name}' at ${trigger.hour}:${trigger.minute.toString().padStart(2,'0')}, delay=${initialDelay}ms")
    }

    private fun scheduleIntervalRule(rule: Rule, trigger: TriggerConfig) {
        // Android minimum is 15 minutes
        val intervalMinutes = maxOf(trigger.intervalMinutes.toLong(), 15L)

        val request = PeriodicWorkRequestBuilder<ScheduledRuleWorker>(intervalMinutes, TimeUnit.MINUTES)
            .setInputData(workDataOf(ScheduledRuleWorker.KEY_RULE_ID to rule.id))
            .addTag("${WORK_TAG_PREFIX}${rule.id}")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "${WORK_TAG_PREFIX}${rule.id}",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )

        Log.d(TAG, "Scheduled INTERVAL rule '${rule.name}' every ${intervalMinutes}min")
    }

    fun cancel(ruleId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("${WORK_TAG_PREFIX}${ruleId}")
        Log.d(TAG, "Cancelled schedule for rule $ruleId")
    }

    suspend fun rescheduleAll() {
        val rules = ruleRepository.getEnabledRulesSortedByPriority()
        rules.forEach { schedule(it) }
        Log.d(TAG, "Rescheduled ${rules.size} rules")
    }

    /**
     * Calculate milliseconds until next occurrence of hour:minute today (or tomorrow if passed).
     */
    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1) // tomorrow
        }
        return target.timeInMillis - now.timeInMillis
    }
}