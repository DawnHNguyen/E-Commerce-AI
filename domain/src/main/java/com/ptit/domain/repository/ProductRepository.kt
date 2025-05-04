package com.ptit.domain.repository

import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.utils.Resource

interface ProductRepository {
    suspend fun getProductDetail(productId: String): Resource<ProductDomainEntity>
    suspend fun getProductsByShop(): Resource<List<ProductDomainEntity>>
//    suspend fun deleteProduct(productId: String): Resource<ProductDomainEntity>
//    suspend fun updateProduct(productId: String, product: ProductDomainEntity): Resource<ProductDomainEntity>
}