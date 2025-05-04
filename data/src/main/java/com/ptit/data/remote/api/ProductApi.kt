package com.ptit.data.remote.api

import com.ptit.data.remote.dto.home.ListProductResponse
import com.ptit.data.remote.dto.product.CategoryDto
import com.ptit.data.remote.dto.product.CreateProductDto
import com.ptit.data.remote.dto.product.ProductDto
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.utils.Resource
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @GET("categories")
    suspend fun getCategories(): Resource<List<CategoryDto>>

    @DELETE("/admin/products/delete/{id_product}")
    suspend fun deleteProduct(
        @Path("id_product") productId: String
    ): Resource<Unit>

    @FormUrlEncoded
    @POST("/admin/products")
    suspend fun createProduct(
        @Field("name") name: String,
      @Field("description") description: String,
      @Field("price") price: Int,
      @Field("priceBeforeDiscount") priceBeforeDiscount: Int,
      @Field("quantity") quantity: Int,
      @Field("images") images: List<String>,
        @Field("image") image: String,
        @Field("category") category: String,
    ): Resource<ProductDto>

    @FormUrlEncoded
    @PUT("/admin/products/{id_product}")
    suspend fun updateProduct(
        @Path("id_product") productId: String,
        @Field("name") name: String,
        @Field("description") description: String,
        @Field("price") price: Int,
        @Field("priceBeforeDiscount") priceBeforeDiscount: Int,
        @Field("quantity") quantity: Int,
        @Field("images") images: List<String>,
        @Field("image") image: String,
        @Field("category") category: String,
    ): Resource<ProductDto>


}
