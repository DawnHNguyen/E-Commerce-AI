package com.ptit.data.remote.dto.profile

import com.google.gson.annotations.SerializedName

data class ChangePasswordRequest(
    @SerializedName("password")
    val currentPassword: String,
    @SerializedName("newPassword")
    val newPassword: String,
    @SerializedName("confirmNewPassword")
    val confirmNewPassword: String
)

