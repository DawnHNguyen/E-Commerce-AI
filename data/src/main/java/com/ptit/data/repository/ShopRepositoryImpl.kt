package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.ShopRemoteDataSource
import com.ptit.domain.entity.shop.ShopDomainEntity
import com.ptit.domain.repository.ShopRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import javax.inject.Inject

class ShopRepositoryImpl @Inject  constructor(
    private val remoteDataSource: ShopRemoteDataSource
) : ShopRepository {
    override suspend fun getMyShop(): Resource<ShopDomainEntity> = remoteDataSource.getMyShop().map { it.toDomainEntity() }
}