package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.PurchaseRemoteDataSource
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.entity.DeletePurchaseResult
import com.ptit.domain.repository.PurchaseRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class PurchaseRepositoryImpl @Inject constructor(
    private val remoteDataSource: PurchaseRemoteDataSource
) : PurchaseRepository {
    override suspend fun getPurchases(status: Int?): Resource<List<PurchaseDomainEntity>> {
        return remoteDataSource.getPurchases(status).map { purchaseDtoList ->
            purchaseDtoList.map { it.toDomainEntity() }
        }
    }

    override suspend fun updatePurchase(productId: String, buyCount: Int): Resource<PurchaseDomainEntity> {
        return remoteDataSource.updatePurchase(productId, buyCount)
            .map { it.toDomainEntity() }
    }

    override suspend fun deletePurchases(purchaseIds: List<String>): Resource<DeletePurchaseResult> {
        return remoteDataSource.deletePurchases(purchaseIds).map { response ->
            DeletePurchaseResult(deletedCount = response.deletedCount)
        }
    }

    override suspend fun addToCart(productId: String, buyCount: Int): Resource<PurchaseDomainEntity> {
        return remoteDataSource.addToCart(productId, buyCount)
            .map { it.toDomainEntity() }
    }

    override suspend fun getPurchaseById(id: String): Resource<PurchaseDomainEntity> {
        return remoteDataSource.getPurchaseById(id).map { purchaseDto ->
            purchaseDto.toDomainEntity()
        }
    }
}