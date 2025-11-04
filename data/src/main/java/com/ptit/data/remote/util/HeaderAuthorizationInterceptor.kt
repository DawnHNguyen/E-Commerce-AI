package com.ptit.data.remote.util

import com.tencent.mmkv.MMKV
import com.ptit.common.const.SecureStorageKey
import okhttp3.Interceptor
import okhttp3.Response

class HeaderAuthorizationInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val mmkv = MMKV.defaultMMKV()
        val jwt = mmkv.decodeString(SecureStorageKey.ACCESS_TOKEN, null)

        val requestBuilder = chain.request().newBuilder()
        if (!jwt.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $jwt")
        } else {
            println("⚠️ No access token found, skip Authorization header")
        }

        return chain.proceed(requestBuilder.build())
    }
}