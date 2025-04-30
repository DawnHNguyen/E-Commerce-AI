package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.api.PurchaseApi
import com.ptit.data.remote.datasource.PurchaseRemoteDataSource
import com.ptit.data.remote.dto.cart.AddToCartRequestDto
import com.ptit.data.remote.dto.cart.UpdatePurchaseRequestDto
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.entity.DeletePurchaseResult
import com.ptit.domain.repository.PurchaseRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class PurchaseRepositoryImpl @Inject constructor(
    private val purchaseApi: PurchaseApi
) : PurchaseRepository {
    override suspend fun getPurchases(status: Int?): Resource<List<PurchaseDomainEntity>> {
        return purchaseApi.getPurchases(status).map { purchaseDtoList ->
            purchaseDtoList.map { it.toDomainEntity() }
        }
    }

    override suspend fun updatePurchase(productId: String, buyCount: Int): Resource<PurchaseDomainEntity> {
        return purchaseApi.updatePurchase(UpdatePurchaseRequestDto(productId, buyCount))
            .map { it.toDomainEntity() }
    }

    override suspend fun deletePurchases(purchaseIds: List<String>): Resource<DeletePurchaseResult> {
        return purchaseApi.deletePurchases(purchaseIds).map { response ->
            DeletePurchaseResult(deletedCount = response.deletedCount)
        }
    }

    override suspend fun addToCart(productId: String, buyCount: Int): Resource<PurchaseDomainEntity> {
        return purchaseApi.addToCart(AddToCartRequestDto(productId, buyCount))
            .map { it.toDomainEntity() }
    }

    override suspend fun getPurchasesByIds(purchaseIds: List<String>): List<PurchaseDomainEntity> {
        // Use coroutineScope to parallelize the API calls for better performance
        return coroutineScope {
            val deferredResults = purchaseIds.map { purchaseId ->
                async {
                    val result = purchaseApi.getPurchaseById(purchaseId)
                    if (result is Resource.Success) {
                        result.data.toDomainEntity()
                    } else {
                        null
                    }
                }
            }

            // Await all results and filter out nulls
            deferredResults.mapNotNull { it.await() }
        }
    }
}
