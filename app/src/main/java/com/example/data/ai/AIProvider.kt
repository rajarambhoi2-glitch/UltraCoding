package com.example.data.ai

import com.example.data.model.ProblemItem
import com.example.data.model.QuickFixResult

interface AIProvider {
    val name: String

    suspend fun findBugs(
        code: String,
        language: String,
        context: String? = null
    ): Result<List<ProblemItem>>

    suspend fun quickFix(
        code: String,
        problem: ProblemItem,
        language: String
    ): Result<QuickFixResult>

    suspend fun writeCode(
        prompt: String,
        contextCode: String?,
        language: String
    ): Result<String>

    suspend fun explainCode(
        code: String,
        language: String,
        hinglish: Boolean
    ): Result<String>

    suspend fun chat(
        messages: List<Pair<String, String>>, // role to message
        currentCode: String?,
        language: String,
        hinglish: Boolean
    ): Result<String>
}
