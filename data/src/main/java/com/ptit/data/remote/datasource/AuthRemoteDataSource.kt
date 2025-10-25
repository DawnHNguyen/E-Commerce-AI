package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.AuthApi
import com.ptit.data.remote.dto.auth.LoginRequest
import com.ptit.data.remote.dto.auth.RefreshTokenRequest
import com.ptit.data.remote.dto.auth.RegisterRequest
import okhttp3.Request
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(private val remoteService: AuthApi) {
    suspend fun login(loginRequest: LoginRequest) =
        remoteService.login(loginRequest = loginRequest)

    suspend fun register(registerRequest: RegisterRequest) =
        remoteService.register(registerRequest = registerRequest)

    suspend fun logout(refreshTokenRequest: RefreshTokenRequest) = remoteService.logout(refreshTokenRequest)
}