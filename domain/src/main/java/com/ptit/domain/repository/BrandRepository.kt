package com.ptit.domain.repository

import com.ptit.domain.entity.brand.BrandDomainEntity
import com.ptit.domain.entity.brand.BrandListDomainEntity
import com.ptit.domain.entity.brand.CreateBrandRequestDomainEntity
import com.ptit.domain.entity.brand.UpdateBrandRequestDomainEntity
import com.ptit.domain.utils.Resource

interface BrandRepository {
    
    /**
     * Get list of brands with pagination
     */
    suspend fun getBrands(
        page: Int = 1,
        limit: Int = 20
    ): Resource<BrandListDomainEntity>
    
    /**
     * Get brand detail by ID
     */
    suspend fun getBrandDetail(brandId: String): Resource<BrandDomainEntity>
    
    /**
     * Create new brand
     */
    suspend fun createBrand(request: CreateBrandRequestDomainEntity): Resource<BrandDomainEntity>
    
    /**
     * Update existing brand
     */
    suspend fun updateBrand(
        brandId: String,
        request: UpdateBrandRequestDomainEntity
    ): Resource<BrandDomainEntity>
    
    /**
     * Delete brand
     */
    suspend fun deleteBrand(brandId: String): Resource<Unit>
}

