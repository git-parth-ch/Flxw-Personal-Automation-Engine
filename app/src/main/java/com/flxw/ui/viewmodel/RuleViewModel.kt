package com.flxw.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flxw.data.model.Rule
import com.flxw.engine.ConflictDetector
import com.flxw.engine.Conflict
import com.flxw.engine.RuleEngine
import com.flxw.engine.TriggerScheduler
import com.flxw.repository.LogRepository
import com.flxw.repository.RuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import android.net.Uri
import com.flxw.util.JsonExportImport
import dagger.hilt.android.qualifiers.ApplicationContext
@HiltViewModel
class RuleViewModel @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val logRepository: LogRepository,
    private val triggerScheduler: TriggerScheduler,
    private val ruleEngine: RuleEngine,
    private val conflictDetector: ConflictDetector,
    private val jsonExportImport: JsonExportImport
) : ViewModel() {

    val rules: StateFlow<List<Rule>> = ruleRepository.getAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    private val _conflicts = MutableStateFlow<List<Conflict>>(emptyList())
    val conflicts: StateFlow<List<Conflict>> = _conflicts.asStateFlow()

    /** Save or update a rule, then schedule it */
    fun saveRule(rule: Rule) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                ruleRepository.upsert(rule)
                triggerScheduler.schedule(rule)
                checkConflicts()
                _snackbarMessage.emit("Rule '${rule.name}' saved")
            } catch (e: Exception) {
                _snackbarMessage.emit("Error saving rule: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Toggle enabled/disabled — reschedules WorkManager */
    fun toggleRule(id: String, enabled: Boolean) {
        viewModelScope.launch {
            ruleRepository.setEnabled(id, enabled)
            val rule = ruleRepository.getById(id) ?: return@launch
            triggerScheduler.schedule(rule)
            checkConflicts()
        }
    }

    /** Delete rule, cancel its WorkManager job, remove logs */
    fun deleteRule(id: String) {
        viewModelScope.launch {
            triggerScheduler.cancel(id)
            logRepository.deleteByRule(id)
            ruleRepository.deleteById(id)
            _snackbarMessage.emit("Rule deleted")
        }
    }

    /** Manual Run Now — fires rule engine directly (no WorkManager) */
    fun runNow(ruleId: String) {
        viewModelScope.launch {
            _snackbarMessage.emit("Running rule…")
            ruleEngine.evaluate(ruleId, dryRun = false)
            _snackbarMessage.emit("Rule executed — check Logs")
        }
    }

    /** Test run — dry-run mode, no real side effects */
    fun testRun(ruleId: String) {
        viewModelScope.launch {
            _snackbarMessage.emit("Running in sandbox mode…")
            ruleEngine.evaluate(ruleId, dryRun = true)
            _snackbarMessage.emit("Dry run complete — check Logs")
        }
    }

    fun getRule(id: String, onResult: (Rule?) -> Unit) {
        viewModelScope.launch {
            onResult(ruleRepository.getById(id))
        }
    }

    private suspend fun checkConflicts() {
        _conflicts.value = conflictDetector.detect()
    }
    fun exportRules(context: Context) {
        viewModelScope.launch {
            jsonExportImport.exportRules(context)
        }
    }

    fun importRules(context: Context, uri: Uri) {
        viewModelScope.launch {
            val count = jsonExportImport.importRules(context, uri)
            if (count > 0) {
                _snackbarMessage.emit("Imported $count rules successfully")
            } else {
                _snackbarMessage.emit("Import failed — check file format")
            }
        }
    }
}