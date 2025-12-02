package com.ptit.core.order

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.cart.CartItemDetailDomainEntity
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.order.CreateOrderRequestDomainEntity
import com.ptit.domain.entity.order.CreateOrderResponseDomainEntity
import com.ptit.domain.entity.order.ReceiverDomainEntity
import com.ptit.domain.entity.order.ShopOrderRequestDomainEntity
import com.ptit.domain.entity.order.ShippingInfoDomainEntity
import com.ptit.domain.entity.shipping.DistrictEntity
import com.ptit.domain.entity.shipping.ProvinceEntity
import com.ptit.domain.entity.shipping.WardEntity
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
    private val shippingRepository: ShippingRepository
) : ViewModel() {

    // ... (Các StateFlows không đổi) ...
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

    // ... (loadUserProfile, setSelectedItems, updateName, v.v... không đổi) ...
    private fun loadUserProfile() {
        viewModelScope.launch {
            _userProfileState.update { Resource.loading() }
            when (val result = userRepository.getUserProfile()) {
                is Resource.Success -> {
                    _userProfileState.update { Resource.success(result.data) }
                    result.data.let { user ->
                        _orderState.update {
                            it.copy(
                                name = TextFieldValue(user.name),
                                phone = TextFieldValue(user.phoneNumber),
                                email = user.email,
                                isUserInfoLoaded = true
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    _userProfileState.update { Resource.error(result.error) }
                    _orderEvents.emit(OrderEvent.ShowError("Không tải được thông tin người dùng"))
                }
                else -> {}
            }
        }
    }

    fun setSelectedItems(
        selectedItemIds: List<String>,
        allGroupedItems: List<CartItemDetailDomainEntity>
    ) {
        if (selectedItemIds.isEmpty()) {
            viewModelScope.launch {
                _orderEvents.emit(OrderEvent.ShowError("Chưa chọn sản phẩm nào"))
            }
            return
        }

        val selectedIdsSet = selectedItemIds.toSet()

        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true, error = null) }

            val filteredShopGroups = allGroupedItems.mapNotNull { shopGroup ->
                val selectedItemsInShop = shopGroup.cartItems.filter { selectedIdsSet.contains(it.id) }
                if (selectedItemsInShop.isNotEmpty()) {
                    shopGroup.copy(cartItems = selectedItemsInShop)
                } else {
                    null
                }
            }

            _orderState.update {
                it.copy(
                    isLoading = false,
                    selectedShops = filteredShopGroups,
                    error = if (filteredShopGroups.isEmpty()) "Không tìm thấy sản phẩm nào đã chọn." else null
                )
            }

            if (filteredShopGroups.isEmpty()) {
                viewModelScope.launch {
                    _orderEvents.emit(OrderEvent.ShowError("Không tìm thấy sản phẩm nào đã chọn trong giỏ hàng hiện tại."))
                }
            }
        }
    }

    fun updateName(value: TextFieldValue) = _orderState.update { it.copy(name = value) }
    fun updatePhone(value: TextFieldValue) = _orderState.update { it.copy(phone = value) }
    fun updateAddress(value: TextFieldValue) = _orderState.update { it.copy(address = value) }
    fun updateNote(value: TextFieldValue) = _orderState.update { it.copy(note = value) }


    // 🔴 XÓA: Hàm validateOrderDetails không còn cần thiết

    /**
     * 🧾 Tạo đơn hàng (Quay lại logic cũ)
     */
    fun createOrder() {
        val state = _orderState.value
        val address = _addressState.value

        // --- 1. Validation (Kiểm tra lại trong hàm này) ---
        if (state.selectedShops.isEmpty()) {
            viewModelScope.launch { _orderEvents.emit(OrderEvent.ShowError("Không có sản phẩm nào được chọn")) }
            return
        }

        if (state.name.text.isBlank() || state.phone.text.isBlank() || state.address.text.isBlank() ||
            address.selectedProvince == null || address.selectedDistrict == null || address.selectedWard == null
        ) {
            viewModelScope.launch { _orderEvents.emit(OrderEvent.ShowError("Vui lòng nhập đủ thông tin người nhận và địa chỉ giao hàng")) }
            return
        }

        // --- 2. Build Entities ---
        val receiver = ReceiverDomainEntity(
            name = state.name.text,
            phone = state.phone.text,
            address = state.address.text,
            provinceId = address.selectedProvince.id,
            districtId = address.selectedDistrict.id,
            wardCode = address.selectedWard.code
        )

        val defaultShippingInfo = ShippingInfoDomainEntity(
            serviceId = null,
            serviceTypeId = 2,
            weight = 1000.0,
            length = 20.0,
            width = 20.0,
            height = 10.0,
            // ✅ Use calculated shipping fee (0 if not calculated, API result if success, 30k if error)
            shippingFee = state.calculatedShippingFee,
            note = state.note.text.ifBlank { null },
            paymentTypeId = 1,
            configFeeId = null,
            extraCostId = null,
            requiredNote = null,
            coupon = null,
            pickShift = null
        )

        val shopRequests = state.selectedShops.mapNotNull { detail ->
            val shopId = detail.shopId ?: return@mapNotNull null
            ShopOrderRequestDomainEntity(
                shopId = shopId,
                receiver = receiver,
                cartItemIds = detail.cartItems.map { it.id },
                discountCodes = emptyList(),
                shippingInfo = defaultShippingInfo,
                // ✅ FIX: isCod = false để tạo đơn PENDING_PAYMENT (Chờ thanh toán)
                // Sau khi user thanh toán thành công → Backend sẽ chuyển sang PENDING_PACKAGE (Chờ vận chuyển)
                isCod = false
            )
        }

        val finalRequest = CreateOrderRequestDomainEntity(
            shops = shopRequests,
            platformDiscountCodes = emptyList()
        )

        // --- 3. API Call ---
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

    // ... (loadProvinces và các hàm address khác không đổi) ...
    private fun loadProvinces() {
        viewModelScope.launch {
            _addressState.update { it.copy(provincesLoading = true) }
            when (val result = shippingRepository.getProvinces()) {
                is Resource.Success -> _addressState.update {
                    it.copy(provinces = result.data, provincesLoading = false)
                }
                is Resource.Error -> {
                    _orderEvents.emit(OrderEvent.ShowError("Lỗi tải Tỉnh/Thành phố"))
                    _addressState.update { it.copy(provincesLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun selectProvince(province: ProvinceEntity?) {
        if (province == null) {
            _addressState.update {
                it.copy(selectedProvince = null, selectedDistrict = null, selectedWard = null, districts = emptyList(), wards = emptyList())
            }
            return
        }
        if (province.id == _addressState.value.selectedProvince?.id) return

        _addressState.update {
            it.copy(
                selectedProvince = province,
                selectedDistrict = null,
                selectedWard = null,
                districts = emptyList(),
                wards = emptyList()
            )
        }
        loadDistricts(province.id)
    }

    private fun loadDistricts(provinceId: Int) {
        viewModelScope.launch {
            _addressState.update { it.copy(districtsLoading = true) }
            when (val result = shippingRepository.getDistricts(provinceId)) {
                is Resource.Success -> _addressState.update {
                    it.copy(districts = result.data, districtsLoading = false)
                }
                is Resource.Error -> {
                    _orderEvents.emit(OrderEvent.ShowError("Lỗi tải Quận/Huyện"))
                    _addressState.update { it.copy(districtsLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun selectDistrict(district: DistrictEntity?) {
        if (district == null) {
            _addressState.update {
                it.copy(selectedDistrict = null, selectedWard = null, wards = emptyList())
            }
            return
        }
        if (district.id == _addressState.value.selectedDistrict?.id) return

        _addressState.update {
            it.copy(
                selectedDistrict = district,
                selectedWard = null,
                wards = emptyList()
            )
        }
        loadWards(district.id)
    }

    fun selectWard(ward: WardEntity?) {
        _addressState.update {
            it.copy(
                selectedWard = ward
            )
        }
        // ✅ NEW: Calculate shipping fee when address is complete
        if (ward != null) {
            calculateShippingFee()
        }
    }

    /**
     * 📦 Calculate shipping fee from GHN API
     * Triggered automatically when user completes address selection (Province → District → Ward)
     *
     * Logic:
     * - Ban đầu: Phí ship = 0đ
     * - API success (200 OK) → Set phí ship = kết quả từ API
     * - API error (≠ 200) → Set phí ship = 30,000đ và cho phép tạo đơn bình thường
     */
    private fun calculateShippingFee() {
        val address = _addressState.value
        val state = _orderState.value

        // Validate address is complete
        if (address.selectedProvince == null ||
            address.selectedDistrict == null ||
            address.selectedWard == null ||
            state.selectedShops.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _orderState.update { it.copy(isCalculatingShippingFee = true) }

            // ✅ Calculate total weight from selected products
            val totalWeight = calculateTotalWeight(state.selectedShops)

            // Build request
            val request = com.ptit.domain.entity.shipping.CalculateShippingFeeRequestDomainEntity(
                height = 10.0,
                weight = totalWeight,
                length = 20.0,
                width = 20.0,
                wardCode = address.selectedWard!!.code,
                districtId = address.selectedDistrict!!.id,
                provinceId = address.selectedProvince!!.id,
                serviceTypeId = 2
            )

            // ✅ Call GHN API via repository
            when (val result = shippingRepository.calculateShippingFee(request)) {
                is Resource.Success -> {
                    // ✅ Response = 200 OK: Use calculated shipping fee from API
                    _orderState.update {
                        it.copy(
                            calculatedShippingFee = result.data.total.toDouble(),
                            isCalculatingShippingFee = false
                        )
                    }
                }
                is Resource.Error -> {
                    // ✅ Response ≠ 200: Set default 30,000đ và cho phép tạo đơn
                    _orderState.update {
                        it.copy(
                            calculatedShippingFee = 30000.0,
                            isCalculatingShippingFee = false
                        )
                    }
                    // Show friendly message - user can still create order
                    _orderEvents.emit(
                        OrderEvent.ShowError("Sử dụng phí ship mặc định 30,000đ. Bạn vẫn có thể đặt hàng bình thường.")
                    )
                }
                else -> {
                    // ✅ Other cases: Set default 30,000đ
                    _orderState.update {
                        it.copy(
                            calculatedShippingFee = 30000.0,
                            isCalculatingShippingFee = false
                        )
                    }
                }
            }
        }
    }

    /**
     * Helper: Calculate total weight from selected products
     * TODO: Get actual weight from product data when available
     */
    private fun calculateTotalWeight(shops: List<CartItemDetailDomainEntity>): Double {
        // Estimate 200g per item (you can improve this later with real product weights)
        val totalItems = shops.sumOf { shop ->
            shop.cartItems.sumOf { it.quantity }
        }
        return (totalItems * 200).toDouble() // grams
    }

    private fun loadWards(districtId: Int) {
        viewModelScope.launch {
            _addressState.update { it.copy(wardsLoading = true) }
            when (val result = shippingRepository.getWards(districtId)) {
                is Resource.Success -> _addressState.update {
                    it.copy(wards = result.data, wardsLoading = false)
                }
                is Resource.Error -> {
                    _orderEvents.emit(OrderEvent.ShowError("Lỗi tải Phường/Xã"))
                    _addressState.update { it.copy(wardsLoading = false) }
                }
                else -> {}
            }
        }
    }

    // ... (OrderFormState, AddressState, OrderEvent không đổi) ...
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
        // ✅ NEW: Store calculated shipping fee from GHN API
        // Ban đầu = 0đ, sau khi gọi API sẽ update
        val calculatedShippingFee: Double = 0.0,
        val isCalculatingShippingFee: Boolean = false
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