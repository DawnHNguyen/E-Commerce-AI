package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.ProductApi
import com.ptit.data.remote.dto.product.CreateProductRequestDto
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
    suspend fun getProductsByShop(createdById: String, isPublic: Boolean? = null) = remoteService.getProductsByShop(createdById, isPublic)
    suspend fun getCategories() = remoteService.getCategories()
    suspend fun deleteProduct(productId: String) = remoteService.deleteProduct(productId)
    suspend fun createProduct(request: CreateProductRequestDto) =
        remoteService.createProduct(request)

    suspend fun updateProduct(productId: String, request: UpdateProductRequestDto) =
        remoteService.updateProduct(productId, request)


}