package com.ptit.core.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.order.OrderDomainEntity
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _allOrders = MutableStateFlow<List<OrderDomainEntity>>(emptyList())

    private val _overviewState = MutableStateFlow<OverviewState>(OverviewState.Loading)
    val overviewState: StateFlow<OverviewState> = _overviewState.asStateFlow()

    private val _startDate = MutableStateFlow<Long?>(null)
    val startDate: StateFlow<Long?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Long?>(null)
    val endDate: StateFlow<Long?> = _endDate.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow<DateFilter?>(null)
    val selectedDateFilter: StateFlow<DateFilter?> = _selectedDateFilter.asStateFlow()

    // Sort order for orders list
    private val _sortDescending = MutableStateFlow(true) // Default: highest value first
    val sortDescending: StateFlow<Boolean> = _sortDescending.asStateFlow()

    init {
        loadAllOrders()
    }

    fun applyPresetFilter(filter: DateFilter) {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        val startDate = when (filter) {
            DateFilter.ONE_DAY -> {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                calendar.timeInMillis
            }
            DateFilter.THREE_DAYS -> {
                calendar.add(Calendar.DAY_OF_MONTH, -3)
                calendar.timeInMillis
            }

            DateFilter.ONE_WEEK -> {
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                calendar.timeInMillis
            }
            DateFilter.ONE_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }
            DateFilter.THREE_MONTHS -> {
                calendar.add(Calendar.MONTH, -3)
                calendar.timeInMillis
            }
            DateFilter.SIX_MONTHS -> {
                calendar.add(Calendar.MONTH, -6)
                calendar.timeInMillis
            }
            DateFilter.NINE_MONTHS -> {
                calendar.add(Calendar.MONTH, -9)
                calendar.timeInMillis
            }
            DateFilter.ONE_YEAR -> {
                calendar.add(Calendar.YEAR, -1)
                calendar.timeInMillis
            }
            DateFilter.CUSTOM -> return // Don't apply for custom
        }

        _startDate.value = startDate
        _endDate.value = endDate
        _selectedDateFilter.value = filter
        filterOrders()
    }

    fun setCustomDateRange(startDate: Long, endDate: Long) {
        _startDate.value = startDate
        _endDate.value = endDate
        _selectedDateFilter.value = DateFilter.CUSTOM
        filterOrders()
    }

    fun clearDateFilter() {
        _startDate.value = null
        _endDate.value = null
        _selectedDateFilter.value = null
        filterOrders()
    }

    fun toggleSortOrder() {
        _sortDescending.value = !_sortDescending.value
        filterOrders()
    }

    fun refresh() {
        loadAllOrders()
    }

    private fun loadAllOrders() {
        viewModelScope.launch {
            _overviewState.value = OverviewState.Loading

            // Load ALL orders from API (no filtering on backend)
            when (val result = orderRepository.getOrders(
                status = null,
                page = 1,
                limit = 1000 // Get all orders
            )) {
                is Resource.Success -> {
                    _allOrders.value = result.data.data
                    filterOrders()
                }
                is Resource.Error -> {
                    _overviewState.value = OverviewState.Error(
                        result.error.message ?: "Không thể tải dữ liệu"
                    )
                }
                else -> {}
            }
        }
    }

    private fun filterOrders() {
        val startDate = _startDate.value
        val endDate = _endDate.value

        // Filter out PENDING, CANCELLED, RETURNED orders (only count paid/active orders)
        val excludedStatuses = setOf("PENDING", "PENDING_PAYMENT", "CANCELLED", "RETURNED")
        val validOrders = _allOrders.value.filter { order ->
            !excludedStatuses.contains(order.status.uppercase())
        }

        // Filter by date range if selected
        val filteredOrders = if (startDate != null && endDate != null) {
            validOrders.filter { order ->
                val orderDate = parseDate(order.createdAt)
                orderDate in startDate..endDate
            }
        } else {
            validOrders
        }

        val totalOrders = filteredOrders.size
        val totalSpent = filteredOrders.sumOf { it.totalPayment }

        // Sort orders by total payment
        val sortedOrders = if (_sortDescending.value) {
            filteredOrders.sortedByDescending { it.totalPayment }
        } else {
            filteredOrders.sortedBy { it.totalPayment }
        }

        _overviewState.value = OverviewState.Success(
            totalOrders = totalOrders,
            totalSpent = totalSpent,
            filteredOrders = sortedOrders
        )
    }

    private fun parseDate(dateString: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.parse(dateString)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    sealed class OverviewState {
        object Loading : OverviewState()
        data class Success(
            val totalOrders: Int,
            val totalSpent: Int,
            val filteredOrders: List<OrderDomainEntity>
        ) : OverviewState()
        data class Error(val message: String) : OverviewState()
    }

    enum class DateFilter(val displayName: String) {
        ONE_DAY("1 ngày"),
        THREE_DAYS("3 ngày"),
        ONE_WEEK("1 tuần"),
        ONE_MONTH("1 tháng"),
        THREE_MONTHS("3 tháng"),
        SIX_MONTHS("6 tháng"),
        NINE_MONTHS("9 tháng"),
        ONE_YEAR("12 tháng"),
        CUSTOM("Tùy chọn")
    }
}

