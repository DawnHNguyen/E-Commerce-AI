package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.ProductApi
import com.ptit.data.remote.dto.product.CreateProductDto
import javax.inject.Inject

class ProductRemoteDataSource @Inject constructor(private val remoteService: ProductApi) {
    suspend fun listProducts(
        page: Int,
        limit: Int,
        sortBy: String?,
        minPrice: Int?,
        maxPrice: Int?,
        rating: Int?,
        name: String?
    ) = remoteService.listProducts(
        page = page,
        limit = limit,
        sortBy = sortBy,
        minPrice = minPrice,
        maxPrice = maxPrice,
        rating = rating,
        name = name
    )

    suspend fun getProductDetail(productId: String) = remoteService.getProductDetail(productId)
    suspend fun getProductsByShop() = remoteService.getProductsByShop()
    suspend fun getCategories() = remoteService.getCategories()

    suspend fun createProduct(name: String, description: String, price: Int, priceBeforeDiscount: Int, quantity: Int, images: List<String>, image: String, category: String) = remoteService.createProduct(name, description, price, priceBeforeDiscount, quantity, images, image, category )
    suspend fun updateProduct(productId: String, name: String, description: String, price: Int, priceBeforeDiscount: Int, quantity: Int, images: List<String>, image:String, category:String) = remoteService.updateProduct(productId, name, description, price, priceBeforeDiscount, quantity, images, image, category)
}
