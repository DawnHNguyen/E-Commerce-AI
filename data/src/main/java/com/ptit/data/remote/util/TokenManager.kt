package com.ptit.data.remote.util

import android.util.Log
import com.ptit.common.const.SecureStorageKey
import com.tencent.mmkv.MMKV
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TokenManager - Quản lý Access Token và Refresh Token
 *
 * Responsibilities:
 * - Lưu/lấy/xóa tokens từ MMKV
 * - Kiểm tra token validity
 * - Provide helper methods cho auth flow
 */
@Singleton
class TokenManager @Inject constructor() {

    private val mmkv: MMKV by lazy { MMKV.defaultMMKV() }

    companion object {
        private const val TAG = "TokenManager"
    }

    /**
     * Lưu cặp token sau khi login/register thành công
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        Log.d(TAG, "Saving tokens...")
        mmkv.encode(SecureStorageKey.ACCESS_TOKEN, accessToken)
        mmkv.encode(SecureStorageKey.REFRESH_TOKEN, refreshToken)
    }

    /**
     * Lấy access token hiện tại
     */
    fun getAccessToken(): String? {
        return mmkv.decodeString(SecureStorageKey.ACCESS_TOKEN)
    }

    /**
     * Lấy refresh token hiện tại
     */
    fun getRefreshToken(): String? {
        return mmkv.decodeString(SecureStorageKey.REFRESH_TOKEN)
    }

    /**
     * Kiểm tra có token hay không
     */
    fun hasValidTokens(): Boolean {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()
        return !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()
    }

    /**
     * Xóa tất cả tokens (dùng khi logout hoặc token hết hạn)
     */
    fun clearTokens() {
        Log.d(TAG, "Clearing all tokens...")
        mmkv.removeValuesForKeys(
            arrayOf(
                SecureStorageKey.ACCESS_TOKEN,
                SecureStorageKey.REFRESH_TOKEN
            )
        )
    }

    /**
     * Cập nhật tokens mới sau khi refresh thành công
     */
    fun updateTokens(newAccessToken: String, newRefreshToken: String) {
        Log.d(TAG, "Updating tokens after refresh...")
        saveTokens(newAccessToken, newRefreshToken)
    }

    /**
     * Kiểm tra access token có tồn tại không (không kiểm tra hết hạn)
     */
    fun hasAccessToken(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }

    /**
     * Kiểm tra refresh token có tồn tại không
     */
    fun hasRefreshToken(): Boolean {
        return !getRefreshToken().isNullOrBlank()
    }
}

