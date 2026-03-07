package com.flxw.engine

// Result from RuleEngine.evaluate()
sealed class EvalResult {
    data class Success(val durationMs: Long, val isDryRun: Boolean = false) : EvalResult()
    data class Skipped(val reason: String) : EvalResult()
    data class Failure(val error: String) : EvalResult()
}

// Context passed through condition/action evaluation
data class EvalContext(
    val ruleId: String,
    val dryRun: Boolean = false,
    val triggeredAt: Long = System.currentTimeMillis()
)