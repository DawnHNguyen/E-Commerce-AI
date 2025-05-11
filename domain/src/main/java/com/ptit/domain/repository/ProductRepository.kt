package com.ptit.domain.repository

import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.utils.Resource


interface ProductRepository {
    suspend fun getProductDetail(productId: String): Resource<ProductDomainEntity>
    suspend fun getProductsByShop(): Resource<List<ProductDomainEntity>>
    suspend fun createProduct(name: String, description: String, price: Int, priceBeforeDiscount: Int, quantity: Int, images: List<String>, image:String, category:String): Resource<ProductDomainEntity>
    suspend fun deleteProduct(productId: String): Resource<Unit>
    suspend fun updateProduct(productId: String, name: String, description: String, price: Int, priceBeforeDiscount: Int, quantity: Int, images: List<String>, image:String, category:String): Resource<ProductDomainEntity>
    suspend fun getCategories(): Resource<List<CategoryDomainEntity>>

    //
    suspend fun getProductsByCategory(category: String): Resource<List<ProductDomainEntity>>
    suspend fun getSimilarProducts(productId: String, amount: Int ): Resource<List<ProductDomainEntity>>
    suspend fun getTrendingProducts(amount: Int) : Resource<List<ProductDomainEntity>>
    suspend fun getHomeRecommendations() : Resource<List<ProductDomainEntity>>
}