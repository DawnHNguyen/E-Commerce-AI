package com.ptit.core.order_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.common.const.OrderStatus
import com.ptit.domain.entity.order.OrderDomainEntity
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class OrderHistoryUiState(
    val orders: List<OrderDomainEntity> = emptyList(),
    val filteredOrders: List<OrderDomainEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedStatus: String = OrderStatus.ALL,
    val startDate: Long? = null,
    val endDate: Long? = null
)

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = orderRepository.getMyOrders()) {
                is Resource.Success<List<OrderDomainEntity>> -> {
                    _uiState.value = _uiState.value.copy(
                        orders = result.data,
                        isLoading = false
                    )
                    applyFilters()
                }
                is Resource.Error<List<OrderDomainEntity>> -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.error.message ?: "Lỗi tải đơn hàng"
                    )
                }
                is Resource.Loading<List<OrderDomainEntity>> -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Resource.Idle -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun selectStatus(status: String) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
        applyFilters()
    }

    fun setDateRange(startDate: Long, endDate: Long) {
        _uiState.value = _uiState.value.copy(
            startDate = startDate,
            endDate = endDate
        )
        applyFilters()
    }

    fun clearDateFilter() {
        _uiState.value = _uiState.value.copy(
            startDate = null,
            endDate = null
        )
        applyFilters()
    }


    private fun applyFilters() {
        val currentState = _uiState.value
        var filtered = currentState.orders

        // Filter by status
        if (currentState.selectedStatus != OrderStatus.ALL) {
            filtered = filtered.filter { it.status == currentState.selectedStatus }
        }

        // Filter by date range
        if (currentState.startDate != null || currentState.endDate != null) {
            filtered = filtered.filter { order ->
                val orderDate = parseDate(order.createdAt)
                val start = currentState.startDate ?: 0L
                val end = currentState.endDate ?: Long.MAX_VALUE
                orderDate in start..end
            }
        }

        _uiState.value = currentState.copy(filteredOrders = filtered)
    }

    private fun parseDate(dateString: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.parse(dateString)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    fun refresh() {
        loadOrders()
    }
}

