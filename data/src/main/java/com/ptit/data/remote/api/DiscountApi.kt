package com.ptit.data.remote.api

import com.ptit.data.remote.dto.discount.*
import com.ptit.domain.utils.Resource
import retrofit2.http.*

interface DiscountApi {

    /**
     * Get available discounts for checkout
     */
    @GET("discounts/available")
    suspend fun getAvailableDiscounts(
        @Query("limit") limit: Int = 20,
        @Query("cartItemIds") cartItemIds: List<String>? = null,
        @Query("onlyShopDiscounts") onlyShopDiscounts: Boolean = false,
        @Query("onlyPlatformDiscounts") onlyPlatformDiscounts: Boolean = false
    ): Resource<List<DiscountDto>>

    /**
     * Validate voucher code
     */
    @POST("discounts/validate-code")
    suspend fun validateVoucherCode(
        @Body request: ValidateVoucherCodeRequestDto
    ): Resource<ValidateVoucherCodeResponseDto>

    /**
     * Apply voucher to order
     */
    @POST("discounts/apply")
    suspend fun applyVoucher(
        @Body request: ApplyVoucherRequestDto
    ): Resource<ApplyVoucherResponseDto>

    /**
     * Get shop's discounts with management filters (Shop owner)
     */
    @GET("manage-discount/discounts")
    suspend fun getShopDiscounts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("name") name: String? = null,
        @Query("code") code: String? = null,
        @Query("discountStatus") discountStatus: String? = null,
        @Query("discountType") discountType: String? = null,
        @Query("createdById") createdById: String
    ): Resource<GetManageDiscountsResponseDto>

    /**
     * Get discount detail
     */
    @GET("manage-discount/discounts/{discountId}")
    suspend fun getDiscountDetail(
        @Path("discountId") discountId: String
    ): GetDiscountDetailResponseDto

    /**
     * Create new discount/voucher (Shop owner)
     */
    @POST("manage-discount/discounts")
    suspend fun createDiscount(
        @Body request: CreateDiscountRequestDto
    ): Resource<CreateDiscountResponseDto>

    /**
     * Update existing discount/voucher (Shop owner)
     */
    @PUT("manage-discount/discounts/{discountId}")
    suspend fun updateDiscount(
        @Path("discountId") discountId: String,
        @Body request: UpdateDiscountRequestDto
    ): Resource<UpdateDiscountResponseDto>

    /**
     * Delete discount/voucher (Shop owner)
     */
    @DELETE("manage-discount/discounts/{discountId}")
    suspend fun deleteDiscount(
        @Path("discountId") discountId: String
    ): Resource<MessageResponseDto>
}

