package com.fahim.geminiApiComposeStarter.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fahim.geminiApiComposeStarter.data.GeminiRepository
import com.fahim.geminiApiComposeStarter.data.local.ChatMessageDao
import com.fahim.geminiApiComposeStarter.data.local.ChatMessageEntity
import com.fahim.geminiApiComposeStarter.data.preferences.UserPreferencesRepository
import com.fahim.geminiApiComposeStarter.security.KeystoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: GeminiRepository,
    private val chatMessageDao: ChatMessageDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val hasApiKey: Boolean
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Collect saved chat history from Room DB
        viewModelScope.launch(Dispatchers.IO) {
            chatMessageDao.getAllMessages().collect { dbMessages ->
                val decryptedMessages = dbMessages.map { entity ->
                    val decryptedPrompt = KeystoreManager.decrypt(entity.prompt)
                    entity.copy(prompt = decryptedPrompt)
                }
                _uiState.update { it.copy(messages = decryptedMessages) }
            }
        }

        // Collect theme preference from DataStore
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.isDarkModeFlow.collect { isDark ->
                _uiState.update { it.copy(isDarkMode = isDark) }
            }
        }

        // Collect model preference from DataStore
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.selectedModelFlow.collect { model ->
                _uiState.update { it.copy(selectedModel = model) }
            }
        }
    }

    fun onPromptChange(value: String) {
        _uiState.update { it.copy(prompt = value, promptError = null) }
    }

    fun onVoiceResult(recognizedText: String) {
        if (recognizedText.isNotBlank()) {
            _uiState.update { it.copy(prompt = recognizedText, promptError = null) }
        }
    }

    fun onSelectModel(modelName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.setSelectedModel(modelName)
        }
    }

    fun onToggleDarkMode() {
        val newMode = !_uiState.value.isDarkMode
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.setDarkMode(newMode)
        }
    }

    fun onClearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            chatMessageDao.clearAll()
        }
    }

    fun onSend() {
        val promptText = _uiState.value.prompt.trim()
        if (promptText.isEmpty()) {
            _uiState.update { it.copy(promptError = PromptError.EMPTY) }
            return
        }
        if (!hasApiKey) {
            _uiState.update { it.copy(errorMessage = MISSING_API_KEY_MESSAGE) }
            return
        }
        if (_uiState.value.isLoading) return

        val currentModel = _uiState.value.selectedModel
        _uiState.update { it.copy(isLoading = true, errorMessage = null, promptError = null, prompt = "") }

        viewModelScope.launch(Dispatchers.IO) {
            repository.generateText(promptText, currentModel).fold(
                onSuccess = { responseText ->
                    // Encrypt prompt before persisting to Room database (Android Keystore AES-256-GCM)
                    val encryptedPrompt = KeystoreManager.encrypt(promptText)
                    val messageEntity = ChatMessageEntity(
                        prompt = encryptedPrompt,
                        response = responseText,
                        modelName = currentModel
                    )
                    chatMessageDao.insertMessage(messageEntity)
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Something went wrong generating response."
                        )
                    }
                }
            )
        }
    }

    fun onRetry(prompt: String) {
        _uiState.update { it.copy(prompt = prompt) }
        onSend()
    }

    companion object {
        const val MISSING_API_KEY_MESSAGE =
            "GEMINI_API_KEY is missing. Add it to local.properties and rebuild."

        fun factory(
            repository: GeminiRepository,
            chatMessageDao: ChatMessageDao,
            preferencesRepository: UserPreferencesRepository,
            hasApiKey: Boolean
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ChatViewModel(repository, chatMessageDao, preferencesRepository, hasApiKey) as T
        }
    }
}
