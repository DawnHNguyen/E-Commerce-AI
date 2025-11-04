package com.ptit.domain.entity.auth

data class AuthToken(
    val accessToken: String,
    val refreshToken: String
)

data class RegisterUser(
    val id: String,
    val email: String,
    val name: String,
    val phoneNumber: String,
    val roleId: String,
    val isTwoFactorEnabled: Boolean,
    val createdAt: String,
    val updatedAt: String
)