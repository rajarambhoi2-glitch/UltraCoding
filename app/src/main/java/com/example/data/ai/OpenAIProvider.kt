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

class OpenAIProvider(
    private val endpointProvider: () -> String = { "https://api.openai.com/v1/chat/completions" },
    private val apiKeyProvider: () -> String,
    private val modelProvider: () -> String = { "gpt-4o-mini" },
    private val fallback: OfflineFallbackProvider = OfflineFallbackProvider()
) : AIProvider {
    override val name: String = "OpenAI / Custom"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private suspend fun callEndpoint(messages: List<Pair<String, String>>): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKeyProvider().trim()
        if (key.isEmpty()) {
            return@withContext Result.failure(Exception("API key not provided"))
        }

        try {
            val endpoint = endpointProvider().ifBlank { "https://api.openai.com/v1/chat/completions" }
            val root = JSONObject().apply {
                put("model", modelProvider())
                val msgsArr = JSONArray()
                for (m in messages) {
                    msgsArr.put(JSONObject().apply {
                        put("role", m.first)
                        put("content", m.second)
                    })
                }
                put("messages", msgsArr)
            }

            val request = Request.Builder()
                .url(endpoint)
                .header("Authorization", "Bearer $key")
                .header("Content-Type", "application/json")
                .post(root.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API error ${response.code}: $respBody"))
            }

            val respJson = JSONObject(respBody)
            val choices = respJson.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val messageObj = choices.getJSONObject(0).optJSONObject("message")
                val content = messageObj?.optString("content", "") ?: ""
                return@withContext Result.success(content)
            }

            Result.failure(Exception("Invalid API response format"))
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
        if (key.isBlank()) return fallback.findBugs(code, language, context)

        val prompt = "Return only a JSON array of problem objects {line: int, severity: 'ERROR'|'WARNING'|'INFO', message: str, rule: str, suggestedFix: str} for this $language code:\n$code"
        val res = callEndpoint(listOf("system" to "You are a code linter that outputs strict JSON.", "user" to prompt))
        return if (res.isSuccess) {
            try {
                var jsonStr = res.getOrNull()?.trim() ?: "[]"
                if (jsonStr.startsWith("```json")) {
                    jsonStr = jsonStr.removePrefix("```json").removeSuffix("```").trim()
                } else if (jsonStr.startsWith("```")) {
                    jsonStr = jsonStr.removePrefix("```").removeSuffix("```").trim()
                }
                val arr = JSONArray(jsonStr)
                val list = mutableListOf<ProblemItem>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val sevStr = obj.optString("severity", "WARNING").uppercase()
                    val sev = when (sevStr) {
                        "ERROR" -> ProblemSeverity.ERROR
                        "INFO" -> ProblemSeverity.INFO
                        else -> ProblemSeverity.WARNING
                    }
                    list.add(
                        ProblemItem(
                            line = obj.optInt("line", 1),
                            severity = sev,
                            message = obj.optString("message", "Lint issue"),
                            rule = obj.optString("rule", "ai-lint"),
                            suggestedFix = obj.optString("suggestedFix", null)
                        )
                    )
                }
                Result.success(list)
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
        if (key.isBlank()) return fallback.quickFix(code, problem, language)

        val res = callEndpoint(listOf(
            "system" to "You fix code. Output ONLY the fixed complete code without explanation.",
            "user" to "Fix issue ${problem.message} at line ${problem.line} in:\n$code"
        ))

        return if (res.isSuccess) {
            Result.success(
                QuickFixResult(
                    originalCode = code,
                    fixedCode = res.getOrNull() ?: code,
                    explanation = "Fixed: ${problem.message}",
                    diffSummary = "Applied fix to line ${problem.line}"
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
        if (key.isBlank()) return fallback.writeCode(prompt, contextCode, language)

        val res = callEndpoint(listOf(
            "system" to "Output only raw code for $language without markdown decorations.",
            "user" to "Prompt: $prompt\nContext: ${contextCode ?: "None"}"
        ))
        return if (res.isSuccess) res else fallback.writeCode(prompt, contextCode, language)
    }

    override suspend fun explainCode(
        code: String,
        language: String,
        hinglish: Boolean
    ): Result<String> {
        val key = apiKeyProvider()
        if (key.isBlank()) return fallback.explainCode(code, language, hinglish)

        return callEndpoint(listOf(
            "system" to if (hinglish) "Explain in friendly Hinglish." else "Explain in English.",
            "user" to "Explain this $language code:\n$code"
        ))
    }

    override suspend fun chat(
        messages: List<Pair<String, String>>,
        currentCode: String?,
        language: String,
        hinglish: Boolean
    ): Result<String> {
        val key = apiKeyProvider()
        if (key.isBlank()) return fallback.chat(messages, currentCode, language, hinglish)

        val sysMsg = "You are CodeForge AI, an expert code assistant inside a mobile VS Code-style IDE. Current language: $language.\n${if (hinglish) "Speak in natural Hinglish." else "Speak in English."}\nActive code:\n${currentCode ?: ""}"
        val formatted = mutableListOf("system" to sysMsg)
        for (m in messages) {
            formatted.add(m.first to m.second)
        }
        return callEndpoint(formatted)
    }
}
