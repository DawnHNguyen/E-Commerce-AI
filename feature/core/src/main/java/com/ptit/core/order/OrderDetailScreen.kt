package com.ptit.core.order

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.core.order.components.SharedOrderItemRow
import com.ptit.core.order.components.SharedTotalAmountSection
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.entity.order.OrderDomainEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    viewModel: OrderDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPaymentClick: (String) -> Unit
) {
    val orderState by viewModel.orderState.collectAsState()

    // Fix: Ensure this is called only once and properly triggers
    LaunchedEffect(Unit) {
        viewModel.loadOrderDetails(orderId)
    }

    val order = orderState.order
    val subtotal = order?.purchases?.sumOf { it.price * it.buyCount } ?: 0
    val shippingFee = order?.shippingFee ?: 30000
    val totalPrice = subtotal + shippingFee

    // Show loading or error states
    val snackbarHostState = remember { SnackbarHostState() }

    // Debug to see what we're receiving
    LaunchedEffect(orderState) {
        println("Debug OrderDetailScreen: isLoading=${orderState.isLoading}, error=${orderState.error}, order=${orderState.order != null}")
    }

    if (orderState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (orderState.error != null) {
        LaunchedEffect(orderState.error) {
            snackbarHostState.showSnackbar(orderState.error!!)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        MaxSizeColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(R.color.colorSystem_background_level_0))
        ) {
            if (order == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không tìm thấy dữ liệu đơn hàng")
                }
            } else {
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
                        SharedTotalAmountSection(subtotal = subtotal, shippingFee = shippingFee, totalPrice = totalPrice)
                    }

                    // Order status
                    item {
                        OrderStatusSection(status = order.status)
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
                            onClick = { onPaymentClick(orderId) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !orderState.isLoading
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderInfoSection(
    name: String,
    phone: String,
    address: String
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
    val statusColor = when(status) {
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
