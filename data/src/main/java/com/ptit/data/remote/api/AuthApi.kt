package com.ptit.data.remote.api

import com.ptit.data.remote.dto.auth.LoginAndRegisterRequest
import com.ptit.data.remote.dto.auth.LoginAndRegisterResponse
import com.ptit.domain.utils.Resource
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(
        @Body loginRequest: LoginAndRegisterRequest,
    ): Resource<LoginAndRegisterResponse>

    @POST("register")
    suspend fun register(
        @Body registerRequest: LoginAndRegisterRequest,
    ): Resource<LoginAndRegisterResponse>

    @POST("logout")
    suspend fun logout(): Resource<Unit>
}
