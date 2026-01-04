package com.ptit.core.manage_order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.order.OrderDomainEntity
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ManageOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    // Danh sách gốc từ API
    private val _originalOrders = MutableStateFlow<List<OrderDomainEntity>>(emptyList())

    // Trạng thái load/error
    private val _loadingState = MutableStateFlow<Boolean>(false)
    val loadingState = _loadingState.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    // Filter States
    private val _selectedStatus = MutableStateFlow<String?>(null) // null = Tất cả
    val selectedStatus = _selectedStatus.asStateFlow()

    private val _dateRange = MutableStateFlow<Pair<Long?, Long?>>(null to null) // Start - End
    val dateRange = _dateRange.asStateFlow()

    // Danh sách hiển thị (đã qua lọc Status + Date)
    val displayedOrders = combine(_originalOrders, _selectedStatus, _dateRange) { orders, status, dates ->
        var result = orders

        // 1. Lọc theo Tab (Status)
        // Lưu ý: Nếu chọn tab "Tất cả" (null) thì không lọc status
        if (status != null) {
            result = result.filter { it.status == status }
        }

        // 2. Lọc theo Date (Client-side filtering demo)
        // API thực tế nên hỗ trợ params startDate/endDate
        val (start, end) = dates
        if (start != null && end != null) {
            result = result.filter { order ->
                val orderDate = parseDate(order.createdAt)
                orderDate in start..end
            }
        }

        result
    }

    // --- Order Detail & Update Status (Giữ nguyên logic cũ) ---
    private val _orderDetailState = MutableStateFlow<Resource<OrderDomainEntity>>(Resource.Idle)
    val orderDetailState = _orderDetailState.asStateFlow()

    private val _updateStatusState = MutableStateFlow<Resource<OrderDomainEntity>>(Resource.Idle)
    val updateStatusState = _updateStatusState.asStateFlow()

    // Khởi tạo
    fun loadData() {
        // Lấy "Tất cả" đơn hàng về rồi lọc local (hoặc gọi API theo status nếu muốn mỗi tab load lại)
        // Ở đây demo lấy tất cả về 1 lần cho mượt
        getManageOrders(null)
    }

    fun getManageOrders(status: String? = null) {
        viewModelScope.launch {
            _loadingState.value = true
            _errorState.value = null

            // Gọi API lấy danh sách (Page 1, Limit 100 để demo filter local)
            val result = orderRepository.getManageOrders(page = 1, limit = 100, status = null) // Lấy tất cả về lọc local cho Tabs

            when (result) {
                is Resource.Success -> {
                    _originalOrders.value = result.data.data
                }
                is Resource.Error -> {
                    _errorState.value = result.error.message
                }
                else -> {}
            }
            _loadingState.value = false
        }
    }

    fun setStatusFilter(status: String?) {
        _selectedStatus.value = status
    }

    fun setDateFilter(start: Long?, end: Long?) {
        _dateRange.value = start to end
    }

    // ... (Các hàm getOrderDetail, confirmShipping giữ nguyên như cũ) ...
    fun getOrderDetail(orderId: String) {
        viewModelScope.launch {
            _orderDetailState.value = Resource.Loading()
            _orderDetailState.value = orderRepository.getManageOrderDetail(orderId)
        }
    }

    fun confirmShipping(orderId: String) {
        viewModelScope.launch {
            _updateStatusState.value = Resource.Loading()
            val result = orderRepository.updateOrderStatus(orderId, "DELIVERED")
            _updateStatusState.value = result
            if (result is Resource.Success) {
                getOrderDetail(orderId)
                loadData() // Reload list
            }
        }
    }

    fun resetUpdateStatusState() {
        _updateStatusState.value = Resource.Idle
    }

    // Helper parse date string to timestamp
    private fun parseDate(dateStr: String): Long {
        return try {
            // Định dạng ISO 8601 từ Backend
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}