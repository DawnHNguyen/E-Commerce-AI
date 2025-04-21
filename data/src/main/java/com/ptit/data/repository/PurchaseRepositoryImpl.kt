package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.PurchaseRemoteDataSource
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.repository.PurchaseRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import javax.inject.Inject

class PurchaseRepositoryImpl @Inject constructor(
    private val remoteDataSource: PurchaseRemoteDataSource
) : PurchaseRepository {

    override suspend fun getPurchases(status: Int?): Resource<List<PurchaseDomainEntity>> {
        return remoteDataSource.getPurchases(status).map { response ->
            response.data?.map { it.toDomainEntity() } ?: emptyList()
        }
    }
}