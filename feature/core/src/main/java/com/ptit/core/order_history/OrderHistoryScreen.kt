package com.ptit.core.order_history

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.ptit.common.R
import com.ptit.common.const.OrderStatus
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.order.OrderDomainEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: OrderHistoryViewModel = hiltViewModel()
) {
    LocalBottomNavigationVisibility.current.value = false

    val uiState by viewModel.uiState.collectAsState()
    val primaryColor = colorResource(id = R.color.colorSystem_heading_button)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Lịch sử đơn hàng",
                        style = CustomTypography.TextBold,
                        fontSize = 20.sp,
                        color = colorResource(id = R.color.colorSystem_greyscale_0_white)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colorResource(id = R.color.colorSystem_greyscale_0_white)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = primaryColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(id = R.color.colorSystem_background_level_0))
        ) {
            // Status Tabs with Green indicator
            StatusTabRow(
                selectedStatus = uiState.selectedStatus,
                onStatusSelected = { viewModel.selectStatus(it) },
                primaryColor = primaryColor
            )

            // Date Filter Section
            DateFilterSection(
                startDate = uiState.startDate,
                endDate = uiState.endDate,
                onDateRangeSelected = { start, end ->
                    viewModel.setDateRange(start, end)
                },
                onClearFilter = {
                    viewModel.clearDateFilter()
                },
                primaryColor = primaryColor
            )

            // Orders List
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "⚠️", fontSize = 48.sp)
                            Text(
                                text = uiState.error ?: "Đã xảy ra lỗi",
                                style = CustomTypography.TextRegular,
                                color = Color.Red
                            )
                            Button(
                                onClick = { viewModel.refresh() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor
                                )
                            ) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                uiState.filteredOrders.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(text = "📦", fontSize = 64.sp)
                            Text(
                                text = "Không có đơn hàng nào",
                                style = CustomTypography.TextBold,
                                fontSize = 18.sp,
                                color = primaryColor
                            )
                            Text(
                                text = "Các đơn hàng của bạn sẽ hiển thị tại đây",
                                style = CustomTypography.TextRegular,
                                fontSize = 14.sp,
                                color = colorResource(id = R.color.colorSystem_greyscale_600)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.filteredOrders, key = { it.id }) { order ->
                            OrderItemCard(
                                order = order,
                                onViewDetail = { onNavigateToDetail(order.id) },
                                primaryColor = primaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusTabRow(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    primaryColor: Color
) {
    ScrollableTabRow(
        selectedTabIndex = OrderStatus.getAllStatuses().indexOf(selectedStatus),
        containerColor = colorResource(id = R.color.colorSystem_background_level_2),
        contentColor = primaryColor,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[OrderStatus.getAllStatuses().indexOf(selectedStatus)]),
                color = primaryColor
            )
        },
        edgePadding = 0.dp
    ) {
        OrderStatus.getAllStatuses().forEach { status ->
            Tab(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                selectedContentColor = primaryColor,
                unselectedContentColor = colorResource(id = R.color.colorSystem_greyscale_600),
                text = {
                    Text(
                        text = OrderStatus.getDisplayName(status),
                        style = CustomTypography.TextSemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
private fun DateFilterSection(
    startDate: Long?,
    endDate: Long?,
    onDateRangeSelected: (Long, Long) -> Unit,
    onClearFilter: () -> Unit,
    primaryColor: Color
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.colorSystem_background_level_2))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date Picker Button
        OutlinedButton(
            onClick = {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val start = Calendar.getInstance().apply {
                            set(year, month, day, 0, 0, 0)
                        }.timeInMillis

                        // Show end date picker
                        DatePickerDialog(
                            context,
                            { _, year2, month2, day2 ->
                                val end = Calendar.getInstance().apply {
                                    set(year2, month2, day2, 23, 59, 59)
                                }.timeInMillis
                                onDateRangeSelected(start, end)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = primaryColor,
                containerColor = if (startDate != null && endDate != null) {
                    primaryColor.copy(alpha = 0.1f)
                } else {
                    Color.Transparent
                }
            ),
            border = BorderStroke(1.dp, primaryColor)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = primaryColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (startDate != null && endDate != null) {
                    "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
                } else {
                    "Chọn khoảng thời gian"
                },
                style = CustomTypography.TextSemiBold,
                fontSize = 14.sp
            )
        }

        // Clear Button (only show when has date range)
        if (startDate != null && endDate != null) {
            TextButton(
                onClick = onClearFilter,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = primaryColor
                )
            ) {
                Text(
                    text = "Xóa",
                    style = CustomTypography.TextSemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun OrderItemCard(
    order: OrderDomainEntity,
    onViewDetail: () -> Unit,
    primaryColor: Color
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    val orderDate = try {
        val parseFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parseFormat.parse(order.createdAt)?.let { dateFormat.format(it) } ?: order.createdAt
    } catch (_: Exception) {
        order.createdAt
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewDetail),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Order ID and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đơn hàng #${order.orderCode ?: order.id.takeLast(8)}",
                    style = CustomTypography.TextBold,
                    fontSize = 16.sp,
                    color = primaryColor
                )

                Text(
                    text = orderDate,
                    style = CustomTypography.TextRegular,
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.colorSystem_greyscale_600)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Badge
            Box(
                modifier = Modifier
                    .background(
                        color = Color(android.graphics.Color.parseColor(OrderStatus.getStatusColor(order.status))).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = OrderStatus.getDisplayName(order.status),
                    style = CustomTypography.TextSemiBold,
                    fontSize = 12.sp,
                    color = Color(android.graphics.Color.parseColor(OrderStatus.getStatusColor(order.status)))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Product Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product Image
                val firstItem = order.items?.firstOrNull()
                if (firstItem != null) {
                    GlideImage(
                        model = firstItem.image,
                        contentDescription = firstItem.productName,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorResource(id = R.color.colorSystem_greyscale_200)),
                        contentScale = ContentScale.Crop,
                        loading = placeholder {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = primaryColor
                                )
                            }
                        },
                        failure = placeholder {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(colorResource(id = R.color.colorSystem_greyscale_200)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "📦", fontSize = 24.sp)
                            }
                        }
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = firstItem.productName,
                            style = CustomTypography.TextSemiBold,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = primaryColor
                        )

                        val remainingCount = (order.items?.size ?: 0) - 1
                        if (remainingCount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "và $remainingCount sản phẩm khác...",
                                style = CustomTypography.TextRegular,
                                fontSize = 12.sp,
                                color = colorResource(id = R.color.colorSystem_greyscale_600)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = colorResource(id = R.color.colorSystem_greyscale_200))

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Total and View Detail Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tổng thanh toán",
                        style = CustomTypography.TextRegular,
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.colorSystem_greyscale_600)
                    )
                    Text(
                        text = currencyFormat.format(order.totalPayment),
                        style = CustomTypography.TextBold,
                        fontSize = 18.sp,
                        color = colorResource(id = R.color.colorSystem_tint_red)
                    )
                }

                Button(
                    onClick = onViewDetail,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Xem chi tiết",
                        style = CustomTypography.TextSemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
