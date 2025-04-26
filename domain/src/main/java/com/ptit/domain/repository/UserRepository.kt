package com.ptit.domain.repository

import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.utils.Resource

interface UserRepository {
    suspend fun getUserProfile(): Resource<UserDomainEntity>
}
