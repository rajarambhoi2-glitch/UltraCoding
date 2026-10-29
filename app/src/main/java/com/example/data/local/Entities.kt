package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val templateType: String = "CUSTOM",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val projectId: String,
    val name: String,
    val path: String,
    val content: String,
    val language: String,
    val isDirty: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachedCodeSnippet: String? = null
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val provider: String = "gemini", // "gemini", "openai", "claude", "custom"
    val geminiApiKey: String = "",
    val openAiApiKey: String = "",
    val claudeApiKey: String = "",
    val customEndpoint: String = "https://api.openai.com/v1/chat/completions",
    val customApiKey: String = "",
    val fontSize: Int = 14,
    val tabSize: Int = 2,
    val autoSave: Boolean = true,
    val sendCodeToAiToggle: Boolean = true,
    val languageResponseMode: String = "hinglish" // "hinglish", "english", "hindi"
)
