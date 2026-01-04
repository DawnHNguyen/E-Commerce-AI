package com.ptit.core.order

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.cart.CartItemDetailDomainEntity
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.discount.DiscountDomainEntity
import com.ptit.domain.entity.order.*
import com.ptit.domain.entity.shipping.*
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.repository.ShippingRepository
import com.ptit.domain.repository.UserRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val shippingRepository: ShippingRepository,
    private val discountRepository: com.ptit.domain.repository.DiscountRepository
) : ViewModel() {

    private val _orderState = MutableStateFlow(OrderFormState())
    val orderState: StateFlow<OrderFormState> = _orderState.asStateFlow()

    private val _addressState = MutableStateFlow(AddressState())
    val addressState: StateFlow<AddressState> = _addressState.asStateFlow()

    private val _orderEvents = MutableSharedFlow<OrderEvent>()
    val orderEvents: SharedFlow<OrderEvent> = _orderEvents.asSharedFlow()

    private val _userProfileState = MutableStateFlow<Resource<UserDomainEntity>>(Resource.idle())
    val userProfileState: StateFlow<Resource<UserDomainEntity>> = _userProfileState.asStateFlow()

    init {
        loadUserProfile()
        loadProvinces()
    }

    // ... (Các hàm load user, province, address... giữ nguyên như cũ) ...
    // ... Bạn có thể giữ nguyên phần code cũ cho các hàm này ...

    // --- COPY PASTE LẠI CÁC HÀM CƠ BẢN ĐỂ KHÔNG BỊ MẤT ---
    private fun loadUserProfile() {
        viewModelScope.launch {
            _userProfileState.update { Resource.loading() }
            when (val result = userRepository.getUserProfile()) {
                is Resource.Success -> {
                    _userProfileState.update { Resource.success(result.data) }
                    result.data.let { user ->
                        _orderState.update { it.copy(name = TextFieldValue(user.name), phone = TextFieldValue(user.phoneNumber), email = user.email, isUserInfoLoaded = true) }
                    }
                }
                else -> {}
            }
        }
    }
    fun setSelectedItems(selectedItemIds: List<String>, allGroupedItems: List<CartItemDetailDomainEntity>) {
        val selectedIdsSet = selectedItemIds.toSet()
        viewModelScope.launch {
            val filteredShopGroups = allGroupedItems.mapNotNull { shopGroup ->
                val selectedItemsInShop = shopGroup.cartItems.filter { selectedIdsSet.contains(it.id) }
                if (selectedItemsInShop.isNotEmpty()) shopGroup.copy(cartItems = selectedItemsInShop) else null
            }
            _orderState.update { it.copy(selectedShops = filteredShopGroups) }
        }
    }
    fun updateName(value: TextFieldValue) = _orderState.update { it.copy(name = value) }
    fun updatePhone(value: TextFieldValue) = _orderState.update { it.copy(phone = value) }
    fun updateAddress(value: TextFieldValue) = _orderState.update { it.copy(address = value) }
    fun updateNote(value: TextFieldValue) = _orderState.update { it.copy(note = value) }
    private fun loadProvinces() {
        viewModelScope.launch {
            _addressState.update { it.copy(provincesLoading = true) }
            when (val result = shippingRepository.getProvinces()) {
                is Resource.Success -> _addressState.update { it.copy(provinces = result.data, provincesLoading = false) }
                else -> _addressState.update { it.copy(provincesLoading = false) }
            }
        }
    }
    fun selectProvince(province: ProvinceEntity?) {
        _addressState.update { it.copy(selectedProvince = province, selectedDistrict = null, selectedWard = null, districts = emptyList(), wards = emptyList()) }
        if (province != null) loadDistricts(province.id)
    }
    private fun loadDistricts(provinceId: Int) {
        viewModelScope.launch {
            when (val result = shippingRepository.getDistricts(provinceId)) {
                is Resource.Success -> _addressState.update { it.copy(districts = result.data) }
                else -> {}
            }
        }
    }
    fun selectDistrict(district: DistrictEntity?) {
        _addressState.update { it.copy(selectedDistrict = district, selectedWard = null, wards = emptyList()) }
        if (district != null) loadWards(district.id)
    }
    private fun loadWards(districtId: Int) {
        viewModelScope.launch {
            when (val result = shippingRepository.getWards(districtId)) {
                is Resource.Success -> _addressState.update { it.copy(wards = result.data) }
                else -> {}
            }
        }
    }
    fun selectWard(ward: WardEntity?) {
        _addressState.update { it.copy(selectedWard = ward) }
        if (ward != null) calculateShippingFee()
    }
    private fun calculateShippingFee() {
        val address = _addressState.value
        val state = _orderState.value
        if (address.selectedProvince == null || address.selectedDistrict == null || address.selectedWard == null || state.selectedShops.isEmpty()) return
        viewModelScope.launch {
            _orderState.update { it.copy(isCalculatingShippingFee = true) }
            val totalWeight = (state.selectedShops.sumOf { shop -> shop.cartItems.sumOf { it.quantity } } * 200).toDouble()
            val request = CalculateShippingFeeRequestDomainEntity(
                height = 10.0, weight = totalWeight, length = 20.0, width = 20.0,
                wardCode = address.selectedWard!!.code, districtId = address.selectedDistrict!!.id, provinceId = address.selectedProvince!!.id, serviceTypeId = 2
            )
            when (val result = shippingRepository.calculateShippingFee(request)) {
                is Resource.Success -> _orderState.update { it.copy(calculatedShippingFee = result.data.total.toDouble(), isCalculatingShippingFee = false) }
                else -> _orderState.update { it.copy(calculatedShippingFee = 30000.0, isCalculatingShippingFee = false) }
            }
        }
    }

    // ========================================
    // 🎟️ VOUCHER LOGIC (QUAN TRỌNG: ĐÃ SỬA)
    // ========================================

    fun loadAvailableVouchers() {
        viewModelScope.launch {
            _orderState.update { it.copy(isLoadingVouchers = true, voucherError = null) }
            val cartItemIds = _orderState.value.selectedShops.flatMap { shop -> shop.cartItems.map { it.id } }

            // Gọi API: onlyPlatformDiscounts = false để lấy hết về cho chắc, sau đó lọc ở client
            when (val result = discountRepository.getAvailableDiscounts(
                limit = 100,
                cartItemIds = cartItemIds,
                onlyShopDiscounts = false,
                onlyPlatformDiscounts = false
            )) {
                is Resource.Success -> {
                    // ✅ Lọc chỉ lấy Platform Voucher để hiển thị
                    // Nếu danh sách rỗng, list này sẽ empty -> UI sẽ hiện "Chưa có voucher"
                    val platformVouchers = result.data.data.filter { it.isPlatform }

                    android.util.Log.d("CreateOrderVM", "Loaded ${result.data.data.size} vouchers. Filtered platform: ${platformVouchers.size}")

                    _orderState.update {
                        it.copy(
                            allAvailableVouchers = platformVouchers,
                            isLoadingVouchers = false
                        )
                    }
                }
                is Resource.Error -> {
                    android.util.Log.e("CreateOrderVM", "Error: ${result.error.message}")
                    _orderState.update {
                        it.copy(isLoadingVouchers = false, voucherError = "Lỗi tải voucher")
                    }
                }
                else -> {}
            }
        }
    }

    fun applyVoucherCode(code: String) {
        viewModelScope.launch {
            _orderState.update { it.copy(isLoadingVouchers = true, voucherError = null) }
            val cartItemIds = _orderState.value.selectedShops.flatMap { shop -> shop.cartItems.map { it.id } }
            val request = com.ptit.domain.entity.discount.ValidateVoucherRequestDomainEntity(code = code, cartItemIds = cartItemIds)

            when (val result = discountRepository.validateVoucherCode(request)) {
                is Resource.Success -> {
                    if (result.data.isValid && result.data.discount != null) {
                        if (result.data.discount!!.isPlatform) {
                            selectPlatformVoucher(result.data.discount!!)
                            _orderState.update { it.copy(isLoadingVouchers = false) }
                            _orderEvents.emit(OrderEvent.ShowError("Áp dụng mã thành công!"))
                        } else {
                            _orderState.update { it.copy(isLoadingVouchers = false, voucherError = "Đây không phải voucher sàn") }
                        }
                    } else {
                        _orderState.update { it.copy(isLoadingVouchers = false, voucherError = result.data.error ?: "Mã không hợp lệ") }
                    }
                }
                is Resource.Error -> {
                    _orderState.update { it.copy(isLoadingVouchers = false, voucherError = "Lỗi xác thực mã") }
                }
                else -> {}
            }
        }
    }

    fun selectPlatformVoucher(voucher: DiscountDomainEntity) {
        if (voucher.isPlatform) {
            _orderState.update { it.copy(selectedPlatformVoucher = voucher) }
        }
    }

    fun removePlatformVoucher() {
        _orderState.update { it.copy(selectedPlatformVoucher = null) }
    }

    fun calculateDiscountValue(voucher: DiscountDomainEntity, baseAmount: Double): Double {
        return when (voucher.discountType) {
            "PERCENTAGE" -> {
                val amount = (baseAmount * voucher.value / 100)
                val max = voucher.maxDiscountValue
                if (max != null) minOf(amount, max) else amount
            }
            "FIX_AMOUNT", "FIXED" -> voucher.value
            else -> 0.0
        }
    }

    fun createOrder() {
        val state = _orderState.value
        val address = _addressState.value

        if (state.selectedShops.isEmpty()) return
        if (state.name.text.isBlank() || state.phone.text.isBlank() || state.address.text.isBlank() || address.selectedProvince == null) {
            viewModelScope.launch { _orderEvents.emit(OrderEvent.ShowError("Vui lòng nhập đủ thông tin")) }
            return
        }

        val receiver = ReceiverDomainEntity(
            name = state.name.text,
            phone = state.phone.text,
            address = state.address.text,
            provinceId = address.selectedProvince!!.id,
            districtId = address.selectedDistrict!!.id,
            wardCode = address.selectedWard!!.code
        )

        val defaultShippingInfo = ShippingInfoDomainEntity(
            serviceId = null, serviceTypeId = 2, weight = 1000.0, length = 20.0, width = 20.0, height = 10.0,
            shippingFee = state.calculatedShippingFee, note = state.note.text.ifBlank { null }, paymentTypeId = 1, configFeeId = null, extraCostId = null, requiredNote = null, coupon = null, pickShift = null
        )

        val platformDiscountCodes = if (state.selectedPlatformVoucher != null) {
            listOf(state.selectedPlatformVoucher.code)
        } else {
            emptyList()
        }

        val shopRequests = state.selectedShops.mapNotNull { detail ->
            val shopId = detail.shopId ?: return@mapNotNull null
            ShopOrderRequestDomainEntity(
                shopId = shopId, receiver = receiver, cartItemIds = detail.cartItems.map { it.id },
                discountCodes = emptyList(), shippingInfo = defaultShippingInfo, isCod = false
            )
        }

        val finalRequest = CreateOrderRequestDomainEntity(shops = shopRequests, platformDiscountCodes = platformDiscountCodes)

        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true) }
            when (val result = orderRepository.createOrder(finalRequest)) {
                is Resource.Success -> {
                    _orderState.update { it.copy(isLoading = false) }
                    result.data.let { _orderEvents.emit(OrderEvent.OrderCreated(it)) }
                }
                is Resource.Error -> {
                    _orderState.update { it.copy(isLoading = false, error = result.error.message) }
                    _orderEvents.emit(OrderEvent.ShowError(result.error.message ?: "Tạo đơn hàng thất bại"))
                }
                else -> {}
            }
        }
    }

    data class OrderFormState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedShops: List<CartItemDetailDomainEntity> = emptyList(),
        val name: TextFieldValue = TextFieldValue(""),
        val phone: TextFieldValue = TextFieldValue(""),
        val email: String = "",
        val address: TextFieldValue = TextFieldValue(""),
        val note: TextFieldValue = TextFieldValue(""),
        val isUserInfoLoaded: Boolean = false,
        val calculatedShippingFee: Double = 0.0,
        val isCalculatingShippingFee: Boolean = false,

        // --- PLATFORM VOUCHER ---
        val selectedPlatformVoucher: DiscountDomainEntity? = null,
        val allAvailableVouchers: List<DiscountDomainEntity> = emptyList(),
        val isLoadingVouchers: Boolean = false,
        val voucherError: String? = null
    )

    data class AddressState(
        val provinces: List<ProvinceEntity> = emptyList(),
        val districts: List<DistrictEntity> = emptyList(),
        val wards: List<WardEntity> = emptyList(),
        val selectedProvince: ProvinceEntity? = null,
        val selectedDistrict: DistrictEntity? = null,
        val selectedWard: WardEntity? = null,
        val provincesLoading: Boolean = false,
        val districtsLoading: Boolean = false,
        val wardsLoading: Boolean = false
    )

    sealed class OrderEvent {
        data class ShowError(val message: String) : OrderEvent()
        data class OrderCreated(val response: CreateOrderResponseDomainEntity) : OrderEvent()
    }
}