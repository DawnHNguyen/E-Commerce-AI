package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.ProductApi
import com.ptit.data.remote.dto.product.CreateCategoryBodyDto
import com.ptit.data.remote.dto.product.CreateProductRequestDto
import com.ptit.data.remote.dto.product.UpdateCategoryBodyDto
import com.ptit.data.remote.dto.product.UpdateProductRequestDto
import javax.inject.Inject

class ProductRemoteDataSource @Inject constructor(private val remoteService: ProductApi) {
    suspend fun listProducts(
        page: Int,
        limit: Int,
        sortBy: String?,
        orderBy: String?,
        minPrice: Int?,
        maxPrice: Int?,
        name: String?,
        categories: List<String>?,
        brandIds: List<String>?
    ) = remoteService.listProducts(
        page = page,
        limit = limit,
        sortBy = sortBy,
        orderBy = orderBy,
        minPrice = minPrice,
        maxPrice = maxPrice,
        name = name,
        categories = categories,
        brandIds = brandIds
    )

    suspend fun getProductDetail(productId: String) = remoteService.getProductDetail(productId)
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
    ) = remoteService.getProductsByShop(createdById, page, limit, sortBy, sortOrder, searchQuery, minPrice, maxPrice, categoryId, brandId)
    suspend fun deleteProduct(productId: String) = remoteService.deleteProduct(productId)
    suspend fun createProduct(request: CreateProductRequestDto) =
        remoteService.createProduct(request)

    suspend fun updateProduct(productId: String, request: UpdateProductRequestDto) =
        remoteService.updateProduct(productId, request)

    // ==================== CATEGORY ====================

    suspend fun getAllCategories(parentCategoryId: String?) =
        remoteService.getAllCategories(parentCategoryId)

    suspend fun getCategoryById(categoryId: String) =
        remoteService.getCategoryById(categoryId)

    suspend fun createCategory(body: CreateCategoryBodyDto) =
        remoteService.createCategory(body)

    suspend fun updateCategory(categoryId: String, body: UpdateCategoryBodyDto) =
        remoteService.updateCategory(categoryId, body)

    suspend fun deleteCategory(categoryId: String) =
        remoteService.deleteCategory(categoryId)
}