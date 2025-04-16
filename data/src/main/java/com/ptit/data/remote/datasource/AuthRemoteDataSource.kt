package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.AuthApi
import com.ptit.data.remote.dto.auth.LoginAndRegisterRequest
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(private val remoteService: AuthApi) {
    suspend fun login(loginRequest: LoginAndRegisterRequest) =
        remoteService.login(loginRequest = loginRequest)

    suspend fun register(registerRequest: LoginAndRegisterRequest) =
        remoteService.register(registerRequest = registerRequest)

    suspend fun logout() = remoteService.logout()
}