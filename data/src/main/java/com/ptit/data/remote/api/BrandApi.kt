package com.ptit.data.remote.api

import com.ptit.data.remote.dto.brand.CreateBrandRequestDto
import com.ptit.data.remote.dto.brand.GetBrandDetailResponseDto
import com.ptit.data.remote.dto.brand.GetBrandsResponseDto
import com.ptit.data.remote.dto.brand.UpdateBrandRequestDto
import com.ptit.domain.utils.Resource
import retrofit2.http.*

interface BrandApi {

    /**
     * Get list of brands with pagination
     */
    @GET("brands")
    suspend fun getBrands(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Resource<GetBrandsResponseDto>

    /**
     * Get brand detail by ID
     */
    @GET("brands/{brandId}")
    suspend fun getBrandDetail(
        @Path("brandId") brandId: String
    ): Resource<GetBrandDetailResponseDto>

    /**
     * Create new brand (Admin only)
     */
    @POST("brands")
    suspend fun createBrand(
        @Body request: CreateBrandRequestDto
    ): Resource<GetBrandDetailResponseDto>

    /**
     * Update existing brand (Admin only)
     */
    @PUT("brands/{brandId}")
    suspend fun updateBrand(
        @Path("brandId") brandId: String,
        @Body request: UpdateBrandRequestDto
    ): Resource<GetBrandDetailResponseDto>

    /**
     * Delete brand (Admin only)
     */
    @DELETE("brands/{brandId}")
    suspend fun deleteBrand(
        @Path("brandId") brandId: String
    ): Resource<Unit>
}

