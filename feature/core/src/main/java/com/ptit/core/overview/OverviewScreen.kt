package com.ptit.core.overview

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.toPriceFormat
import com.ptit.domain.entity.order.OrderDomainEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    onBack: () -> Unit,
    onViewAllOrders: () -> Unit,
    onOrderClick: (String) -> Unit,
    viewModel: OverviewViewModel = hiltViewModel()
) {
    val overviewState by viewModel.overviewState.collectAsState()
    val sortDescending by viewModel.sortDescending.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsState()

    val primaryColor = colorResource(R.color.colorSystem_heading_button)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Thống kê chi tiêu",
                            style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button),
                    titleContentColor = colorResource(R.color.colorSystem_greyscale_0_white)
                )
            )
        }
    ) { paddingValues ->
        when (val state = overviewState) {
            is OverviewViewModel.OverviewState.Loading -> {
                FullScreenProgressBar()
            }
            is OverviewViewModel.OverviewState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            style = CustomTypography.TextRegular,
                            color = Color.Red
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            is OverviewViewModel.OverviewState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(colorResource(R.color.colorSystem_background_level_0)),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date Filter Section with preset chips + custom date picker
                    item {
                        DateFilterSection(
                            selectedFilter = selectedDateFilter,
                            startDate = startDate,
                            endDate = endDate,
                            onFilterSelected = { filter ->
                                if (filter == OverviewViewModel.DateFilter.CUSTOM) {
                                    // Will be handled by date picker button
                                } else {
                                    viewModel.applyPresetFilter(filter)
                                }
                            },
                            onDateRangeSelected = { start, end ->
                                viewModel.setCustomDateRange(start, end)
                            },
                            onClearFilter = {
                                viewModel.clearDateFilter()
                            },
                            primaryColor = primaryColor
                        )
                    }

                    // Statistics Cards
                    item {
                        StatisticsSection(
                            totalOrders = state.totalOrders,
                            totalSpent = state.totalSpent
                        )
                    }

                    // Order List Section
                    item {
                        OrderListSection(
                            orders = state.filteredOrders,
                            sortDescending = sortDescending,
                            onToggleSort = { viewModel.toggleSortOrder() },
                            onOrderClick = onOrderClick,
                            primaryColor = primaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateFilterSection(
    selectedFilter: OverviewViewModel.DateFilter?,
    startDate: Long?,
    endDate: Long?,
    onFilterSelected: (OverviewViewModel.DateFilter) -> Unit,
    onDateRangeSelected: (Long, Long) -> Unit,
    onClearFilter: () -> Unit,
    primaryColor: Color
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(OverviewViewModel.DateFilter.entries.filter { it != OverviewViewModel.DateFilter.CUSTOM }) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(filter.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Custom Date Picker Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    containerColor = if (selectedFilter == OverviewViewModel.DateFilter.CUSTOM) {
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
                    text = if (selectedFilter == OverviewViewModel.DateFilter.CUSTOM && startDate != null && endDate != null) {
                        "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
                    } else {
                        "Chọn khoảng thời gian"
                    },
                    style = CustomTypography.TextSemiBold,
                    fontSize = 14.sp
                )
            }

            // Clear Button (only show when has filter)
            if (selectedFilter != null) {
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
}

@Composable
private fun StatisticsSection(
    totalOrders: Int,
    totalSpent: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total Orders Card
        StatisticCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Tổng đơn hàng",
            value = totalOrders.toString(),
            backgroundColor = colorResource(R.color.colorSystem_heading_button),
            icon = Icons.Default.ShoppingBag
        )

        // Total Spent Card
        StatisticCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Tổng chi tiêu",
            value = totalSpent.toPriceFormat(),
            backgroundColor = Color(0xFFFF6B6B),
            icon = Icons.Default.AttachMoney
        )
    }
}

@Composable
private fun StatisticCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    backgroundColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = value,
                    style = CustomTypography.TextBold.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun OrderListSection(
    orders: List<OrderDomainEntity>,
    sortDescending: Boolean,
    onToggleSort: () -> Unit,
    onOrderClick: (String) -> Unit,
    primaryColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Danh sách đơn hàng",
                style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                color = colorResource(R.color.colorSystem_heading_button)
            )

            // Sort button only
            IconButton(onClick = onToggleSort) {
                Icon(
                    imageVector = if (sortDescending) {
                        Icons.Default.ArrowDownward
                    } else {
                        Icons.Default.ArrowUpward
                    },
                    contentDescription = if (sortDescending) "Giảm dần" else "Tăng dần",
                    tint = colorResource(R.color.colorSystem_heading_button)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không có đơn hàng nào",
                    style = CustomTypography.TextRegular,
                    color = colorResource(R.color.colorSystem_greyscale_500)
                )
            }
        } else {
            orders.forEachIndexed { index, order ->
                OrderItemRow(
                    order = order,
                    onClick = { onOrderClick(order.id) },
                    primaryColor = primaryColor
                )
                if (index < orders.size - 1) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = colorResource(R.color.colorSystem_greyscale_200))
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun OrderItemRow(
    order: OrderDomainEntity,
    onClick: () -> Unit,
    primaryColor: Color
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val orderDate = try {
        val parseFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parseFormat.parse(order.createdAt)?.let { dateFormat.format(it) } ?: order.createdAt
    } catch (_: Exception) {
        order.createdAt
    }

    val orderItems = order.items ?: emptyList()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Order ID and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đơn hàng #${order.id.takeLast(8)}",
                    style = CustomTypography.TextBold.copy(fontSize = 14.sp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )
                Text(
                    text = orderDate,
                    style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                    color = colorResource(R.color.colorSystem_greyscale_500)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Order Items
            orderItems.take(2).forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Product Image
                    GlideImage(
                        model = item.image,
                        contentDescription = item.productName,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Product Info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.productName,
                            style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
                            color = colorResource(R.color.colorSystem_normal_text),
                            maxLines = 2
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "x${item.quantity}",
                            style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                            color = colorResource(R.color.colorSystem_greyscale_500)
                        )
                    }

                    // Item Price
                    Text(
                        text = item.skuPrice.toPriceFormat(),
                        style = CustomTypography.TextSemiBold.copy(fontSize = 14.sp),
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                }

                if (index < 1 && orderItems.size > 1) {
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Show more items indicator
            if (orderItems.size > 2) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "và ${orderItems.size - 2} sản phẩm khác...",
                    style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                    color = colorResource(R.color.colorSystem_greyscale_500)
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = colorResource(R.color.colorSystem_greyscale_200))
            Spacer(Modifier.height(12.dp))

            // Status and Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status
                val statusColor = getStatusColor(order.status)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = getStatusDisplayName(order.status),
                        style = CustomTypography.TextMedium.copy(fontSize = 12.sp),
                        color = statusColor
                    )
                }

                // Total Payment
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Tổng thanh toán",
                        style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                        color = colorResource(R.color.colorSystem_greyscale_500)
                    )
                    Text(
                        text = order.totalPayment.toPriceFormat(),
                        style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                        color = primaryColor
                    )
                }
            }
        }
    }
}

private fun getStatusDisplayName(status: String): String {
    return when (status.uppercase()) {
        "PENDING", "PENDING_PAYMENT" -> "Chờ thanh toán"
        "PROCESSING", "PENDING_PACKAGE", "PENDING_PACKAGING" -> "Chờ vận chuyển"
        "SHIPPING", "PENDING_DELIVERY", "PICKUPED" -> "Đang giao hàng"
        "DELIVERED" -> "Đã giao"
        "CANCELLED" -> "Đã hủy"
        "RETURNED" -> "Trả hàng"
        else -> status
    }
}

@Composable
private fun getStatusColor(status: String): Color {
    val normalizedStatus = status.uppercase()
    val colorHex = when (normalizedStatus) {
        "PENDING", "PENDING_PAYMENT" -> "#FFA500"
        "PROCESSING", "PENDING_PACKAGE", "PENDING_PACKAGING" -> "#FFD700"
        "SHIPPING", "PENDING_DELIVERY", "PICKUPED" -> "#9C27B0"
        "DELIVERED" -> "#4CAF50"
        "RETURNED" -> "#FF9800"
        "CANCELLED" -> "#F44336"
        else -> "#757575"
    }
    return Color(android.graphics.Color.parseColor(colorHex))
}

