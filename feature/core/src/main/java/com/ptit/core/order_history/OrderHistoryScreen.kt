package com.ptit.core.order.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ptit.common.R
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.toPriceFormat
import com.ptit.domain.entity.order.OrderDomainEntity
import com.ptit.domain.utils.Resource
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: OrderHistoryViewModel = hiltViewModel()
) {
    LocalBottomNavigationVisibility.current.value = false
    val orderListState by viewModel.orderListState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Tải lại dữ liệu khi màn hình được focus
    LaunchedEffect(Unit) {
        viewModel.loadOrderHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Lịch sử đơn hàng",
                            style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(R.color.colorSystem_background_level_0))
        ) {
            when (val state = orderListState) {
                is Resource.Loading -> FullScreenProgressBar()
                is Resource.Success -> {
                    val orders = state.data.data
                    if (orders.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Bạn chưa có đơn hàng nào", style = CustomTypography.TextMedium)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(orders, key = { it.id }) { order ->
                                OrderHistoryItemRow(
                                    order = order,
                                    onClick = { onNavigateToDetail(order.id) }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            state.error.message ?: "Lỗi tải lịch sử đơn hàng",
                            style = CustomTypography.TextMedium,
                            color = Color.Red
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun OrderHistoryItemRow(
    order: OrderDomainEntity,
    onClick: () -> Unit
) {
    // 🔴 SỬA: Unwrap nullable items ngay đầu
    val itemsList = order.items ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Đơn hàng #${order.id.take(8)}...",
                style = CustomTypography.TextSemiBold,
                color = colorResource(R.color.colorSystem_heading_button)
            )
            OrderStatusBadge(status = order.status)
        }

        // 🔴 SỬA: Dùng itemsList thay vì order.items
        itemsList.firstOrNull()?.let { firstItem ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${firstItem.quantity} x ",
                    style = CustomTypography.TextRegular,
                    color = colorResource(R.color.colorSystem_normal_text)
                )
                Text(
                    text = firstItem.productName,
                    style = CustomTypography.TextRegular,
                    color = colorResource(R.color.colorSystem_normal_text),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 🔴 SỬA: Dùng itemsList.size
        if (itemsList.size > 1) {
            Text(
                text = "và ${itemsList.size - 1} sản phẩm khác...",
                style = CustomTypography.TextRegular.copy(fontSize = 13.sp),
                color = colorResource(R.color.colorSystem_greyscale_500)
            )
        }

        // 🔴 THÊM: Hiển thị thông báo nếu không có items
        if (itemsList.isEmpty()) {
            Text(
                text = "Không có thông tin sản phẩm",
                style = CustomTypography.TextRegular.copy(fontSize = 13.sp),
                color = colorResource(R.color.colorSystem_greyscale_400)
            )
        }

        Divider(color = colorResource(R.color.colorSystem_background_level_0))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTimestampSimple(order.createdAt),
                style = CustomTypography.TextRegular,
                color = colorResource(R.color.colorSystem_greyscale_600)
            )
            // 🔴 SỬA: Safe access cho totalPayment
            Text(
                text = "Tổng: ${(order.totalPayment ?: order.totalAmount).toPriceFormat()}",
                style = CustomTypography.TextBold,
                color = colorResource(R.color.colorSystem_tint_red)
            )
        }
    }
}
@Composable
private fun OrderStatusBadge(status: String) {
    val (text, color) = when (status) {
        "PENDING_PAYMENT" -> "Chờ thanh toán" to colorResource(R.color.colorSystem_tint_yellow)
        "PENDING_PACKAGING" -> "Chờ đóng gói" to colorResource(R.color.colorSystem_tint_blue)
        "PICKUPED" -> "Đã lấy hàng" to colorResource(R.color.colorSystem_tint_blue)
        "PENDING_DELIVERY" -> "Đang giao" to colorResource(R.color.colorSystem_tint_blue)
        "DELIVERED" -> "Hoàn thành" to colorResource(R.color.colorSystem_tint_green)
        "CANCELLED" -> "Đã huỷ" to colorResource(R.color.colorSystem_tint_red)
        "RETURNED" -> "Hoàn trả" to colorResource(R.color.colorSystem_tint_red)
        else -> status to colorResource(R.color.colorSystem_greyscale_500)
    }

    Text(
        text = text,
        style = CustomTypography.TextSemiBold.copy(fontSize = 13.sp),
        color = color
    )
}

private fun formatTimestampSimple(timestamp: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()
        val date = inputFormat.parse(timestamp)
        date?.let { outputFormat.format(it) } ?: timestamp
    } catch (e: Exception) {
        timestamp
    }
}