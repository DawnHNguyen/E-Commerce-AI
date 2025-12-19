package com.ptit.core.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.order.CancelOrderResponseDomainEntity
import com.ptit.domain.entity.order.OrderDomainEntity
import com.ptit.domain.entity.payment.ProcessPaymentDomainEntity
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.repository.PaymentMethodRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    private val _orderState = MutableStateFlow<Resource<OrderDomainEntity>>(Resource.idle())
    val orderState = _orderState.asStateFlow()


    // 🛑 Trạng thái khi huỷ đơn hàng
    private val _cancelOrderState =
        MutableStateFlow<Resource<CancelOrderResponseDomainEntity>>(Resource.idle())
    val cancelOrderState = _cancelOrderState.asStateFlow()

    // 💳 Trạng thái khi thanh toán
    private val _processPaymentState =
        MutableStateFlow<Resource<ProcessPaymentDomainEntity>>(Resource.idle())
    val processPaymentState = _processPaymentState.asStateFlow()

    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            _orderState.value = Resource.loading()
            _orderState.update {
                orderRepository.getOrderById(orderId)
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _cancelOrderState.value = Resource.loading()
            _cancelOrderState.update {
                orderRepository.cancelOrder(orderId)
            }
        }
    }

    fun processPayment(orderId: String, tokenId: String) {
        viewModelScope.launch {
            _processPaymentState.value = Resource.loading()
            _processPaymentState.update {
                paymentMethodRepository.processPayment(
                    orderId = orderId,
                    tokenId = tokenId,
                    currency = "VND"
                )
            }
        }
    }

    fun reloadOrderAfterPayment(orderId: String) {
        viewModelScope.launch {
            // Keep loading indicator visible
            _orderState.value = Resource.loading()

            // Small delay to ensure backend has updated order status
            kotlinx.coroutines.delay(1000)

            // Reset payment state first
            _processPaymentState.value = Resource.idle()

            // Reload order details
            _orderState.update {
                orderRepository.getOrderById(orderId)
            }
        }
    }

    fun resetPaymentState() {
        _processPaymentState.value = Resource.idle()
    }
}
