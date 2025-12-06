package com.ptit.data.repository

import com.ptit.common.const.SecureStorageKey
import com.ptit.data.mapping.auth.toDomain
import com.ptit.data.remote.datasource.AuthRemoteDataSource
import com.ptit.data.remote.dto.auth.LoginRequest
import com.ptit.data.remote.dto.auth.RefreshTokenRequest
import com.ptit.data.remote.dto.auth.RegisterRequest
import com.ptit.domain.entity.auth.AuthToken
import com.ptit.domain.entity.auth.RegisterUser
import com.ptit.domain.repository.AuthRepository
import com.ptit.domain.utils.CustomException
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import com.ptit.domain.utils.onSuccess
import com.tencent.mmkv.MMKV
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    private val mmkv = MMKV.defaultMMKV()

    override suspend fun login(email: String, password: String): Resource<AuthToken> {
        return remoteDataSource
            .login(LoginRequest(email, password))
            .map { it.toDomain() }
            .onSuccess { token ->
                mmkv.putString(SecureStorageKey.ACCESS_TOKEN, token.accessToken)
                mmkv.putString(SecureStorageKey.REFRESH_TOKEN, token.refreshToken)
                mmkv.sync()
            }
    }

    override suspend fun register(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        phoneNumber: String
    ): Resource<RegisterUser> {
        return remoteDataSource
            .register(RegisterRequest(email, password, confirmPassword, name, phoneNumber))
            .map { it.toDomain() }
    }

    override suspend fun logout(): Resource<Unit> {
        val refreshToken = mmkv.decodeString(SecureStorageKey.REFRESH_TOKEN)

        // ✅ Always clear local tokens first
        mmkv.removeValuesForKeys(
            arrayOf(
                SecureStorageKey.ACCESS_TOKEN,
                SecureStorageKey.REFRESH_TOKEN
            )
        )
        mmkv.commit()

        // ✅ If no refresh token, already logged out locally
        if (refreshToken.isNullOrEmpty()) {
            return Resource.success(Unit)
        }

        // ✅ Try to call logout API, but don't fail if it errors
        // This is because we've already cleared local tokens
        return try {
            remoteDataSource.logout(RefreshTokenRequest(refreshToken))
            // If API succeeds, great!
            Resource.success(Unit)
        } catch (e: Exception) {
            // ✅ If API fails (500 error, network error, etc.), still return success
            // because we've already logged out locally
            Resource.success(Unit)
        }
    }
}