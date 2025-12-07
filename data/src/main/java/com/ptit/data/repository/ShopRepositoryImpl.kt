package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.ShopRemoteDataSource
import com.ptit.data.remote.dto.shop.CreateShopRequest
import com.ptit.data.remote.dto.shop.UpdateShopRequest
import com.ptit.domain.entity.shop.ShopDomainEntity
import com.ptit.domain.repository.ShopRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import javax.inject.Inject

class ShopRepositoryImpl @Inject constructor(
    private val remoteDataSource: ShopRemoteDataSource
) : ShopRepository {
    override suspend fun getMyShop(): Resource<ShopDomainEntity> =
        remoteDataSource.getMyShop().map { it.data.toDomainEntity() }

    override suspend fun createShop(
        name: String,
        description: String?,
        address: String?,
        phone: String?,
        avatar: String?
    ): Resource<ShopDomainEntity> {
        val request = CreateShopRequest(
            name = name,
            description = description,
            address = address,
            phone = phone,
            avatar = avatar
        )
        return remoteDataSource.createShop(request).map { it.data.toDomainEntity() }
    }

    override suspend fun updateShop(
        name: String?,
        description: String?,
        address: String?,
        phone: String?,
        avatar: String?
    ): Resource<ShopDomainEntity> {
        val request = UpdateShopRequest(
            name = name,
            description = description,
            address = address,
            phone = phone,
            avatar = avatar
        )
        return remoteDataSource.updateShop(request).map { it.data.toDomainEntity() }
    }
}