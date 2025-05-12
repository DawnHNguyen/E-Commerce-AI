package com.ptit.data.remote.api

import com.ptit.data.remote.dto.common.UserDto
import com.ptit.domain.utils.Resource
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserService {
    @GET("user")
    suspend fun getUserProfile(): Resource<UserDto>

    @PUT("user")
    suspend fun updateUserProfile(@Body userDto: UserDto): Resource<UserDto>
}
