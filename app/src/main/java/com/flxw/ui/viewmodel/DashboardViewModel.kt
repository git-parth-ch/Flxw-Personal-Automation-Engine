package com.flxw.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flxw.repository.LogRepository
import com.flxw.repository.RuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class DashboardStats(
    val totalRules: Int = 0,
    val enabledRules: Int = 0,
    val totalLogs: Int = 0,
    val recentLogs: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val logRepository: LogRepository
) : ViewModel() {

    val allRules = ruleRepository.getAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs = logRepository.getRecent(20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}