package com.ptit.data.remote.dto.product

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("logo")
    val logo: String?,
    @SerializedName("parentCategoryId")
    val parentCategoryId: String?,
    @SerializedName("createdById")
    val createdById: String?,
    @SerializedName("updatedById")
    val updatedById: String?,
    @SerializedName("deletedById")
    val deletedById: String?,
    @SerializedName("deletedAt")
    val deletedAt: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
)


data class GetAllCategoriesResDto(
    @SerializedName("data")
    val data: List<CategoryDto>?,
    @SerializedName("totalItems")
    val totalItems: Int?
)

data class CreateCategoryBodyDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("logo")
    val logo: String?,
    @SerializedName("parentCategoryId")
    val parentCategoryId: String?
)

data class UpdateCategoryBodyDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("logo")
    val logo: String?,
    @SerializedName("parentCategoryId")
    val parentCategoryId: String?
)

data class MessageResDto(
    @SerializedName("message")
    val message: String?
)
