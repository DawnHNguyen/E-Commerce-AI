package com.ptit.data.remote.api

import com.ptit.data.remote.dto.auth.LoginRequest
import com.ptit.data.remote.dto.auth.LoginResponse
import com.ptit.data.remote.dto.auth.RefreshTokenRequest
import com.ptit.data.remote.dto.auth.RegisterRequest
import com.ptit.domain.utils.Resource
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest,
    ): Resource<LoginResponse>

    @POST("/auth/register")
    suspend fun register(
        @Body registerRequest: RegisterRequest,
    ): Resource<LoginResponse>

    @POST("/auth/logout")
    suspend fun logout(
        @Body request: RefreshTokenRequest
    ): Resource<Unit>
}
