package com.ptit.domain.repository

import com.ptit.domain.utils.Resource

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
    ): Resource<Unit>

    suspend fun register(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        phoneNumber: String,
    ): Resource<Unit>

    suspend fun logout(): Resource<Unit>
}