package com.ptit.domain.repository

import com.ptit.domain.utils.Resource

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
    ): Resource<Unit>

    suspend fun register(
        password: String,
        email: String,
    ): Resource<Unit>

    suspend fun logout(): Resource<Unit>
}