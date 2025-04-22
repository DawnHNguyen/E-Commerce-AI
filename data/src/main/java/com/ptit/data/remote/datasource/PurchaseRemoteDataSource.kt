package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.PurchaseApi
import com.ptit.data.remote.dto.cart.UpdatePurchaseRequestDto
import javax.inject.Inject

class PurchaseRemoteDataSource @Inject constructor(private val remoteService: PurchaseApi) {
    suspend fun getPurchases(status: Int? = null) = remoteService.getPurchases(status)

    suspend fun updatePurchase(productId: String, buyCount: Int) =
        remoteService.updatePurchase(UpdatePurchaseRequestDto(productId, buyCount))

    suspend fun deletePurchases(purchaseIds: List<String>) =
        remoteService.deletePurchases(purchaseIds)
}