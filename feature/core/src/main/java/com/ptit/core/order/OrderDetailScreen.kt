package com.ptit.core.order

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.common.utils.toPriceFormat
import com.ptit.core.order.components.SharedSnapshotItemRow
import com.ptit.core.order.components.SharedTotalAmountSection
// 🔴 MỚI: Thêm lại import
import com.ptit.core.purchase.PurchaseBottomSheet
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    // 🔴 MỚI: Thêm lại
    navigateToPaymentMethod: () -> Unit,
    backToCart: () -> Unit,
) {
    LocalBottomNavigationVisibility.current.value = false
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context = LocalContext.current
    val viewModel: OrderDetailViewModel = hiltViewModel()

    val orderState by viewModel.orderState.collectAsStateWithLifecycle()
    val cancelOrderState by viewModel.cancelOrderState.collectAsStateWithLifecycle()
    val processPaymentState by viewModel.processPaymentState.collectAsStateWithLifecycle()

    val isShowProgressBar = rememberState { false }
    // 🔴 MỚI: Thêm lại state
    val isShowPurchaseConfirmBottomSheet = rememberState { false }

    LaunchedEffect(Unit) {
        viewModel.loadOrderDetails(orderId)

        lifecycleOwner.safeCollectFlow(viewModel.orderState) {
            it
                .onLoading { isShowProgressBar.value = true }
                .onSuccess { isShowProgressBar.value = false }
                .onError {
                    isShowProgressBar.value = false
                    Toast.makeText(context, "Lỗi khi tải đơn hàng", Toast.LENGTH_SHORT).show()
                }
        }

        lifecycleOwner.safeCollectFlow(viewModel.cancelOrderState) {
            it
                .onLoading { isShowProgressBar.value = true }
                .onSuccess {
                    isShowProgressBar.value = false
                    Toast.makeText(context, "Huỷ đơn hàng thành công", Toast.LENGTH_SHORT).show()
                    onBack()
                }
                .onError {
                    isShowProgressBar.value = false
                    Toast.makeText(context, "Huỷ đơn hàng thất bại", Toast.LENGTH_SHORT).show()
                }
        }

        lifecycleOwner.safeCollectFlow(viewModel.processPaymentState) {
            it
                .onLoading { isShowProgressBar.value = true }
                .onSuccess { result ->
                    Toast.makeText(context, "Thanh toán thành công!", Toast.LENGTH_SHORT).show()
                    isShowPurchaseConfirmBottomSheet.value = false

                    // Reload order details after a short delay to ensure backend has updated
                    viewModel.reloadOrderAfterPayment(orderId)
                }
                .onError { error ->
                    isShowProgressBar.value = false

                    // Check if error is token expiration
                    val errorMessage = error.message ?: ""
                    if (errorMessage.contains("Token is invalid or expired", ignoreCase = true)) {
                        Toast.makeText(
                            context,
                            "Token thanh toán đã hết hạn. Vui lòng xóa và thêm lại phương thức thanh toán.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Thanh toán thất bại: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    viewModel.resetPaymentState()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Chi tiết đơn hàng",
                            style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
        if (orderState is Resource.Success) {
            val order = remember(orderState) { (orderState as Resource.Success).data }

            // 🔴 MỚI: Logic hiển thị nút theo yêu cầu mới
            val isPayable = remember(order.status) {
                order.status == "PENDING_PAYMENT"
            }
            val isCancellable = remember(order.status) {
                order.status == "PENDING_PAYMENT" || order.status == "PENDING_PACKAGING"
            }

            MaxSizeColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(colorResource(R.color.colorSystem_background_level_0))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ... (Các item giữ nguyên) ...
                    order.receiver?.let {
                        item {
                            OrderInfoSection(
                                name = it.name,
                                phone = it.phone,
                                address = it.address
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Chi tiết đơn hàng",
                            style = CustomTypography.TextBold,
                            color = colorResource(R.color.colorSystem_heading_button)
                        )
                    }

                    val itemsList = order.items ?: emptyList()

                    if (itemsList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Không có thông tin sản phẩm",
                                    style = CustomTypography.TextRegular,
                                    color = colorResource(R.color.colorSystem_greyscale_400)
                                )
                            }
                        }
                    } else {
                        items(itemsList) { item ->
                            SharedSnapshotItemRow(snapshot = item)
                        }
                    }

                    if (order.totalVoucherDiscount > 0) {
                        item {
                            DiscountInfoRow(
                                label = "Giảm giá Voucher:",
                                value = order.totalVoucherDiscount
                            )
                        }
                    }

                    item {
                        SharedTotalAmountSection(
                            subtotal = order.totalItemCost.toPriceFormat(),
                            shippingFee = order.totalShippingFee.toPriceFormat(),
                            totalPrice = order.totalPayment.toPriceFormat()
                        )
                    }

                    item { OrderStatusSection(status = order.status) }

                    item {
                        OrderTimestampsSection(
                            createdAt = order.createdAt,
                            updatedAt = order.updatedAt ?: ""
                        )
                    }

                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }

                // ⚙️ Bottom Actions (Đã sửa logic)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(R.color.colorSystem_greyscale_0_white))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 🔴 SỬA: Chỉ hiển thị nút Thanh toán khi `isPayable`
                    if (isPayable) {
                        FilledButton(
                            text = "Thanh toán",
                            onClick = { isShowPurchaseConfirmBottomSheet.value = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isShowProgressBar.value
                        )
                    }

                    // 🔴 SỬA: Chỉ hiển thị nút Huỷ khi `isCancellable`
                    if (isCancellable) {
                        FilledButton(
                            text = "Huỷ đơn hàng",
                            onClick = { viewModel.cancelOrder(orderId) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isShowProgressBar.value,
                            // 🔴 TÙY CHỌN: Đổi màu nút Hủy nếu muốn
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = colorResource(R.color.colorSystem_greyscale_300),
//                                contentColor = colorResource(R.color.colorSystem_greyscale_700)
//                            )
                        )
                    }
                }
            }
        }
    }

    // 🔴 MỚI: Thêm lại PurchaseBottomSheet
    PurchaseBottomSheet(
        isVisible = isShowPurchaseConfirmBottomSheet.value,
        onDismiss = { isShowPurchaseConfirmBottomSheet.value = false },
        onAddPaymentMethod = {
            navigateToPaymentMethod()
        },
        onConfirmedPurchase = { tokenId ->
            // Process payment with the actual API
            viewModel.processPayment(orderId, tokenId)
        }
    )

    if (isShowProgressBar.value)
        FullScreenProgressBar()
}

// ... (Các Composable bên dưới không đổi) ...
@Composable
fun OrderInfoSection(
    name: String,
    phone: String,
    address: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Thông tin vận chuyển",
            style = CustomTypography.TextBold,
            color = colorResource(R.color.colorSystem_heading_button)
        )
        InfoRow(label = "Họ tên:", value = name)
        InfoRow(label = "Số điện thoại:", value = phone)
        InfoRow(label = "Địa chỉ:", value = address)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = CustomTypography.TextRegular,
            color = colorResource(R.color.colorSystem_normal_text)
        )
        Text(
            text = value,
            style = CustomTypography.TextSemiBold,
            color = colorResource(R.color.colorSystem_heading_button)
        )
    }
}

