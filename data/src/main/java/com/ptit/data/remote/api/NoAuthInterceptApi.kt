package com.ptit.data.remote.api

import com.ptit.data.remote.dto.auth.LoginAndRegisterResponse
import com.ptit.data.remote.dto.auth.RefreshTokenRequest
import com.ptit.domain.utils.Resource
import retrofit2.http.Body
import retrofit2.http.POST

interface NoAuthInterceptApi {

    @POST("refresh-access-token")
    suspend fun refreshAccessToken(
        @Body refreshTokenRequest: RefreshTokenRequest
    ): Resource<LoginAndRegisterResponse>

}