package com.ptit.domain.repository

import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.utils.Resource

interface PurchaseRepository {
    suspend fun getPurchases(status: Int? = null): Resource<List<PurchaseDomainEntity>>
}