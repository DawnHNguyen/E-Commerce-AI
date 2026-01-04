package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.DiscountApi
import com.ptit.data.remote.dto.discount.*
import com.ptit.domain.utils.Resource
import javax.inject.Inject

class DiscountRemoteDataSource @Inject constructor(
    private val api: DiscountApi
) {

    /**
     * Lấy danh sách mã giảm giá khả dụng cho checkout
     */
    suspend fun getAvailableDiscounts(
        limit: Int,
        cartItemIds: List<String>?,
        onlyShopDiscounts: Boolean,
        onlyPlatformDiscounts: Boolean
    ): Resource<List<DiscountDto>> {
        return api.getAvailableDiscounts(
            limit = limit,
            cartItemIds = cartItemIds,
            onlyShopDiscounts = onlyShopDiscounts,
            onlyPlatformDiscounts = onlyPlatformDiscounts
        )
    }

    /**
     * Kiểm tra mã voucher có hợp lệ không
     */
    suspend fun validateVoucherCode(
        request: ValidateVoucherCodeRequestDto
    ): Resource<ValidateVoucherCodeResponseDto> {
        return api.validateVoucherCode(request)
    }

    /**
     * Áp dụng mã voucher vào đơn hàng
     */
    suspend fun applyVoucher(
        request: ApplyVoucherRequestDto
    ): Resource<ApplyVoucherResponseDto> {
        return api.applyVoucher(request)
    }

    /**
     * Lấy danh sách voucher của Shop (Quản lý)
     */
    suspend fun getShopDiscounts(
        page: Int,
        limit: Int,
        name: String?,
        code: String?,
        discountStatus: String?,
        discountType: String?,
        createdById: String
    ): Resource<GetManageDiscountsResponseDto> {
        return api.getShopDiscounts(
            page = page,
            limit = limit,
            name = name,
            code = code,
            discountStatus = discountStatus,
            discountType = discountType,
            createdById = createdById
        )
    }

    /**
     * Lấy chi tiết voucher
     */
    suspend fun getDiscountDetail(discountId: String): Resource<GetDiscountDetailResponseDto> {
        return try {
            val response = api.getDiscountDetail(discountId)
            android.util.Log.e("DiscountDataSource", "✅ Response received: $response")
            android.util.Log.e("DiscountDataSource", "✅ Response.data: ${response.data}")
            android.util.Log.e("DiscountDataSource", "✅ Response.statusCode: ${response.statusCode}")

            if (response.data != null) {
                Resource.Success(response)
            } else {
                Resource.Error(
                    com.ptit.domain.utils.UnknownException(
                        error = null,
                        message = "Discount data is null",
                        requestUrl = "manage-discount/discounts/$discountId"
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("DiscountDataSource", "❌ Exception: ${e.message}", e)
            Resource.Error(
                com.ptit.domain.utils.UnknownException(
                    error = null,
                    message = e.message ?: "Unknown error",
                    requestUrl = "manage-discount/discounts/$discountId"
                )
            )
        }
    }

    /**
     * Tạo voucher mới (Shop)
     */
    suspend fun createDiscount(
        request: CreateDiscountRequestDto
    ): Resource<CreateDiscountResponseDto> {
        return api.createDiscount(request)
    }

    /**
     * Cập nhật voucher (Shop)
     */
    suspend fun updateDiscount(
        discountId: String,
        request: UpdateDiscountRequestDto
    ): Resource<UpdateDiscountResponseDto> {
        return api.updateDiscount(discountId, request)
    }

    /**
     * Xóa voucher (Shop)
     */
    suspend fun deleteDiscount(
        discountId: String
    ): Resource<MessageResponseDto> {
        return api.deleteDiscount(discountId)
    }
}