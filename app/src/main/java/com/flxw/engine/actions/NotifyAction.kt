package com.flxw.engine.actions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.flxw.R
import com.flxw.data.model.ActionConfig
import com.flxw.engine.EvalContext
import kotlin.random.Random

class NotifyAction(private val context: Context) : IAction {

    companion object {
        const val CHANNEL_ID = "automation_engine_rules"
        const val CHANNEL_NAME = "Rule Notifications"

        fun createChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications triggered by automation rules"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override suspend fun execute(config: ActionConfig, context: EvalContext): ActionResult {
        if (context.dryRun) return ActionResult.Success // Dry-run: skip real notification

        return try {
            val notificationManager =
                this.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notification = NotificationCompat.Builder(this.context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(config.notifTitle)
                .setContentText(config.notifBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(Random.nextInt(), notification)
            ActionResult.Success
        } catch (e: Exception) {
            ActionResult.Failure("Notification failed: ${e.message}")
        }
    }
}