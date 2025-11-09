package com.ptit.core.order.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.order.GetOrderListDomainEntity
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orderListState =
        MutableStateFlow<Resource<GetOrderListDomainEntity>>(Resource.idle())
    val orderListState = _orderListState.asStateFlow()

    init {
        loadOrderHistory()
    }

    fun loadOrderHistory() {
        viewModelScope.launch {
            _orderListState.value = Resource.loading()
            // Gọi API GET /orders (lấy danh sách)
            val result = orderRepository.getOrders(page = 1, limit = 20)
            _orderListState.update { result }
        }
    }
}