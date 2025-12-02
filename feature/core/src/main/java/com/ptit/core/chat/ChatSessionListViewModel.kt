package com.ptit.core.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.chat.ChatSession
import com.ptit.domain.repository.ChatRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatSessionListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatSessionListUiState())
    val uiState: StateFlow<ChatSessionListUiState> = _uiState.asStateFlow()

    init {
        observeSessions()
    }

    private fun observeSessions() {
        chatRepository.getAllSessions()
            .onEach { sessions ->
                _uiState.update { it.copy(sessions = sessions, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun createNewSession(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = chatRepository.createSession()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(result.data.id)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error.message
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            when (val result = chatRepository.deleteSession(sessionId)) {
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.error.message) }
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ChatSessionListUiState(
    val sessions: List<ChatSession> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)