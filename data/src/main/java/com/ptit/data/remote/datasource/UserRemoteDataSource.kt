package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.UserService
import com.ptit.data.remote.dto.common.UserDto
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(private val remoteService: UserService) {
    suspend fun getUserProfile() = remoteService.getUserProfile()

    suspend fun updateUserProfile(userDto: UserDto) =
        remoteService.updateUserProfile(userDto = userDto)
}
