package com.ptit.data.remote.api

import com.ptit.data.remote.dto.home.ListProductResponse
import com.ptit.data.remote.dto.product.CategoryDto
import com.ptit.data.remote.dto.product.CreateCategoryBodyDto
import com.ptit.data.remote.dto.product.CreateProductRequestDto
import com.ptit.data.remote.dto.product.GetAllCategoriesResDto
import com.ptit.data.remote.dto.product.MessageResDto
import com.ptit.data.remote.dto.product.ProductDto
import com.ptit.data.remote.dto.product.UpdateCategoryBodyDto
import com.ptit.data.remote.dto.product.UpdateProductRequestDto
import com.ptit.domain.utils.Resource
import retrofit2.http.Body
import retrofit2.http.DELETE
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
        @Query("orderBy") orderBy: String?, // Thêm orderBy
        @Query("minPrice") minPrice: Int?, // Đổi từ price_min
        @Query("maxPrice") maxPrice: Int?, // Đổi từ price_max
        @Query("name") name: String?,
        @Query("categories") categories: List<String>?, // Đổi từ category sang categories (array)
        @Query("brandIds") brandIds: List<String>? // Thêm brandIds
    ): Resource<ListProductResponse>

    @GET("products/{id_product}")
    suspend fun getProductDetail(
        @Path("id_product") productId: String
    ): Resource<ProductDto>

    @GET("manage-product/products")
    suspend fun getProductsByShop(
        @Query("createdById") createdById: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortOrder") sortOrder: String = "desc",
        @Query("name") searchQuery: String? = null,
        @Query("minPrice") minPrice: Int? = null,
        @Query("maxPrice") maxPrice: Int? = null,
        @Query("categoryId") categoryId: String? = null,
        @Query("brandId") brandId: String? = null
    ): Resource<ListProductResponse>

    @DELETE("manage-product/products/{productId}")
    suspend fun deleteProduct(
        @Path("productId") productId: String
    ): Resource<Unit>

    @POST("manage-product/products")
    suspend fun createProduct(
        @Body product: CreateProductRequestDto
    ): Resource<ProductDto>

    @PUT("manage-product/products/{productId}")
    suspend fun updateProduct(
        @Path("productId") productId: String,
        @Body product: UpdateProductRequestDto
    ): Resource<ProductDto>

    // ==================== CATEGORY API ====================

    @GET("categories")
    suspend fun getAllCategories(
        @Query("parentCategoryId") parentCategoryId: String?
    ): Resource<GetAllCategoriesResDto>

    @GET("categories/{categoryId}")
    suspend fun getCategoryById(
        @Path("categoryId") categoryId: String
    ): Resource<CategoryDto>

    @POST("categories")
    suspend fun createCategory(
        @Body body: CreateCategoryBodyDto
    ): Resource<CategoryDto>

    @PUT("categories/{categoryId}")
    suspend fun updateCategory(
        @Path("categoryId") categoryId: String,
        @Body body: UpdateCategoryBodyDto
    ): Resource<CategoryDto>

    @DELETE("categories/{categoryId}")
    suspend fun deleteCategory(
        @Path("categoryId") categoryId: String
    ): Resource<MessageResDto>

//    @GET("https://recommend-system-323292678684.us-central1.run.app/recommendations/trending")
//    suspend fun getTrendingProducts(
//        @Query("amount") amount: Int,
//    ): Resource<List<ProductDto>>
//
//    @GET("https://recommend-system-323292678684.us-central1.run.app/recommendations/similar")
//    suspend fun getSimilarProducts(
//        @Query("product_id") productId: String,
//        @Query("amount") amount: Int
//    ): Resource<List<ProductDto>>
//
//    @GET("https://recommend-system-323292678684.us-central1.run.app/recommendations/home")
//    suspend fun getHomeRecommendations(
//        @Query("user_id") userId: String = MMKV.defaultMMKV().decodeString(SecureStorageKey.USER_ID)
//            .toString(),
//    ): Resource<List<ProductDto>>
}