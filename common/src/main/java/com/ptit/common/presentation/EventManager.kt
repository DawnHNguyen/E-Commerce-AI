package com.ptit.common.presentation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object EventManager {
    private val _events = Channel<String>()
    val events = _events.receiveAsFlow()

    suspend fun sendEvent(message: String) {
        _events.send(message)
    }

}
