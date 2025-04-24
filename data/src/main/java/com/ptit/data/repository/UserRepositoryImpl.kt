package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.UserRemoteDataSource
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
}
