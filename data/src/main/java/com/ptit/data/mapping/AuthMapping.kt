package com.ptit.data.mapping.auth

import com.ptit.data.remote.dto.auth.LoginResponse
import com.ptit.data.remote.dto.auth.RegisterResponse
import com.ptit.domain.entity.auth.AuthToken
import com.ptit.domain.entity.auth.RegisterUser

fun LoginResponse.toDomain() = AuthToken(
    accessToken = accessToken,
    refreshToken = refreshToken
)

fun RegisterResponse.toDomain() = RegisterUser(
    id = id,
    email = email,
    name = name,
    phoneNumber = phoneNumber,
    roleId = roleId,
    isTwoFactorEnabled = isTwoFactorEnabled,
    createdAt = createdAt,
    updatedAt = updatedAt
)