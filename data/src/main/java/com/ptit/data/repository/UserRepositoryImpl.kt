package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.UserRemoteDataSource
import com.ptit.data.remote.dto.common.UserDto
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.repository.UserRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource,
) : UserRepository {
    override suspend fun getUserProfile(): Resource<UserDomainEntity> =
        remoteDataSource.getUserProfile().map { it.toDomainEntity() }

    override suspend fun updateUserProfile(
        name: String?,
        phoneNumber: String?,
        avatar: String?,
    ): Resource<Unit> {
        val request = UserDto(
            id = null,
            email = null,
            name = name,
            phoneNumber = phoneNumber,
            avatar = avatar,
            status = null,
            roleId = null,
            createdById = null,
            updatedById = null,
            deletedById = null,
            deletedAt = null,
            createdAt = null,
            updatedAt = null,
            role = null,
        )
        return remoteDataSource.updateUserProfile(request).map { }
    }
}
