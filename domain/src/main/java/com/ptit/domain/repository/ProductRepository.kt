package com.ptit.domain.repository

import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.CreateProductRequestDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.entity.product.UpdateProductRequestDomainEntity
import com.ptit.domain.utils.Resource


interface ProductRepository {
    suspend fun listProducts(
        page: Int,
        limit: Int,
        sortBy: String? = null,
        orderBy: String? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        name: String? = null,
        categories: List<String>? = null,
        brandIds: List<String>? = null
    ): Resource<List<ProductDomainEntity>>

    suspend fun getProductDetail(productId: String): Resource<ProductDomainEntity>
    suspend fun getProductsByShop(createdById: String, isPublic: Boolean? = null): Resource<List<ProductDomainEntity>>
    suspend fun createProduct(request: CreateProductRequestDomainEntity): Resource<ProductDomainEntity>
    suspend fun deleteProduct(productId: String): Resource<Unit>
    suspend fun updateProduct(productId: String, request: UpdateProductRequestDomainEntity): Resource<ProductDomainEntity>
    suspend fun getCategories(): Resource<List<CategoryDomainEntity>>

    //
//    suspend fun getProductsByCategory(category: String): Resource<List<ProductDomainEntity>>
//    suspend fun getSimilarProducts(productId: String, amount: Int): Resource<List<ProductDomainEntity>>
//    suspend fun getTrendingProducts(amount: Int) : Resource<List<ProductDomainEntity>>
//    suspend fun getHomeRecommendations() : Resource<List<ProductDomainEntity>>
}