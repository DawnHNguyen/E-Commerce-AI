package com.ptit.domain.repository

import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.CreateProductRequestDomainEntity
import com.ptit.domain.entity.product.GetAllCategoriesDomainEntity
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
    suspend fun getProductsByShop(
        createdById: String,
        page: Int = 1,
        limit: Int = 10,
        sortBy: String = "createdAt",
        sortOrder: String = "desc",
        searchQuery: String? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        categoryId: String? = null,
        brandId: String? = null
    ): Resource<List<ProductDomainEntity>>
    suspend fun createProduct(request: CreateProductRequestDomainEntity): Resource<ProductDomainEntity>
    suspend fun deleteProduct(productId: String): Resource<Unit>
    suspend fun updateProduct(productId: String, request: UpdateProductRequestDomainEntity): Resource<ProductDomainEntity>

    // ==================== CATEGORY ====================

    suspend fun getAllCategories(parentCategoryId: String? = null): Resource<GetAllCategoriesDomainEntity>
    suspend fun getCategoryById(categoryId: String): Resource<CategoryDomainEntity>
    suspend fun createCategory(name: String, logo: String?, parentCategoryId: String?): Resource<CategoryDomainEntity>
    suspend fun updateCategory(categoryId: String, name: String, logo: String?, parentCategoryId: String?): Resource<CategoryDomainEntity>
    suspend fun deleteCategory(categoryId: String): Resource<String>

    //
//    suspend fun getProductsByCategory(category: String): Resource<List<ProductDomainEntity>>
//    suspend fun getSimilarProducts(productId: String, amount: Int): Resource<List<ProductDomainEntity>>
//    suspend fun getTrendingProducts(amount: Int) : Resource<List<ProductDomainEntity>>
//    suspend fun getHomeRecommendations() : Resource<List<ProductDomainEntity>>
}