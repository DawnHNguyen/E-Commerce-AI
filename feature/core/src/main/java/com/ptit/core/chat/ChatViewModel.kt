package com.ptit.core.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.chat.ChatMessage
import com.ptit.domain.entity.chat.UiTag
import com.ptit.domain.usecase.chat.GetChatHistoryUseCase
import com.ptit.domain.usecase.chat.SendChatMessageUseCase
import com.ptit.domain.usecase.chat.ClearChatHistoryUseCase
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NavigationEvent {
    data object AddPayment : NavigationEvent()
    data class OrderDetail(val orderId: String) : NavigationEvent()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val getChatHistoryUseCase: GetChatHistoryUseCase,
    private val clearChatHistoryUseCase: ClearChatHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentSessionId: String? = null
    private var historyObserverJob: Job? = null

    fun setSessionId(sessionId: String) {
        if (currentSessionId == sessionId) return

        currentSessionId = sessionId
        _uiState.update { it.copy(sessionId = sessionId) }

        // Cancel previous observer and start new one
        historyObserverJob?.cancel()
        observeChatHistory(sessionId)
    }

    private fun observeChatHistory(sessionId: String) {
        historyObserverJob = getChatHistoryUseCase(sessionId)
            .onEach { messages ->
                _uiState.update { it.copy(messages = messages) }

                // Check for navigation events in the latest AI message
                messages.lastOrNull()?.let { lastMessage ->
                    processNavigationTags(lastMessage)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun processNavigationTags(message: ChatMessage) {
        message.tags.forEach { tag ->
            when (tag) {
                is UiTag.NavigateToScreen -> {
                    when (tag.screen) {
                        "add_payment" -> {
                            _uiState.update {
                                it.copy(navigationEvent = NavigationEvent.AddPayment)
                            }
                        }
                        "order_detail" -> {
                            // Extract order ID if present in the message
                            val orderIdRegex = Regex("""order[_\s]?id[:\s]+([A-Za-z0-9-]+)""", RegexOption.IGNORE_CASE)
                            orderIdRegex.find(message.content)?.groupValues?.get(1)?.let { orderId ->
                                _uiState.update {
                                    it.copy(navigationEvent = NavigationEvent.OrderDetail(orderId))
                                }
                            }
                        }
                    }
                }
                else -> { /* Other tags are handled in UI */ }
            }
        }
    }

    fun sendMessage(message: String) {
        val sessionId = currentSessionId ?: return
        if (message.isBlank()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            sendChatMessageUseCase(sessionId, message)
                .onEach { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is Resource.Success -> {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = resource.error.message ?: "Unknown error"
                                )
                            }
                        }
                        else -> {}
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun clearHistory() {
        val sessionId = currentSessionId ?: return

        viewModelScope.launch {
            when (val result = clearChatHistoryUseCase(sessionId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(messages = emptyList()) }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(error = result.error.message ?: "Failed to clear history")
                    }
                }
                else -> {}
            }
        }
    }

    fun clearNavigationEvent() {
        _uiState.update { it.copy(navigationEvent = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ChatUiState(
    val sessionId: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigationEvent: NavigationEvent? = null
)
