package com.ptit.core.order

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.cart.CartItemDetailDomainEntity
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.order.CreateOrderRequestDomainEntity
import com.ptit.domain.entity.order.CreateOrderResponseDomainEntity
import com.ptit.domain.entity.order.ReceiverDomainEntity
import com.ptit.domain.repository.CartRepository
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.repository.UserRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _orderState = MutableStateFlow(OrderFormState())
    val orderState: StateFlow<OrderFormState> = _orderState.asStateFlow()

    private val _orderEvents = MutableSharedFlow<OrderEvent>()
    val orderEvents: SharedFlow<OrderEvent> = _orderEvents.asSharedFlow()

    private val _userProfileState = MutableStateFlow<Resource<UserDomainEntity>>(Resource.idle())
    val userProfileState: StateFlow<Resource<UserDomainEntity>> = _userProfileState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _userProfileState.update { Resource.loading() }
            when (val result = userRepository.getUserProfile()) {
                is Resource.Success -> {
                    _userProfileState.update { Resource.success(result.data) }
                    result.data?.let { user ->
                        _orderState.update {
                            it.copy(
                                name = TextFieldValue(user.name),
                                phone = TextFieldValue(user.phoneNumber),
                                address = TextFieldValue(""),
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

    /**
     * 🛒 Lấy các cartItemDetail (theo shop)
     */
    fun setSelectedItemIds(itemIds: List<String>) {
        if (itemIds.isEmpty()) {
            viewModelScope.launch {
                _orderEvents.emit(OrderEvent.ShowError("Chưa chọn sản phẩm nào"))
            }
            return
        }

        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true, error = null) }

            val selectedShopGroups = mutableListOf<CartItemDetailDomainEntity>()
            var hasError = false

            for (id in itemIds) {
                when (val result = cartRepository.getCartItemById(id)) {
                    is Resource.Success -> result.data?.let { selectedShopGroups.add(it) }
                    is Resource.Error -> {
                        hasError = true
                        _orderEvents.emit(OrderEvent.ShowError("Không tải được sản phẩm ID: $id"))
                    }
                    else -> {}
                }
            }

            _orderState.update {
                it.copy(
                    isLoading = false,
                    selectedShops = selectedShopGroups,
                    error = if (hasError) "Một số sản phẩm không tải được" else null
                )
            }
        }
    }

    fun updateName(value: TextFieldValue) = _orderState.update { it.copy(name = value) }
    fun updatePhone(value: TextFieldValue) = _orderState.update { it.copy(phone = value) }
    fun updateAddress(value: TextFieldValue) = _orderState.update { it.copy(address = value) }
    fun updateNote(value: TextFieldValue) = _orderState.update { it.copy(note = value) }

    /**
     * 🧾 Tạo đơn hàng theo từng shop
     */
    fun createOrder() {
        val state = _orderState.value

        if (state.selectedShops.isEmpty()) {
            viewModelScope.launch { _orderEvents.emit(OrderEvent.ShowError("Không có sản phẩm nào được chọn")) }
            return
        }

        if (state.name.text.isBlank() || state.phone.text.isBlank() || state.address.text.isBlank()) {
            viewModelScope.launch { _orderEvents.emit(OrderEvent.ShowError("Vui lòng nhập đủ thông tin giao hàng")) }
            return
        }

        val receiver = ReceiverDomainEntity(
            name = state.name.text,
            phone = state.phone.text,
            address = state.address.text
        )

        // ✅ Mỗi CartItemDetailDomainEntity tương ứng với 1 shopId → 1 request
        val orderRequests = state.selectedShops.mapNotNull { detail ->
            val shopId = detail.shopId ?: return@mapNotNull null
            CreateOrderRequestDomainEntity(
                shopId = shopId,
                receiver = receiver,
                cartItemIds = detail.cartItems.map { it.id }
            )
        }

        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true) }

            when (val result = orderRepository.createOrder(orderRequests)) {
                is Resource.Success -> {
                    _orderState.update { it.copy(isLoading = false) }
                    result.data?.let { _orderEvents.emit(OrderEvent.OrderCreated(it)) }
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
        val selectedShops: List<CartItemDetailDomainEntity> = emptyList(), // ✅ Mỗi shop là 1 group
        val name: TextFieldValue = TextFieldValue(""),
        val phone: TextFieldValue = TextFieldValue(""),
        val address: TextFieldValue = TextFieldValue(""),
        val note: TextFieldValue = TextFieldValue(""),
        val isUserInfoLoaded: Boolean = false
    )

    sealed class OrderEvent {
        data class ShowError(val message: String) : OrderEvent()
        data class OrderCreated(val response: CreateOrderResponseDomainEntity) : OrderEvent()
    }
}
