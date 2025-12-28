package com.ptit.common.presentation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TokenRefreshEventBus - Event bus để broadcast token refresh events
 *
 * Sử dụng:
 * - RefreshTokenAuthenticator emit event khi refresh thành công
 * - ViewModels/Screens collect event để reload data
 *
 * Benefits:
 * - ✅ Auto reload màn hình sau khi refresh token
 * - ✅ No need manual refresh
 * - ✅ Better UX (seamless)
 */
@Singleton
class TokenRefreshEventBus @Inject constructor() {

    private val _tokenRefreshedEvents = MutableSharedFlow<TokenRefreshEvent>(
        replay = 0, // Không replay events cũ
        extraBufferCapacity = 1 // Buffer 1 event
    )

    val tokenRefreshedEvents: SharedFlow<TokenRefreshEvent> = _tokenRefreshedEvents.asSharedFlow()

    /**
     * Emit event khi token được refresh thành công
     */
    suspend fun emitTokenRefreshed() {
        _tokenRefreshedEvents.emit(TokenRefreshEvent.TokenRefreshed)
    }

    /**
     * Emit event khi token refresh failed và user cần login lại
     */
    suspend fun emitTokenRefreshFailed() {
        _tokenRefreshedEvents.emit(TokenRefreshEvent.TokenRefreshFailed)
    }
}

/**
 * Các loại events
 */
sealed class TokenRefreshEvent {
    /**
     * Token refresh thành công
     * Screens nên reload data với token mới
     */
    object TokenRefreshed : TokenRefreshEvent()

    /**
     * Token refresh thất bại
     * User cần login lại
     */
    object TokenRefreshFailed : TokenRefreshEvent()
}

