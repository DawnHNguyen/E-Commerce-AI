package com.ptit.domain.repository

import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.utils.Resource

interface ProductRepository {
    suspend fun getProductDetail(productId: String): Resource<ProductDomainEntity>
    suspend fun getProductsByShop(): Resource<List<ProductDomainEntity>>
    suspend fun getSimilarProducts(productId: String, amount: Int = 6): Resource<List<ProductDomainEntity>>
}