package com.flxw.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.flxw.data.model.Rule
import com.flxw.repository.RuleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonExportImport @Inject constructor(
    private val ruleRepository: RuleRepository
) {
    companion object {
        private const val TAG = "JsonExportImport"
        private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    }

    suspend fun exportRules(context: Context) = withContext(Dispatchers.IO) {
        try {
            // Get ALL rules using Flow.first()
            val rules = ruleRepository.getAllRules().first()
            val jsonString = json.encodeToString(rules)

            val exportFile = File(context.cacheDir, "flxw_rules_export.json")
            exportFile.writeText(jsonString)

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Flxw Rules Export")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export Rules").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            Log.d(TAG, "Exported ${rules.size} rules")
        } catch (e: Exception) {
            Log.e(TAG, "Export failed: ${e.message}", e)
        }
    }

    suspend fun importRules(context: Context, uri: Uri): Int = withContext(Dispatchers.IO) {
        return@withContext try {
            val jsonString = context.contentResolver
                .openInputStream(uri)
                ?.bufferedReader()
                ?.readText()
                ?: return@withContext 0

            val importedRules: List<Rule> = json.decodeFromString(jsonString)
            importedRules.forEach { rule ->
                ruleRepository.upsert(rule.copy(id = UUID.randomUUID().toString()))
            }
            Log.d(TAG, "Imported ${importedRules.size} rules")
            importedRules.size
        } catch (e: Exception) {
            Log.e(TAG, "Import failed: ${e.message}", e)
            -1
        }
    }
}