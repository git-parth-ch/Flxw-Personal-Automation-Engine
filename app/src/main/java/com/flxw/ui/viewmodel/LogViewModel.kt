package com.flxw.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flxw.data.model.LogEntry
import com.flxw.data.model.LogStatus
import com.flxw.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val logRepository: LogRepository
) : ViewModel() {

    private val _statusFilter = MutableStateFlow<LogStatus?>(null)
    val statusFilter: StateFlow<LogStatus?> = _statusFilter.asStateFlow()

    val logs: StateFlow<List<LogEntry>> = _statusFilter.flatMapLatest { status ->
        if (status == null) logRepository.getRecent(100)
        else logRepository.getByStatus(status)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(status: LogStatus?) {
        _statusFilter.value = status
    }

    fun clearLogs() {
        viewModelScope.launch { logRepository.clearAll() }
    }

    fun getLogsForRule(ruleId: String): Flow<List<LogEntry>> =
        logRepository.getByRule(ruleId)
}