package com.ptit.data.remote.dto.brand

import com.google.gson.annotations.SerializedName

data class BrandDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("logo")
    val logo: String,

    @SerializedName("createdById")
    val createdById: String? = null,

    @SerializedName("updatedById")
    val updatedById: String? = null,

    @SerializedName("deletedById")
    val deletedById: String? = null,

    @SerializedName("deletedAt")
    val deletedAt: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("discountId")
    val discountId: String? = null
)

data class GetBrandsResponseDto(
    @SerializedName("data")
    val data: List<BrandDto>,

    @SerializedName("totalItems")
    val totalItems: Int,

    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("statusCode")
    val statusCode: Int
)

data class GetBrandDetailResponseDto(
    @SerializedName("data")
    val data: BrandDto,

    @SerializedName("statusCode")
    val statusCode: Int
)

data class CreateBrandRequestDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("logo")
    val logo: String
)

data class UpdateBrandRequestDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("logo")
    val logo: String
)

