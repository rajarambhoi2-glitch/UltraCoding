package com.example.data.model

enum class ProblemSeverity {
    ERROR,
    WARNING,
    INFO
}

data class ProblemItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val line: Int,
    val column: Int = 1,
    val severity: ProblemSeverity,
    val message: String,
    val rule: String,
    val suggestedFix: String? = null,
    val filePath: String? = null
)

data class QuickFixResult(
    val originalCode: String,
    val fixedCode: String,
    val explanation: String,
    val diffSummary: String
)
