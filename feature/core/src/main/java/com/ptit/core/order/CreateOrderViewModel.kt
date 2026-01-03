package com.ptit.core.order

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.cart.CartItemDetailDomainEntity
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.discount.DiscountDomainEntity
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

    // ========================================
    // 🚚 SHIPPING & ADDRESS
    // ========================================

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
        _addressState.update { it.copy(selectedWard = ward) }
        if (ward != null) {
            calculateShippingFee()
        }
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

    private fun calculateShippingFee() {
        val address = _addressState.value
        val state = _orderState.value

        if (address.selectedProvince == null || address.selectedDistrict == null || address.selectedWard == null || state.selectedShops.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _orderState.update { it.copy(isCalculatingShippingFee = true) }
            val totalWeight = calculateTotalWeight(state.selectedShops)

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

            when (val result = shippingRepository.calculateShippingFee(request)) {
                is Resource.Success -> {
                    _orderState.update {
                        it.copy(
                            calculatedShippingFee = result.data.total.toDouble(),
                            isCalculatingShippingFee = false
                        )
                    }
                }
                is Resource.Error -> {
                    _orderState.update {
                        it.copy(
                            calculatedShippingFee = 30000.0,
                            isCalculatingShippingFee = false
                        )
                    }
                    _orderEvents.emit(OrderEvent.ShowError("Sử dụng phí ship mặc định 30,000đ."))
                }
                else -> {
                    _orderState.update { it.copy(calculatedShippingFee = 30000.0, isCalculatingShippingFee = false) }
                }
            }
        }
    }

    private fun calculateTotalWeight(shops: List<CartItemDetailDomainEntity>): Double {
        val totalItems = shops.sumOf { shop -> shop.cartItems.sumOf { it.quantity } }
        return (totalItems * 200).toDouble()
    }

    // ========================================
    // 🎟️ VOUCHER LOGIC (UPDATED)
    // ========================================

    /**
     * Mở BottomSheet chọn voucher
     * @param shopId: null nếu là Platform Voucher, string nếu là Shop Voucher
     */
    fun openVoucherSheet(shopId: String?) {
        _orderState.update { it.copy(currentSelectingShopId = shopId) }

        // Luôn tải lại hoặc tải lần đầu để đảm bảo có voucher mới nhất
        if (_orderState.value.allAvailableVouchers.isEmpty()) {
            loadAvailableVouchers()
        }
    }

    fun loadAvailableVouchers() {
        viewModelScope.launch {
            _orderState.update { it.copy(isLoadingVouchers = true, voucherError = null) }

            val cartItemIds = _orderState.value.selectedShops.flatMap { shop ->
                shop.cartItems.map { it.id }
            }

            when (val result = discountRepository.getAvailableDiscounts(
                limit = 100, // Tải nhiều để lọc client-side
                cartItemIds = cartItemIds
            )) {
                is Resource.Success -> {
                    _orderState.update {
                        it.copy(
                            allAvailableVouchers = result.data.data,
                            isLoadingVouchers = false
                        )
                    }
                }
                is Resource.Error -> {
                    _orderState.update {
                        it.copy(
                            isLoadingVouchers = false,
                            voucherError = "Không thể tải danh sách voucher"
                        )
                    }
                }
                else -> {}
            }
        }
    }

    fun applyVoucherCode(code: String) {
        // Chức năng này tạm thời gọi API validate,
        // Sau đó nếu hợp lệ thì tự động add vào context hiện tại
        viewModelScope.launch {
            _orderState.update { it.copy(isLoadingVouchers = true, voucherError = null) }

            val cartItemIds = _orderState.value.selectedShops.flatMap { shop -> shop.cartItems.map { it.id } }

            val request = com.ptit.domain.entity.discount.ValidateVoucherRequestDomainEntity(
                code = code,
                cartItemIds = cartItemIds
            )

            when (val result = discountRepository.validateVoucherCode(request)) {
                is Resource.Success -> {
                    if (result.data.isValid && result.data.discount != null) {
                        // Tự động chọn voucher sau khi apply thành công
                        selectVoucher(result.data.discount!!)
                        _orderState.update { it.copy(isLoadingVouchers = false) }
                        _orderEvents.emit(OrderEvent.ShowError("Áp dụng mã thành công!"))
                    } else {
                        _orderState.update {
                            it.copy(isLoadingVouchers = false, voucherError = result.data.error ?: "Mã không hợp lệ")
                        }
                    }
                }
                is Resource.Error -> {
                    _orderState.update {
                        it.copy(isLoadingVouchers = false, voucherError = "Lỗi xác thực mã")
                    }
                }
                else -> {}
            }
        }
    }

    fun selectVoucher(voucher: DiscountDomainEntity) {
        val currentContext = _orderState.value.currentSelectingShopId

        _orderState.update { state ->
            if (currentContext == null) {
                // Đang chọn cho Platform
                if (voucher.isPlatform) {
                    state.copy(selectedPlatformVoucher = voucher)
                } else {
                    state // Không cho phép chọn voucher shop vào slot platform
                }
            } else {
                // Đang chọn cho Shop cụ thể
                if (!voucher.isPlatform && voucher.shopId == currentContext) {
                    val newMap = state.selectedShopVouchers.toMutableMap()
                    newMap[currentContext] = voucher
                    state.copy(selectedShopVouchers = newMap)
                } else {
                    state
                }
            }
        }
    }

    fun removeVoucher() {
        val currentContext = _orderState.value.currentSelectingShopId
        _orderState.update { state ->
            if (currentContext == null) {
                state.copy(selectedPlatformVoucher = null)
            } else {
                val newMap = state.selectedShopVouchers.toMutableMap()
                newMap.remove(currentContext)
                state.copy(selectedShopVouchers = newMap)
            }
        }
    }

    /**
     * Helper tính toán giá trị giảm giá (Dùng cho UI display)
     */
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

    // ========================================
    // 📦 CREATE ORDER
    // ========================================

    fun createOrder() {
        val state = _orderState.value
        val address = _addressState.value

        if (state.selectedShops.isEmpty()) {
            viewModelScope.launch { _orderEvents.emit(OrderEvent.ShowError("Không có sản phẩm nào được chọn")) }
            return
        }

        if (state.name.text.isBlank() || state.phone.text.isBlank() || state.address.text.isBlank() ||
            address.selectedProvince == null || address.selectedDistrict == null || address.selectedWard == null
        ) {
            viewModelScope.launch { _orderEvents.emit(OrderEvent.ShowError("Vui lòng nhập đủ thông tin nhận hàng")) }
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
            serviceId = null,
            serviceTypeId = 2,
            weight = 1000.0,
            length = 20.0,
            width = 20.0,
            height = 10.0,
            shippingFee = state.calculatedShippingFee,
            note = state.note.text.ifBlank { null },
            paymentTypeId = 1,
            configFeeId = null,
            extraCostId = null,
            requiredNote = null,
            coupon = null,
            pickShift = null
        )

        // 1. Platform Codes
        val platformDiscountCodes = if (state.selectedPlatformVoucher != null) {
            listOf(state.selectedPlatformVoucher.code)
        } else {
            emptyList()
        }

        // 2. Build Shop Requests (Mỗi shop kèm voucher của shop đó nếu có)
        val shopRequests = state.selectedShops.mapNotNull { detail ->
            val shopId = detail.shopId ?: return@mapNotNull null

            // Lấy voucher đã chọn cho shop này
            val shopVoucher = state.selectedShopVouchers[shopId]
            val shopDiscountCodes = if (shopVoucher != null) listOf(shopVoucher.code) else emptyList()

            ShopOrderRequestDomainEntity(
                shopId = shopId,
                receiver = receiver,
                cartItemIds = detail.cartItems.map { it.id },
                discountCodes = shopDiscountCodes, // ✅ Voucher Shop
                shippingInfo = defaultShippingInfo,
                isCod = false
            )
        }

        val finalRequest = CreateOrderRequestDomainEntity(
            shops = shopRequests,
            platformDiscountCodes = platformDiscountCodes // ✅ Voucher Platform
        )

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

        // --- NEW VOUCHER STATE ---
        // Voucher Sàn
        val selectedPlatformVoucher: DiscountDomainEntity? = null,
        // Voucher Shop: Map<ShopId, Voucher>
        val selectedShopVouchers: Map<String, DiscountDomainEntity> = emptyMap(),
        // Context: Đang chọn voucher cho ai? (null = Platform, String = ShopId)
        val currentSelectingShopId: String? = null,
        // Tất cả voucher lấy từ API
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