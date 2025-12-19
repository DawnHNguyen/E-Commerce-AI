package com.ptit.data.remote.util

import android.util.Log
import com.ptit.data.remote.api.NoAuthInterceptApi
import com.ptit.data.remote.dto.auth.RefreshTokenRequest
import com.ptit.domain.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/**
 * RefreshTokenAuthenticator - Tự động refresh token khi nhận HTTP 401
 *
 * Flow:
 * 1. API call bị 401 (Unauthorized)
 * 2. OkHttp gọi authenticate()
 * 3. Lấy refresh token từ storage
 * 4. Gọi API refresh token (sử dụng NoAuthInterceptApi)
 * 5. Nếu thành công: lưu token mới và retry request
 * 6. Nếu thất bại: clear tokens và return null (user về Login)
 */
class RefreshTokenAuthenticator @Inject constructor(
    private val remoteService: NoAuthInterceptApi,
    private val tokenManager: TokenManager
) : Authenticator {


    companion object {
        private const val TAG = "RefreshTokenAuth"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Lấy refresh token từ storage
        val refreshToken = tokenManager.getRefreshToken()

        if (refreshToken.isNullOrBlank()) {
            Log.w(TAG, "⚠️ No refresh token found, cannot refresh")
            return null
        }

        Log.d(TAG, "🔄 Access token expired, refreshing...")

        return runBlocking(Dispatchers.IO) {
            try {
                val result = remoteService.refreshAccessToken(
                    RefreshTokenRequest(refreshToken = refreshToken)
                )

                when (result) {
                    is Resource.Success -> {
                        val newAccessToken = result.data.accessToken
                        val newRefreshToken = result.data.refreshToken

                        // Validate new tokens
                        if (newAccessToken.isEmpty()) {
                            Log.e(TAG, "❌ New access token is empty, clearing tokens")
                            tokenManager.clearTokens()
                            return@runBlocking null
                        }

                        // Save new tokens
                        tokenManager.updateTokens(newAccessToken, newRefreshToken)
                        Log.d(TAG, "✅ Token refreshed successfully")

                        // Retry original request with new token
                        response.request.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .build()
                    }

                    is Resource.Error -> {
                        Log.e(TAG, "❌ Refresh token failed: ${result.error.message}")
                        tokenManager.clearTokens()
                        null
                    }

                    else -> {
                        Log.e(TAG, "❌ Unexpected refresh token result")
                        tokenManager.clearTokens()
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception during token refresh: ${e.message}", e)
                tokenManager.clearTokens()
                null
            }
        }
    }
}