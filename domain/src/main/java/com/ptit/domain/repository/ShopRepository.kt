package com.ptit.domain.repository

import com.ptit.domain.entity.shop.ShopDomainEntity
import com.ptit.domain.utils.Resource

interface ShopRepository {
    suspend fun getMyShop(): Resource<ShopDomainEntity>
}