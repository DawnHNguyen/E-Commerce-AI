package com.ptit.data.remote.dto.recommendation

import com.google.gson.annotations.SerializedName
import com.ptit.data.remote.dto.brand.BrandDto
import com.ptit.data.remote.dto.product.CategoryDto

data class RecommendedProductDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("basePrice")
    val basePrice: Int,
    @SerializedName("virtualPrice")
    val virtualPrice: Int,
    @SerializedName("images")
    val images: List<String>,
    @SerializedName("categoryId")
    val categoryId: String?,
    @SerializedName("brandId")
    val brandId: String,
    @SerializedName("score")
    val score: Double,
    @SerializedName("brand")
    val brand: BrandDto,
    @SerializedName("category")
    val category: CategoryDto?
)

// The API response has nested data structure:
// { "data": { "data": [...products...], "total": 10, "message": "..." }, "statusCode": 200 }
data class RecommendationsDataWrapper(
    @SerializedName("data")
    val data: List<RecommendedProductDto>,  // Changed from 'products' to 'data' to match API
    @SerializedName("total")
    val total: Int,
    @SerializedName("message")
    val message: String
)

data class GetRecommendationsResponse(
    @SerializedName("data")
    val data: RecommendationsDataWrapper,
    @SerializedName("statusCode")
    val statusCode: Int
)


