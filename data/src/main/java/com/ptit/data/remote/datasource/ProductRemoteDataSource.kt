package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.ProductApi
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

    // Hàm mới để lấy tất cả sản phẩm
    suspend fun getAllProducts(limit: Int = 1000) = remoteService.listProducts(
        page = 1,
        limit = limit,
        sortBy = null,
        minPrice = null,
        maxPrice = null,
        rating = null,
        name = null
    )

    suspend fun getProductDetail(productId: String) = remoteService.getProductDetail(productId)
    suspend fun getProductsByShop() = remoteService.getProductsByShop()
    suspend fun getCategories() = remoteService.getCategories()
    suspend fun deleteProduct(productId: String) = remoteService.deleteProduct(productId)

    suspend fun getTrendingProducts(amount: Int = 6) = remoteService.getTrendingProducts(amount = amount)

    suspend fun createProduct(name: String, description: String, price: Int, priceBeforeDiscount: Int, quantity: Int, images: List<String>, image: String, category: String) = remoteService.createProduct(name, description, price, priceBeforeDiscount, quantity, images, image, category )
    suspend fun updateProduct(productId: String, name: String, description: String, price: Int, priceBeforeDiscount: Int, quantity: Int, images: List<String>, image:String, category:String) = remoteService.updateProduct(productId, name, description, price, priceBeforeDiscount, quantity, images, image, category)

    suspend fun getSimilarProducts(productId: String, amount: Int = 6) =
        remoteService.getSimilarProducts(productId, amount)
}