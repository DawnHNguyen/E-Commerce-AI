package com.ptit.domain.repository

import com.ptit.domain.entity.shop.ShopDomainEntity
import com.ptit.domain.utils.Resource

interface ShopRepository {
    suspend fun getMyShop(): Resource<ShopDomainEntity>

    suspend fun createShop(
        name: String,
        description: String?,
        address: String?,
        phone: String?,
        avatar: String?
    ): Resource<ShopDomainEntity>

    suspend fun updateShop(
        name: String?,
        description: String?,
        address: String?,
        phone: String?,
        avatar: String?
    ): Resource<ShopDomainEntity>
}