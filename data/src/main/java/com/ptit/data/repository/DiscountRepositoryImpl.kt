package com.ptit.data.repository

import com.ptit.data.remote.api.DiscountApi
import com.ptit.data.remote.mapper.toDomainEntity
import com.ptit.data.remote.mapper.toDto
import com.ptit.domain.entity.discount.*
import com.ptit.domain.repository.DiscountRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onSuccess
import javax.inject.Inject

class DiscountRepositoryImpl @Inject constructor(
    private val discountApi: DiscountApi
) : DiscountRepository {

    override suspend fun getAvailableDiscounts(
        limit: Int,
        cartItemIds: List<String>?,
        onlyShopDiscounts: Boolean,
        onlyPlatformDiscounts: Boolean
    ): Resource<DiscountListDomainEntity> {
        var result: Resource<DiscountListDomainEntity> = Resource.idle()

        discountApi.getAvailableDiscounts(
            limit = limit,
            cartItemIds = cartItemIds,
            onlyShopDiscounts = onlyShopDiscounts,
            onlyPlatformDiscounts = onlyPlatformDiscounts
        )
            .onSuccess { response ->
                result = Resource.success(response.toDomainEntity())
            }
            .onError { error ->
                result = Resource.error(error)
            }

        return result
    }

    override suspend fun validateVoucherCode(
        request: ValidateVoucherRequestDomainEntity
    ): Resource<ValidateVoucherResponseDomainEntity> {
        var result: Resource<ValidateVoucherResponseDomainEntity> = Resource.idle()

        discountApi.validateVoucherCode(request.toDto())
            .onSuccess { response ->
                result = Resource.success(response.toDomainEntity())
            }
            .onError { error ->
                result = Resource.error(error)
            }

        return result
    }

    override suspend fun applyVoucher(
        request: ApplyVoucherRequestDomainEntity
    ): Resource<ApplyVoucherResponseDomainEntity> {
        var result: Resource<ApplyVoucherResponseDomainEntity> = Resource.idle()

        discountApi.applyVoucher(request.toDto())
            .onSuccess { response ->
                result = Resource.success(response.toDomainEntity())
            }
            .onError { error ->
                result = Resource.error(error)
            }

        return result
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
        var result: Resource<DiscountListDomainEntity> = Resource.idle()

        discountApi.getShopDiscounts(
            page = page,
            limit = limit,
            name = name,
            code = code,
            discountStatus = discountStatus,
            discountType = discountType,
            createdById = createdById
        )
            .onSuccess { response ->
                result = Resource.success(response.toDomainEntity())
            }
            .onError { error ->
                result = Resource.error(error)
            }

        return result
    }

    override suspend fun getDiscountDetail(
        discountId: String
    ): Resource<DiscountDomainEntity> {
        var result: Resource<DiscountDomainEntity> = Resource.idle()

        discountApi.getDiscountDetail(discountId)
            .onSuccess { response ->
                result = Resource.success(response.toDomainEntity())
            }
            .onError { error ->
                result = Resource.error(error)
            }

        return result
    }

    override suspend fun createDiscount(
        request: CreateDiscountRequestDomainEntity
    ): Resource<DiscountDomainEntity> {
        var result: Resource<DiscountDomainEntity> = Resource.idle()

        discountApi.createDiscount(request.toDto())
            .onSuccess { response ->
                result = Resource.success(response.toDomainEntity())
            }
            .onError { error ->
                result = Resource.error(error)
            }

        return result
    }

    override suspend fun updateDiscount(
        discountId: String,
        request: UpdateDiscountRequestDomainEntity
    ): Resource<DiscountDomainEntity> {
        var result: Resource<DiscountDomainEntity> = Resource.idle()

        discountApi.updateDiscount(discountId, request.toDto())
            .onSuccess { response ->
                result = Resource.success(response.toDomainEntity())
            }
            .onError { error ->
                result = Resource.error(error)
            }

        return result
    }

    override suspend fun deleteDiscount(
        discountId: String
    ): Resource<Unit> {
        var result: Resource<Unit> = Resource.idle()

        discountApi.deleteDiscount(discountId)
            .onSuccess {
                result = Resource.success(Unit)
            }
            .onError { error ->
                result = Resource.error(error)
            }

        return result
    }
}

