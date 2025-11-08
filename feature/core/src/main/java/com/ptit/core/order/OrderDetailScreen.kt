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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.core.order.components.SharedOrderItemRow
import com.ptit.core.order.components.SharedTotalAmountSection
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
    navigateToPaymentMethod: () -> Unit,
    backToCart: () -> Unit,
) {
    LocalBottomNavigationVisibility.current.value = false

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val viewModel: OrderDetailViewModel = hiltViewModel()

    val orderState by viewModel.orderState.collectAsStateWithLifecycle()

    val isShowProgressBar = rememberState { false }
    val isShowPurchaseConfirmBottomSheet = rememberState { false }

    LaunchedEffect(Unit) {
        viewModel.loadOrderDetails(orderId)

        lifecycleOwner.safeCollectFlow(viewModel.orderState) {
            it
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onSuccess {
                    isShowProgressBar.value = false
                }
                .onError {
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Có lỗi xảy ra trong quá trình tải đơn hàng",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        lifecycleOwner.safeCollectFlow(viewModel.payOrderState) {
            it
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onSuccess {
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Thanh toán thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    backToCart()
                }
                .onError {
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Có lỗi xảy ra trong quá trình thanh toán",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Đơn hàng",
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
            MaxSizeColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(colorResource(R.color.colorSystem_background_level_0))
            ) {
                // Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Shipping Information
                    item {
                        OrderInfoSection(
                            name = order.fullName,
                            phone = order.phone,
                            address = order.address
                        )
                    }

                    // Order Details
                    item {
                        Text(
                            text = "Chi tiết đơn hàng",
                            style = CustomTypography.TextBold,
                            color = colorResource(R.color.colorSystem_heading_button)
                        )
                    }

                    // Order Items
                    items(order.purchases) { purchase ->
                        SharedOrderItemRow(purchase = purchase)
                    }

                    // Note Section
                    if (order.note.isNotBlank()) {
                        item {
                            OrderNoteDisplay(note = order.note)
                        }
                    }

                    // Total amount
                    item {
                        SharedTotalAmountSection(subtotal = order.subTotal, shippingFee = order.shippingFee, totalPrice = order.totalPrice)
                    }

                    // Order status
                    item {
                        OrderStatusSection(status = order.status)
                    }
                    item {
                        OrderTimestampsSection(
                            createdAt = order.createdAt,
                            updatedAt = order.updatedAt
                        )
                    }

                    // Spacer at the bottom for better layout
                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }

                // Bottom Bar with Payment Button
                if (order.status == "Pending" || order.status == "Processing") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorResource(R.color.colorSystem_greyscale_0_white))
                            .padding(16.dp)
                    ) {
                        FilledButton(
                            text = "Thanh toán",
                            onClick = {
                                isShowPurchaseConfirmBottomSheet.value = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }

    PurchaseBottomSheet(
        isVisible = isShowPurchaseConfirmBottomSheet.value,
        onDismiss = { isShowPurchaseConfirmBottomSheet.value = false },
        onAddPaymentMethod = navigateToPaymentMethod,
        onConfirmedPurchase = { token ->
            isShowPurchaseConfirmBottomSheet.value = false
            viewModel.payOrder(
                orderId = orderId,
                token = token
            )
        }
    )

    if (isShowProgressBar.value)
        FullScreenProgressBar()
}

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

        // Read-only information display
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
    val statusColor = when (status) {
        "Paid" -> colorResource(R.color.colorSystem_heading_button)
        "Pending" -> colorResource(R.color.colorSystem_tint_yellow)
        else -> colorResource(R.color.colorSystem_normal_text)
    }

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
            text = status,
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
        // Format the timestamps from ISO format to a more readable format
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

/**
 * Formats ISO 8601 timestamp to a more readable format
 * Input: "2025-05-04T14:26:12.234Z"
 * Output: "04/05/2025 14:26"
 */
private fun formatTimestamp(timestamp: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault() // Convert to local timezone

        val date = inputFormat.parse(timestamp)
        date?.let { outputFormat.format(it) } ?: timestamp
    } catch (e: Exception) {
        // Fallback in case of parsing error
        timestamp
    }
}