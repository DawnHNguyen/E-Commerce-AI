package com.ptit.domain.repository

import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.entity.DeletePurchaseResult
import com.ptit.domain.utils.Resource

interface PurchaseRepository {
    suspend fun getPurchases(status: Int? = null): Resource<List<PurchaseDomainEntity>>
    suspend fun updatePurchase(productId: String, buyCount: Int): Resource<PurchaseDomainEntity>
    suspend fun addToCart(productId: String, buyCount: Int): Resource<PurchaseDomainEntity>
    suspend fun deletePurchases(purchaseIds: List<String>): Resource<DeletePurchaseResult>

    // Add new method to get purchases by their IDs
    suspend fun getPurchasesByIds(purchaseIds: List<String>): List<PurchaseDomainEntity>
}
