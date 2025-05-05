package com.ptit.data.remote.api

import com.ptit.data.remote.dto.home.ListProductResponse
import com.ptit.data.remote.dto.product.ProductDto
import com.ptit.domain.utils.Resource
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {
    @GET("products")
    suspend fun listProducts(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("sortBy") sortBy: String?,
        @Query("price_min") minPrice: Int?,
        @Query("price_max") maxPrice: Int?,
        @Query("rating") rating: Int?,
        @Query("name") name: String?
    ): Resource<ListProductResponse>

    @GET("products/{id_product}")
    suspend fun getProductDetail(
        @Path("id_product") productId: String
    ): Resource<ProductDto>

    @GET("/admin/products/my-products")
    suspend fun getProductsByShop(): Resource<List<ProductDto>>
}
