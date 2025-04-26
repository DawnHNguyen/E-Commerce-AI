package com.ptit.data.repository

import com.ptit.common.const.SecureStorageKey
import com.ptit.data.remote.datasource.AuthRemoteDataSource
import com.ptit.data.remote.dto.auth.LoginAndRegisterRequest
import com.ptit.domain.repository.AuthRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import com.ptit.domain.utils.onSuccess
import com.tencent.mmkv.MMKV
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val remoteDataSource: AuthRemoteDataSource) : AuthRepository {
    override suspend fun login(email: String, password: String): Resource<Unit> {
        val mmkv = MMKV.defaultMMKV()
        val request = LoginAndRegisterRequest(
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

    override suspend fun register(password: String, email: String): Resource<Unit> {
        val mmkv = MMKV.defaultMMKV()
        val request = LoginAndRegisterRequest(
            email = email,
            password = password
        )

        return remoteDataSource
            .register(request)
            .onSuccess {
                mmkv.putString(SecureStorageKey.ACCESS_TOKEN, it.accessToken)
                mmkv.putString(SecureStorageKey.REFRESH_TOKEN, it.refreshToken)
            }
            .map { }
    }

    override suspend fun logout(): Resource<Unit> =
        remoteDataSource
            .logout()
            .onSuccess {
                MMKV.defaultMMKV().remove(SecureStorageKey.ACCESS_TOKEN)
                MMKV.defaultMMKV().remove(SecureStorageKey.REFRESH_TOKEN)
            }
            .map { }
}