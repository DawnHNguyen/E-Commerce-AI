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

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orderState = MutableStateFlow<Resource<OrderDomainEntity>>(Resource.idle())
    val orderState = _orderState.asStateFlow()

    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            _orderState.value = Resource.loading()

            _orderState.update {
                orderRepository.getOrderById(orderId)
            }
        }
    }
}