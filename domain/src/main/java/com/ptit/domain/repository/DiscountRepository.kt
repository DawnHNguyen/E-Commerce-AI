package com.ptit.domain.repository

import com.ptit.domain.entity.discount.ApplyVoucherRequestDomainEntity
import com.ptit.domain.entity.discount.ApplyVoucherResponseDomainEntity
import com.ptit.domain.entity.discount.CreateDiscountRequestDomainEntity
import com.ptit.domain.entity.discount.DiscountDomainEntity
import com.ptit.domain.entity.discount.DiscountListDomainEntity
import com.ptit.domain.entity.discount.UpdateDiscountRequestDomainEntity
import com.ptit.domain.entity.discount.ValidateVoucherRequestDomainEntity
import com.ptit.domain.entity.discount.ValidateVoucherResponseDomainEntity
import com.ptit.domain.utils.Resource

interface DiscountRepository {

    /**
     * Get available discounts for checkout
     */
    suspend fun getAvailableDiscounts(
        limit: Int = 20,
        cartItemIds: List<String>? = null,
        onlyShopDiscounts: Boolean = false,
        onlyPlatformDiscounts: Boolean = false
    ): Resource<DiscountListDomainEntity>

    /**
     * Validate voucher code
     */
    suspend fun validateVoucherCode(
        request: ValidateVoucherRequestDomainEntity
    ): Resource<ValidateVoucherResponseDomainEntity>

    /**
     * Apply voucher to order
     */
    suspend fun applyVoucher(
        request: ApplyVoucherRequestDomainEntity
    ): Resource<ApplyVoucherResponseDomainEntity>

    /**
     * Get shop's discounts with management filters
     */
    suspend fun getShopDiscounts(
        page: Int = 1,
        limit: Int = 10,
        name: String? = null,
        code: String? = null,
        discountStatus: String? = null,
        discountType: String? = null,
        createdById: String
    ): Resource<DiscountListDomainEntity>

    /**
     * Get discount detail
     */
    suspend fun getDiscountDetail(
        discountId: String
    ): Resource<DiscountDomainEntity>

    /**
     * Create new discount/voucher (Shop owner)
     */
    suspend fun createDiscount(
        request: CreateDiscountRequestDomainEntity
    ): Resource<DiscountDomainEntity>

    /**
     * Update existing discount/voucher (Shop owner)
     */
    suspend fun updateDiscount(
        discountId: String,
        request: UpdateDiscountRequestDomainEntity
    ): Resource<DiscountDomainEntity>

    /**
     * Delete discount/voucher (Shop owner)
     */
    suspend fun deleteDiscount(
        discountId: String
    ): Resource<Unit>
}

