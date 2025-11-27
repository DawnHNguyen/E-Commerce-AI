package com.ptit.domain.repository

import com.ptit.domain.entity.auth.AuthToken
import com.ptit.domain.entity.auth.RegisterUser
import com.ptit.domain.utils.Resource

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
    ): Resource<AuthToken>

    suspend fun register(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        phoneNumber: String,
    ): Resource<RegisterUser>

    suspend fun logout(): Resource<Unit>
}