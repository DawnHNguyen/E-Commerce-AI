package com.ptit.core.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.order.OrderDomainEntity
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderDetailState(
    val isLoading: Boolean = false,
    val order: OrderDomainEntity? = null,
    val error: String? = null
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orderState = MutableStateFlow(OrderDetailState())
    val orderState = _orderState.asStateFlow()

    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true, error = null) }

            when (val result = orderRepository.getOrderById(orderId)) {
                is Resource.Success -> {
                    _orderState.update {
                        it.copy(
                            isLoading = false,
                            order = result.data,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _orderState.update {
                        it.copy(
                            isLoading = false,
                            error = "Không thể tải thông tin đơn hàng: ${result.error.message}"
                        )
                    }
                }
                else -> _orderState.update { it.copy(isLoading = false) }
            }
        }
    }
}