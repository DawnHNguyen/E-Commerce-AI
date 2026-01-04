package com.ptit.core.manage_order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.toPriceFormat
import com.ptit.domain.entity.order.OrderDomainEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOrderListScreen(
    viewModel: ManageOrderViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    LocalBottomNavigationVisibility.current.value = false

    // States from ViewModel
    val filteredOrders by viewModel.displayedOrders.collectAsStateWithLifecycle(initialValue = emptyList())
    val isLoading by viewModel.loadingState.collectAsStateWithLifecycle()
    val error by viewModel.errorState.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatus.collectAsStateWithLifecycle()
    val dateRange by viewModel.dateRange.collectAsStateWithLifecycle()

    // Local UI States
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // List of Tabs
    val tabs = listOf(
        "Tất cả" to null,
        "Chờ thanh toán" to "PENDING_PAYMENT",
        "Chờ vận chuyển" to "PENDING_PACKAGING",
        "Đã giao" to "DELIVERED"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quản lý đơn hàng",
                        // Update: Cỡ chữ to hơn, màu trắng
                        style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                        color = colorResource(R.color.colorSystem_greyscale_0_white)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(R.color.colorSystem_greyscale_0_white)
                        )
                    }
                },
                // Update: Màu nền xanh heading_button
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button)
                )
            )
        },
        containerColor = colorResource(R.color.colorSystem_background_level_0)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 1. TABS SECTION
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOfFirst { it.second == selectedStatus }.takeIf { it >= 0 } ?: 0,
                containerColor = colorResource(R.color.colorSystem_background_level_2),
                contentColor = colorResource(R.color.colorSystem_heading_button),
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[tabs.indexOfFirst { it.second == selectedStatus }.takeIf { it >= 0 } ?: 0]),
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                }
            ) {
                tabs.forEach { (title, statusKey) ->
                    Tab(
                        selected = selectedStatus == statusKey,
                        onClick = { viewModel.setStatusFilter(statusKey) },
                        text = {
                            Text(
                                text = title,
                                style = if (selectedStatus == statusKey) CustomTypography.TextBold else CustomTypography.TextMedium,
                                color = if (selectedStatus == statusKey)
                                    colorResource(R.color.colorSystem_heading_button)
                                else
                                    colorResource(R.color.colorSystem_greyscale_500)
                            )
                        }
                    )
                }
            }

            // 2. FILTER SECTION
            FilterSection(
                dateRange = dateRange,
                onDateClick = { showDatePicker = true },
                onClearDate = { viewModel.setDateFilter(null, null) }
            )

            // 3. ORDER LIST
            Box(modifier = Modifier.weight(1f)) {
                if (isLoading) {
                    FullScreenProgressBar()
                } else if (error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Lỗi: $error", color = colorResource(R.color.colorSystem_error))
                    }
                } else if (filteredOrders.isEmpty()) {
                    EmptyOrderState()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredOrders) { order ->
                            ManageOrderItem(
                                order = order,
                                onClick = { onNavigateToDetail(order.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { start ->
                        // Đơn giản hóa: Chọn 1 ngày -> Lọc trong ngày đó
                        // Hoặc bạn có thể dùng DateRangePicker
                        val end = start + 86400000L // + 24h
                        viewModel.setDateFilter(start, end)
                    }
                    showDatePicker = false
                }) {
                    Text("Chọn", color = colorResource(R.color.colorSystem_heading_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy", color = colorResource(R.color.colorSystem_greyscale_500))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun FilterSection(
    dateRange: Pair<Long?, Long?>,
    onDateClick: () -> Unit,
    onClearDate: () -> Unit
) {
    val (start, end) = dateRange
    val hasFilter = start != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.colorSystem_background_level_1))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            tint = colorResource(R.color.colorSystem_greyscale_500),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Lọc theo ngày:",
            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
            color = colorResource(R.color.colorSystem_greyscale_600)
        )
        Spacer(modifier = Modifier.width(12.dp))

        // Date Filter Chip
        Surface(
            modifier = Modifier
                .clickable { onDateClick() }
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    if (hasFilter) colorResource(R.color.colorSystem_heading_button) else colorResource(R.color.colorSystem_greyscale_300),
                    RoundedCornerShape(8.dp)
                ),
            color = if (hasFilter) colorResource(R.color.colorSystem_heading_button).copy(alpha = 0.1f) else Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (hasFilter) colorResource(R.color.colorSystem_heading_button) else colorResource(R.color.colorSystem_greyscale_500)
                )
                Spacer(modifier = Modifier.width(8.dp))

                val text = if (hasFilter) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdf.format(Date(start!!))
                } else {
                    "Tất cả thời gian"
                }

                Text(
                    text = text,
                    style = CustomTypography.TextMedium.copy(fontSize = 13.sp),
                    color = if (hasFilter) colorResource(R.color.colorSystem_heading_button) else colorResource(R.color.colorSystem_greyscale_600)
                )

                if (hasFilter) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onClearDate() },
                        tint = colorResource(R.color.colorSystem_heading_button)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ManageOrderItem(
    order: OrderDomainEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.colorSystem_background_level_2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Order ID & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đơn #${order.orderCode ?: order.id.takeLast(8)}",
                    style = CustomTypography.TextBold.copy(fontSize = 14.sp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )

                // Status Badge Update
                val statusColor = getStatusColor(order.status)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = getStatusText(order.status),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = CustomTypography.TextSemiBold.copy(fontSize = 12.sp),
                        color = statusColor
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = colorResource(R.color.colorSystem_greyscale_100)
            )

            // Product Info
            order.items?.firstOrNull()?.let { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Placeholder for image if needed
                    GlideImage(
                        model = item.image, // URL ảnh từ item
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp) // Tăng kích thước một chút cho đẹp
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, colorResource(R.color.colorSystem_greyscale_200), RoundedCornerShape(8.dp))
                    ) {
                        it.centerCrop() // Scale ảnh cho vừa khung
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.productName,
                            style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
                            color = colorResource(R.color.colorSystem_greyscale_900),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Phân loại: ${item.skuValue}",
                                style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                                color = colorResource(R.color.colorSystem_greyscale_500)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "x${item.quantity}",
                                style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                                color = colorResource(R.color.colorSystem_greyscale_600)
                            )
                        }
                    }
                }
            }

            if ((order.items?.size ?: 0) > 1) {
                Text(
                    text = "Xem thêm ${order.items!!.size - 1} sản phẩm...",
                    style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                    color = colorResource(R.color.colorSystem_text_button),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Total Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.items?.sumOf { it.quantity }} sản phẩm",
                    style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                    color = colorResource(R.color.colorSystem_greyscale_600)
                )

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Thành tiền: ",
                        style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                        color = colorResource(R.color.colorSystem_greyscale_700)
                    )
                    Text(
                        text = order.totalPayment.toPriceFormat(),
                        style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                        color = colorResource(R.color.colorSystem_tint_red)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyOrderState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon (Dùng tạm icon có sẵn hoặc resource ảnh)
        Icon(
            imageVector = Icons.Default.Inventory,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colorResource(R.color.colorSystem_greyscale_300)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có đơn hàng nào",
            style = CustomTypography.TextBold.copy(fontSize = 18.sp),
            color = colorResource(R.color.colorSystem_greyscale_600)
        )
        Text(
            text = "Các đơn hàng mới sẽ xuất hiện tại đây",
            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
            color = colorResource(R.color.colorSystem_greyscale_500)
        )
    }
}

// Helper functions (Update colors based on color_system)
@Composable
fun getStatusColor(status: String): Color {
    return when (status) {
        "PENDING_PAYMENT" -> colorResource(R.color.colorSystem_tint_yellow) // Cam/Vàng
        "PENDING_PACKAGING" -> colorResource(R.color.colorSystem_tint_blue) // Xanh dương
        "DELIVERED" -> colorResource(R.color.colorSystem_tint_green) // Xanh lá
        else -> colorResource(R.color.colorSystem_greyscale_500)
    }
}

fun getStatusText(status: String): String {
    return when (status) {
        "PENDING_PAYMENT" -> "Chờ thanh toán"
        "PENDING_PACKAGING" -> "Chờ vận chuyển"
        "DELIVERED" -> "Đã giao hàng"
        else -> status
    }
}