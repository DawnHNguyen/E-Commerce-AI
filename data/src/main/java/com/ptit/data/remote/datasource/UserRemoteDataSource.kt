package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.UserService
import com.ptit.data.remote.dto.profile.ChangePasswordRequest
import com.ptit.data.remote.dto.profile.UpdateProfileRequest
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(private val remoteService: UserService) {
    suspend fun getUserProfile() = remoteService.getUserProfile()

    suspend fun updateUserProfile(
        name: String,
        phoneNumber: String,
        avatar: String
    ) = remoteService.updateUserProfile(
        UpdateProfileRequest(
            name = name,
            phoneNumber = phoneNumber,
            avatar = avatar
        )
    )

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ) = remoteService.changePassword(
        ChangePasswordRequest(
            currentPassword = currentPassword,
            newPassword = newPassword,
            confirmNewPassword = confirmNewPassword
        )
    )
}
