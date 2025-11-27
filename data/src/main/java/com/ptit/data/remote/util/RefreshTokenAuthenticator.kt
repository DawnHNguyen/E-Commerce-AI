package com.ptit.data.remote.util

import com.ptit.common.const.SecureStorageKey
import com.ptit.data.remote.api.NoAuthInterceptApi
import com.ptit.data.remote.dto.auth.RefreshTokenRequest
import com.ptit.domain.utils.Resource
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class RefreshTokenAuthenticator @Inject constructor(
    private val remoteService: NoAuthInterceptApi,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val mmkv = MMKV.defaultMMKV()
        val refreshToken = mmkv.decodeString(SecureStorageKey.REFRESH_TOKEN) ?: return null

        return runBlocking(Dispatchers.IO) {
            val newToken = remoteService.refreshAccessToken(RefreshTokenRequest(refreshToken = refreshToken))

            if (newToken is Resource.Success) {
                val newAccessToken = newToken.data.accessToken
                if (newAccessToken.isEmpty()) {
                    mmkv.removeValuesForKeys(arrayOf(SecureStorageKey.ACCESS_TOKEN, SecureStorageKey.REFRESH_TOKEN))
                    return@runBlocking null
                }
                val newRefreshToken = newToken.data.refreshToken
                mmkv.encode(SecureStorageKey.ACCESS_TOKEN, newAccessToken)
                mmkv.encode(SecureStorageKey.REFRESH_TOKEN, newRefreshToken)
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                mmkv.removeValuesForKeys(arrayOf(SecureStorageKey.ACCESS_TOKEN, SecureStorageKey.REFRESH_TOKEN))
                null
            }
        }
    }
}