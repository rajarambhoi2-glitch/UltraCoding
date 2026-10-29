package com.example.data.ai

import com.example.data.local.AppSettingsDao
import com.example.data.local.AppSettingsEntity
import com.example.data.model.ProblemItem
import com.example.data.model.QuickFixResult

class AiManager(
    private val appSettingsDao: AppSettingsDao
) {
    private val offlineProvider = OfflineFallbackProvider()

    private fun getBuildConfigGeminiKey(): String {
        return try {
            val field = Class.forName("com.example.BuildConfig").getField("GEMINI_API_KEY")
            (field.get(null) as? String) ?: ""
        } catch (e: Throwable) {
            ""
        }
    }

    suspend fun getActiveProvider(): AIProvider {
        val settings = appSettingsDao.getSettings() ?: AppSettingsEntity()
        val buildConfigKey = getBuildConfigGeminiKey()

        return when (settings.provider.lowercase()) {
            "gemini" -> {
                val effectiveKey = settings.geminiApiKey.ifBlank { buildConfigKey }
                if (effectiveKey.isNotBlank()) {
                    GeminiProvider(apiKeyProvider = { effectiveKey }, fallback = offlineProvider)
                } else {
                    offlineProvider
                }
            }
            "openai" -> {
                if (settings.openAiApiKey.isNotBlank()) {
                    OpenAIProvider(
                        apiKeyProvider = { settings.openAiApiKey },
                        modelProvider = { "gpt-4o-mini" },
                        fallback = offlineProvider
                    )
                } else {
                    offlineProvider
                }
            }
            "claude" -> {
                if (settings.claudeApiKey.isNotBlank()) {
                    // Claude / Anthropic compatible endpoint or OpenAI fallback
                    OpenAIProvider(
                        endpointProvider = { "https://api.anthropic.com/v1/messages" },
                        apiKeyProvider = { settings.claudeApiKey },
                        modelProvider = { "claude-3-5-sonnet-latest" },
                        fallback = offlineProvider
                    )
                } else {
                    offlineProvider
                }
            }
            "custom" -> {
                if (settings.customApiKey.isNotBlank() || settings.customEndpoint.isNotBlank()) {
                    OpenAIProvider(
                        endpointProvider = { settings.customEndpoint },
                        apiKeyProvider = { settings.customApiKey },
                        fallback = offlineProvider
                    )
                } else {
                    offlineProvider
                }
            }
            else -> offlineProvider
        }
    }

    suspend fun findBugs(code: String, language: String, context: String? = null): Result<List<ProblemItem>> {
        val provider = getActiveProvider()
        return provider.findBugs(code, language, context)
    }

    suspend fun quickFix(code: String, problem: ProblemItem, language: String): Result<QuickFixResult> {
        val provider = getActiveProvider()
        return provider.quickFix(code, problem, language)
    }

    suspend fun writeCode(prompt: String, contextCode: String?, language: String): Result<String> {
        val provider = getActiveProvider()
        return provider.writeCode(prompt, contextCode, language)
    }

    suspend fun explainCode(code: String, language: String, hinglish: Boolean): Result<String> {
        val provider = getActiveProvider()
        return provider.explainCode(code, language, hinglish)
    }

    suspend fun chat(messages: List<Pair<String, String>>, currentCode: String?, language: String, hinglish: Boolean): Result<String> {
        val provider = getActiveProvider()
        return provider.chat(messages, currentCode, language, hinglish)
    }
}
