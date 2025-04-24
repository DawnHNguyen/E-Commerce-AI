package com.ptit.data.remote.api

import com.ptit.data.remote.dto.common.UserDto
import com.ptit.domain.utils.Resource
import retrofit2.http.GET

interface UserService {
    @GET("user")
    suspend fun getUserProfile(): Resource<UserDto>
}
