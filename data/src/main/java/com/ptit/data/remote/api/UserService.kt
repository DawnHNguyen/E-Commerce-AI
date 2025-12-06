package com.ptit.data.remote.api

import com.ptit.data.remote.dto.common.UserDto
import com.ptit.data.remote.dto.profile.ChangePasswordRequest
import com.ptit.data.remote.dto.profile.ChangePasswordResponse
import com.ptit.data.remote.dto.profile.UpdateProfileRequest
import com.ptit.data.remote.dto.profile.UpdateProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserService {
    @GET("profile")
    suspend fun getUserProfile(): UpdateProfileResponse

    @PUT("profile")
    suspend fun updateUserProfile(@Body request: UpdateProfileRequest): UpdateProfileResponse

    @PUT("profile/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ChangePasswordResponse
}