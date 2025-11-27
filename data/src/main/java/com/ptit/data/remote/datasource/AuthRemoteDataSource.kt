package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.AuthApi
import com.ptit.data.remote.dto.auth.LoginRequest
import com.ptit.data.remote.dto.auth.LoginResponse
import com.ptit.data.remote.dto.auth.RefreshTokenRequest
import com.ptit.data.remote.dto.auth.RegisterRequest
import com.ptit.data.remote.dto.auth.RegisterResponse
import com.ptit.domain.utils.Resource
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val api: AuthApi
) {
    suspend fun login(request: LoginRequest): Resource<LoginResponse> =
        api.login(request)

    suspend fun register(request: RegisterRequest): Resource<RegisterResponse> =
        api.register(request)

    suspend fun logout(request: RefreshTokenRequest): Resource<Unit> =
        api.logout(request)
}