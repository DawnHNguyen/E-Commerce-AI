package com.ptit.data.repository

import com.ptit.data.remote.api.BrandApi
import com.ptit.data.remote.mapper.toDomainEntity
import com.ptit.data.remote.mapper.toDto
import com.ptit.domain.entity.brand.BrandDomainEntity
import com.ptit.domain.entity.brand.BrandListDomainEntity
import com.ptit.domain.entity.brand.CreateBrandRequestDomainEntity
import com.ptit.domain.entity.brand.UpdateBrandRequestDomainEntity
import com.ptit.domain.repository.BrandRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onSuccess
import javax.inject.Inject

class BrandRepositoryImpl @Inject constructor(
    private val brandApi: BrandApi
) : BrandRepository {

    override suspend fun getBrands(
        page: Int,
        limit: Int
    ): Resource<BrandListDomainEntity> {
        var result: Resource<BrandListDomainEntity> = Resource.idle()

        brandApi.getBrands(page, limit)
            .onSuccess { response ->
                result = Resource.success(response.toDomainEntity())
            }
            .onError { error ->
                result = Resource.error(error)
            }

        return result
    }

    override suspend fun getBrandDetail(brandId: String): Resource<BrandDomainEntity> {
        var result: Resource<BrandDomainEntity> = Resource.idle()

        brandApi.getBrandDetail(brandId)
            .onSuccess { response ->
                result = Resource.success(response.toDomainEntity())
            }
            .onError { error ->
                result = Resource.error(error)
            }

        return result
    }

    override suspend fun createBrand(
        request: CreateBrandRequestDomainEntity
    ): Resource<BrandDomainEntity> {
        var result: Resource<BrandDomainEntity> = Resource.idle()

        brandApi.createBrand(request.toDto())
            .onSuccess { response ->
                result = Resource.success(response.toDomainEntity())
            }
            .onError { error ->
                result = Resource.error(error)
            }

        return result
    }

    override suspend fun updateBrand(
        brandId: String,
        request: UpdateBrandRequestDomainEntity
    ): Resource<BrandDomainEntity> {
        var result: Resource<BrandDomainEntity> = Resource.idle()

        brandApi.updateBrand(brandId, request.toDto())
            .onSuccess { response ->
                result = Resource.success(response.toDomainEntity())
            }
            .onError { error ->
                result = Resource.error(error)
            }

        return result
    }

    override suspend fun deleteBrand(brandId: String): Resource<Unit> {
        var result: Resource<Unit> = Resource.idle()

        brandApi.deleteBrand(brandId)
            .onSuccess {
                result = Resource.success(Unit)
            }
            .onError { error ->
                result = Resource.error(error)
            }

        return result
    }
}

