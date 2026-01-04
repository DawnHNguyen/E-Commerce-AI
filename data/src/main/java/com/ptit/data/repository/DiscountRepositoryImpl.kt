package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.mapping.toDto
import com.ptit.data.remote.datasource.DiscountRemoteDataSource
import com.ptit.domain.entity.discount.*
import com.ptit.domain.repository.DiscountRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import javax.inject.Inject

class DiscountRepositoryImpl @Inject constructor(
    private val remoteDataSource: DiscountRemoteDataSource
) : DiscountRepository {

    override suspend fun getAvailableDiscounts(
        limit: Int,
        cartItemIds: List<String>?,
        onlyShopDiscounts: Boolean,
        onlyPlatformDiscounts: Boolean
    ): Resource<List<DiscountDomainEntity>> {
        return remoteDataSource.getAvailableDiscounts(
            limit = limit,
            cartItemIds = cartItemIds,
            onlyShopDiscounts = onlyShopDiscounts,
            onlyPlatformDiscounts = onlyPlatformDiscounts
        ).map { it.map { it.toDomainEntity() }}
    }

    override suspend fun validateVoucherCode(
        request: ValidateVoucherRequestDomainEntity
    ): Resource<ValidateVoucherResponseDomainEntity> {
        return remoteDataSource.validateVoucherCode(request.toDto())
            .map { it.toDomainEntity() }
    }

    override suspend fun applyVoucher(
        request: ApplyVoucherRequestDomainEntity
    ): Resource<ApplyVoucherResponseDomainEntity> {
        return remoteDataSource.applyVoucher(request.toDto())
            .map { it.toDomainEntity() }
    }

    override suspend fun getShopDiscounts(
        page: Int,
        limit: Int,
        name: String?,
        code: String?,
        discountStatus: String?,
        discountType: String?,
        createdById: String
    ): Resource<DiscountListDomainEntity> {
        return remoteDataSource.getShopDiscounts(
            page = page,
            limit = limit,
            name = name,
            code = code,
            discountStatus = discountStatus,
            discountType = discountType,
            createdById = createdById
        ).map { it.toDomainEntity() }
    }

    override suspend fun getDiscountDetail(
        discountId: String
    ): Resource<DiscountDomainEntity> {
        return remoteDataSource.getDiscountDetail(discountId)
            .map { it.toDomainEntity() }
    }

    override suspend fun createDiscount(
        request: CreateDiscountRequestDomainEntity
    ): Resource<DiscountDomainEntity> {
        return remoteDataSource.createDiscount(request.toDto())
            .map { it.toDomainEntity() }
    }

    override suspend fun updateDiscount(
        discountId: String,
        request: UpdateDiscountRequestDomainEntity
    ): Resource<DiscountDomainEntity> {
        return remoteDataSource.updateDiscount(discountId, request.toDto())
            .map { it.toDomainEntity() }
    }

    override suspend fun deleteDiscount(
        discountId: String
    ): Resource<Unit> {
        return remoteDataSource.deleteDiscount(discountId).map { }
    }
}