package com.ptit.core.discount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.constants.DiscountConstants
import com.ptit.domain.entity.discount.UpdateDiscountRequestDomainEntity
import com.ptit.domain.repository.DiscountRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// State holder cho Form chỉnh sửa
data class EditDiscountUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdateSuccess: Boolean = false,

    // Form Fields
    val name: String = "",
    val description: String = "",
    val code: String = "",
    val value: String = "",
    val minOrderValue: String = "",
    val maxDiscountValue: String = "",
    val maxUses: String = "",
    val maxUsesPerUser: String = "",
    val discountType: String = DiscountConstants.DISCOUNT_TYPE_PERCENTAGE,
    val discountStatus: String = DiscountConstants.DISCOUNT_STATUS_ACTIVE,
    val discountApplyType: String = DiscountConstants.DISCOUNT_APPLY_TYPE_ALL,
    val voucherType: String = DiscountConstants.VOUCHER_TYPE_SHOP,
    val displayType: String = DiscountConstants.DISPLAY_TYPE_PUBLIC,
    val startDate: String = "",
    val endDate: String = "",

    // IDs for SPECIFIC apply type
    val brandIds: List<String> = emptyList(),
    val categoryIds: List<String> = emptyList(),
    val productIds: List<String> = emptyList()
)

@HiltViewModel
class EditDiscountViewModel @Inject constructor(
    private val discountRepository: DiscountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditDiscountUiState())
    val uiState = _uiState.asStateFlow()

    // 1. Load dữ liệu chi tiết
    fun loadDiscountDetail(discountId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = discountRepository.getDiscountDetail(discountId)) {
                is Resource.Success -> {
                    val discount = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = discount.name,
                            description = discount.description ?: "",
                            code = discount.code,
                            value = if(discount.discountType == DiscountConstants.DISCOUNT_TYPE_PERCENTAGE) discount.value.toInt().toString() else discount.value.toString(),
                            minOrderValue = discount.minOrderValue.toInt().toString(),
                            maxDiscountValue = discount.maxDiscountValue?.toInt()?.toString() ?: "",
                            maxUses = discount.maxUses.toString(),
                            maxUsesPerUser = discount.maxUsesPerUser.toString(),
                            discountType = discount.discountType,
                            discountStatus = discount.discountStatus,
                            discountApplyType = discount.discountApplyType,
                            voucherType = discount.voucherType,
                            displayType = discount.displayType,
                            startDate = discount.startDate,
                            endDate = discount.endDate,
                            // Load IDs từ discount data
                            brandIds = discount.brands?.map { it.id } ?: emptyList(),
                            categoryIds = discount.categories?.map { it.id } ?: emptyList(),
                            productIds = discount.products?.map { it.id } ?: emptyList()
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                }
                else -> {}
            }
        }
    }

    // 2. Các hàm update field từ UI
    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onCodeChange(value: String) = _uiState.update { it.copy(code = value) }
    fun onValueChange(value: String) = _uiState.update { it.copy(value = value) }
    fun onMinOrderChange(value: String) = _uiState.update { it.copy(minOrderValue = value) }
    fun onMaxDiscountChange(value: String) = _uiState.update { it.copy(maxDiscountValue = value) }
    fun onMaxUsesChange(value: String) = _uiState.update { it.copy(maxUses = value) }
    fun onMaxUsesPerUserChange(value: String) = _uiState.update { it.copy(maxUsesPerUser = value) }
    fun onDiscountTypeChange(value: String) = _uiState.update { it.copy(discountType = value) }
    fun onStatusChange(value: String) = _uiState.update { it.copy(discountStatus = value) }
    fun onApplyTypeChange(value: String) = _uiState.update { it.copy(discountApplyType = value) }
    fun onVoucherTypeChange(value: String) = _uiState.update { it.copy(voucherType = value) }
    fun onDisplayTypeChange(value: String) = _uiState.update { it.copy(displayType = value) }
    fun onStartDateChange(value: String) = _uiState.update { it.copy(startDate = value) }
    fun onEndDateChange(value: String) = _uiState.update { it.copy(endDate = value) }

    // 3. Submit Update
    fun updateDiscount(discountId: String, shopId: String) {
        val state = _uiState.value

        // Basic validation
        if (state.name.isBlank() || state.code.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng điền đầy đủ thông tin bắt buộc") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Tính toán maxDiscountValue:
            // - Nếu người dùng nhập giá trị thì dùng giá trị đó
            // - Nếu không nhập thì dùng default 1,000,000
            // - API yêu cầu bắt buộc phải có field này
            val maxDiscountValueParsed = state.maxDiscountValue.toDoubleOrNull() ?: 1000000.0

            // Nếu discountApplyType là SPECIFIC nhưng không có IDs, tự động chuyển về ALL
            val finalDiscountApplyType = if (state.discountApplyType == DiscountConstants.DISCOUNT_APPLY_TYPE_SPECIFIC &&
                state.brandIds.isEmpty() && state.categoryIds.isEmpty() && state.productIds.isEmpty()) {

                android.util.Log.w("EditDiscountViewModel", """
                    ⚠️ Voucher có discountApplyType = SPECIFIC nhưng không có brands/categories/products IDs!
                    - brandIds: ${state.brandIds}
                    - categoryIds: ${state.categoryIds}
                    - productIds: ${state.productIds}
                    → Tự động chuyển sang discountApplyType = ALL để tránh lỗi validation
                """.trimIndent())

                // Cập nhật state để hiển thị thông báo cho user
                _uiState.update {
                    it.copy(
                        error = "Lưu ý: Voucher này sẽ áp dụng cho TẤT CẢ sản phẩm vì không có thông tin cụ thể từ server"
                    )
                }

                DiscountConstants.DISCOUNT_APPLY_TYPE_ALL
            } else {
                state.discountApplyType
            }

            // Chỉ truyền IDs khi discountApplyType là SPECIFIC và có IDs
            val (brands, categories, products) = if (finalDiscountApplyType == DiscountConstants.DISCOUNT_APPLY_TYPE_SPECIFIC) {
                Triple(
                    if (state.brandIds.isNotEmpty()) state.brandIds else null,
                    if (state.categoryIds.isNotEmpty()) state.categoryIds else null,
                    if (state.productIds.isNotEmpty()) state.productIds else null
                )
            } else {
                Triple(null, null, null)
            }

            val request = UpdateDiscountRequestDomainEntity(
                name = state.name,
                description = state.description.ifBlank { null },
                value = state.value.toDoubleOrNull() ?: 0.0,
                code = state.code,
                startDate = state.startDate,
                endDate = state.endDate,
                maxUsesPerUser = state.maxUsesPerUser.toIntOrNull() ?: 1,
                minOrderValue = state.minOrderValue.toDoubleOrNull() ?: 0.0,
                maxUses = state.maxUses.toIntOrNull() ?: 0,
                maxDiscountValue = maxDiscountValueParsed,
                displayType = state.displayType,
                voucherType = state.voucherType,
                isPlatform = false,
                shopId = shopId,
                discountApplyType = finalDiscountApplyType,  // ← Sử dụng finalDiscountApplyType đã xử lý
                discountStatus = state.discountStatus,
                discountType = state.discountType,
                brands = brands,
                categories = categories,
                products = products
            )

            when (val result = discountRepository.updateDiscount(discountId, request)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isUpdateSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                }
                else -> {}
            }
        }
    }

    fun resetState() {
        _uiState.update { EditDiscountUiState() }
    }
}