@Composable
fun DiscountInfoRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = CustomTypography.TextRegular,
            color = colorResource(R.color.colorSystem_normal_text)
        )
        Text(
            text = "-${value.toPriceFormat()}",
            style = CustomTypography.TextSemiBold,
            color = colorResource(R.color.colorSystem_heading_button)
        )
    }
}

@Composable
fun OrderNoteDisplay(note: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(16.dp)
    ) {
        Text(
            text = "Ghi chú:",
            style = CustomTypography.TextSemiBold,
            color = colorResource(R.color.colorSystem_heading_button)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = note,
            style = CustomTypography.TextRegular,
            color = colorResource(R.color.colorSystem_normal_text)
        )
    }
}


@Composable
fun OrderStatusSection(status: String) {
    // Map backend status values to OrderStatus constants
    val normalizedStatus = when (status) {
        "PENDING_PAYMENT", "PENDING" -> com.ptit.common.const.OrderStatus.PENDING_PAYMENT
        "PENDING_PACKAGING", "PENDING_PACKAGE", "PROCESSING" -> com.ptit.common.const.OrderStatus.PENDING_PACKAGING
        "PICKUPED", "PENDING_DELIVERY", "SHIPPING" -> com.ptit.common.const.OrderStatus.SHIPPING
        "DELIVERED" -> com.ptit.common.const.OrderStatus.DELIVERED
        "CANCELLED" -> com.ptit.common.const.OrderStatus.CANCELLED
        "RETURNED" -> com.ptit.common.const.OrderStatus.RETURNED
        else -> status
    }

    val statusText = com.ptit.common.const.OrderStatus.getDisplayName(normalizedStatus)
    val statusColorHex = com.ptit.common.const.OrderStatus.getStatusColor(normalizedStatus)
    val statusColor = Color(android.graphics.Color.parseColor(statusColorHex))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Trạng thái đơn hàng:",
            style = CustomTypography.TextSemiBold,
            color = colorResource(R.color.colorSystem_normal_text)
        )

        Text(
            text = statusText,
            style = CustomTypography.TextSemiBold,
            color = statusColor
        )
    }
}

@Composable
fun OrderTimestampsSection(
    createdAt: String,
    updatedAt: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val formattedCreatedAt = formatTimestamp(createdAt)
        val formattedUpdatedAt = formatTimestamp(updatedAt)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Thời gian tạo đơn hàng:",
                style = CustomTypography.TextRegular,
                color = colorResource(R.color.colorSystem_normal_text)
            )
            Text(
                text = formattedCreatedAt,
                style = CustomTypography.TextMedium,
                color = colorResource(R.color.colorSystem_heading_button)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Thời gian cập nhật:",
                style = CustomTypography.TextRegular,
                color = colorResource(R.color.colorSystem_normal_text)
            )
            Text(
                text = formattedUpdatedAt,
                style = CustomTypography.TextMedium,
                color = colorResource(R.color.colorSystem_heading_button)
            )
        }
    }
}

private fun formatTimestamp(timestamp: String): String {
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