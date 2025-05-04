package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.ProductRemoteDataSource
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(private val remoteDataSource: ProductRemoteDataSource) : ProductRepository {

    override suspend fun getProductDetail(productId: String): Resource<ProductDomainEntity> {
        return remoteDataSource.getProductDetail(productId).map { it.toDomainEntity() }
    }

    override suspend fun getProductsByShop(): Resource<List<ProductDomainEntity>> {
        return remoteDataSource.getProductsByShop().map { response -> response.map { it.toDomainEntity() } }
    }
}