package com.ptit.data.repository

import com.ptit.common.const.SecureStorageKey
import com.ptit.data.remote.datasource.AuthRemoteDataSource
import com.ptit.data.remote.dto.auth.LoginRequest
import com.ptit.data.remote.dto.auth.RefreshTokenRequest
import com.ptit.data.remote.dto.auth.RegisterRequest
import com.ptit.domain.repository.AuthRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import com.ptit.domain.utils.onSuccess
import com.tencent.mmkv.MMKV
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val remoteDataSource: AuthRemoteDataSource) : AuthRepository {
    val mmkv = MMKV.defaultMMKV()
    override suspend fun login(email: String, password: String): Resource<Unit> {
        val request = LoginRequest(
            email = email,
            password = password
        )

        return remoteDataSource
            .login(request)
            .onSuccess {
                mmkv.putString(SecureStorageKey.ACCESS_TOKEN, it.accessToken)
                mmkv.putString(SecureStorageKey.REFRESH_TOKEN, it.refreshToken)
            }
            .map { }
    }

    override suspend fun register(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        phoneNumber: String,
    ): Resource<Unit> {
        val request = RegisterRequest(
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            name = name,
            phoneNumber = phoneNumber,
        )

        return remoteDataSource
            .register(request)
            .map { }
    }

    override suspend fun logout(): Resource<Unit> {
        val refreshToken = mmkv.decodeString(SecureStorageKey.REFRESH_TOKEN)

        if (refreshToken.isNullOrEmpty()) {
            mmkv.removeValuesForKeys(
                arrayOf(
                    SecureStorageKey.ACCESS_TOKEN,
                    SecureStorageKey.REFRESH_TOKEN,
                )
            )
            return Resource.Success(Unit)
        }

        val request = RefreshTokenRequest(refreshToken = refreshToken)

        return remoteDataSource
            .logout(request)
            .onSuccess {
                mmkv.removeValuesForKeys(
                    arrayOf(
                        SecureStorageKey.ACCESS_TOKEN,
                        SecureStorageKey.REFRESH_TOKEN,
                    )
                )
            }
            .map { }

    }

}