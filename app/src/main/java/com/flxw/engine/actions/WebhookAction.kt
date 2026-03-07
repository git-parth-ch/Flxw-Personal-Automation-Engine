package com.flxw.engine.actions

import android.util.Log
import com.flxw.data.model.ActionConfig
import com.flxw.engine.EvalContext
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

class WebhookAction : IAction {

    private val client = HttpClient(Android) {
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
            connectTimeoutMillis = 10_000
        }
        install(ContentNegotiation) { json() }
    }

    override suspend fun execute(config: ActionConfig, context: EvalContext): ActionResult {
        if (context.dryRun) return ActionResult.Success // Skip real HTTP in dry-run
        if (config.webhookUrl.isBlank()) return ActionResult.Failure("Webhook URL is empty")

        return try {
            val response = client.post(config.webhookUrl) {
                contentType(ContentType.Application.Json)
                setBody(config.webhookPayload)
            }
            Log.d("WebhookAction", "POST to ${config.webhookUrl} → ${response.status}")
            ActionResult.Success
        } catch (e: Exception) {
            Log.e("WebhookAction", "Webhook failed: ${e.message}")
            ActionResult.Failure("Webhook failed: ${e.message}")
        }
    }
}