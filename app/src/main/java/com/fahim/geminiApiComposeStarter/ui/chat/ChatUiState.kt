package com.fahim.geminiApiComposeStarter.ui.chat

import com.fahim.geminiApiComposeStarter.data.local.ChatMessageEntity

/** Immutable UI state for the Gemini chat application. */
data class ChatUiState(
    val messages: List<ChatMessageEntity> = emptyList(),
    val prompt: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val promptError: PromptError? = null,
    val isDarkMode: Boolean = false,
    val selectedModel: String = "gemini-3.6-flash"
)

enum class PromptError { EMPTY }
