package com.example.data.ai

import com.example.data.model.ProblemItem
import com.example.data.model.ProblemSeverity
import com.example.data.model.QuickFixResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiProvider(
    private val apiKeyProvider: () -> String,
    private val fallback: OfflineFallbackProvider = OfflineFallbackProvider()
) : AIProvider {
    override val name: String = "Gemini AI"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private suspend fun callGemini(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKeyProvider().trim()
        if (key.isEmpty()) {
            return@withContext Result.failure(Exception("API key is not configured"))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$key"
            val requestBodyObj = JSONObject().apply {
                val contentsArr = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArr = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", partsArr)
                    }
                    put(contentObj)
                }
                put("contents", contentsArr)
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBodyObj.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gemini API error ${response.code}: $respBody"))
            }

            val root = JSONObject(respBody)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    return@withContext Result.success(text)
                }
            }

            Result.failure(Exception("Empty response from Gemini API"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun findBugs(
        code: String,
        language: String,
        context: String?
    ): Result<List<ProblemItem>> {
        val key = apiKeyProvider()
        if (key.isBlank()) {
            return fallback.findBugs(code, language, context)
        }

        val prompt = """You are an expert static analyzer and bug detector for $language.
Analyze the following code for syntax errors, logical bugs, security flaws (hardcoded secrets, XSS, injection), and performance problems.
Return a STRICT JSON ARRAY of problem objects, each with:
- "line": integer line number (1-based)
- "severity": "ERROR", "WARNING", or "INFO"
- "message": short description of the problem in clear English or Hinglish
- "rule": identifier rule like "security/xss" or "syntax/missing-colon"
- "suggestedFix": string replacement for that line or immediate fix

DO NOT include any markdown code fences or explanations outside the JSON array. Output only valid JSON.
CODE:
$code"""

        val result = callGemini(prompt)
        return if (result.isSuccess) {
            try {
                var jsonStr = result.getOrNull()?.trim() ?: "[]"
                if (jsonStr.startsWith("```json")) {
                    jsonStr = jsonStr.removePrefix("```json").removeSuffix("```").trim()
                } else if (jsonStr.startsWith("```")) {
                    jsonStr = jsonStr.removePrefix("```").removeSuffix("```").trim()
                }

                val jsonArray = JSONArray(jsonStr)
                val problems = mutableListOf<ProblemItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val severityStr = obj.optString("severity", "WARNING").uppercase()
                    val severity = when (severityStr) {
                        "ERROR" -> ProblemSeverity.ERROR
                        "INFO" -> ProblemSeverity.INFO
                        else -> ProblemSeverity.WARNING
                    }
                    problems.add(
                        ProblemItem(
                            line = obj.optInt("line", 1),
                            column = 1,
                            severity = severity,
                            message = obj.optString("message", "Issue detected"),
                            rule = obj.optString("rule", "ai-lint"),
                            suggestedFix = obj.optString("suggestedFix", null)
                        )
                    )
                }
                Result.success(problems)
            } catch (e: Exception) {
                fallback.findBugs(code, language, context)
            }
        } else {
            fallback.findBugs(code, language, context)
        }
    }

    override suspend fun quickFix(
        code: String,
        problem: ProblemItem,
        language: String
    ): Result<QuickFixResult> {
        val key = apiKeyProvider()
        if (key.isBlank()) {
            return fallback.quickFix(code, problem, language)
        }

        val prompt = """Fix the following $language issue:
Rule: ${problem.rule}
Message: ${problem.message}
At Line: ${problem.line}

Original Code:
$code

Return only the fully fixed complete code. Do not wrap in markdown quotes if possible, or use standard code fence."""

        val result = callGemini(prompt)
        return if (result.isSuccess) {
            var fixed = result.getOrNull() ?: code
            if (fixed.contains("```")) {
                val lines = fixed.lines()
                val codeLines = lines.dropWhile { it.startsWith("```") }
                    .takeWhile { !it.startsWith("```") }
                fixed = codeLines.joinToString("\n")
            }
            Result.success(
                QuickFixResult(
                    originalCode = code,
                    fixedCode = fixed,
                    explanation = "AI Fixed: ${problem.message}",
                    diffSummary = "Applied AI correction for line ${problem.line}"
                )
            )
        } else {
            fallback.quickFix(code, problem, language)
        }
    }

    override suspend fun writeCode(
        prompt: String,
        contextCode: String?,
        language: String
    ): Result<String> {
        val key = apiKeyProvider()
        if (key.isBlank()) {
            return fallback.writeCode(prompt, contextCode, language)
        }

        val userPrompt = """Write clean, production-ready $language code for the following request:
$prompt

${if (!contextCode.isNullOrBlank()) "Active file context:\n$contextCode\n" else ""}
Output only the pure code snippet without extra markdown chat conversational text."""

        val result = callGemini(userPrompt)
        return if (result.isSuccess) {
            var clean = result.getOrNull() ?: ""
            if (clean.contains("```")) {
                val lines = clean.lines()
                val codeLines = lines.dropWhile { it.startsWith("```") }
                    .takeWhile { !it.startsWith("```") }
                clean = codeLines.joinToString("\n")
            }
            Result.success(clean)
        } else {
            fallback.writeCode(prompt, contextCode, language)
        }
    }

    override suspend fun explainCode(
        code: String,
        language: String,
        hinglish: Boolean
    ): Result<String> {
        val key = apiKeyProvider()
        if (key.isBlank()) {
            return fallback.explainCode(code, language, hinglish)
        }

        val languageInst = if (hinglish) {
            "Explain in friendly Hinglish (Hindi + English mix). Use bullet points."
        } else {
            "Explain clearly in professional English. Use concise bullet points."
        }

        val prompt = """You are a senior code reviewer. Explain what the following $language code does.
$languageInst

CODE:
$code"""

        return callGemini(prompt)
    }

    override suspend fun chat(
        messages: List<Pair<String, String>>,
        currentCode: String?,
        language: String,
        hinglish: Boolean
    ): Result<String> {
        val key = apiKeyProvider()
        if (key.isBlank()) {
            return fallback.chat(messages, currentCode, language, hinglish)
        }

        val conversationStr = messages.joinToString("\n") { "${it.first.uppercase()}: ${it.second}" }
        val prompt = """You are CodeForge AI, an expert code editor assistant embedded in a mobile IDE.
Current File Language: $language
${if (!currentCode.isNullOrBlank()) "Current active code in editor:\n$currentCode\n" else ""}

Response Language Rule: ${if (hinglish) "Respond in natural conversational Hinglish (Hindi + English) as preferred by Indian developers." else "Respond in professional English."}

Conversation History:
$conversationStr
ASSISTANT:"""

        return callGemini(prompt)
    }
}
