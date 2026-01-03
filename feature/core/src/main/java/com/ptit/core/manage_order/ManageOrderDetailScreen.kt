package com.ptit.core.manage_order

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.toPriceFormat
import com.ptit.domain.utils.Resource
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOrderDetailScreen(
    orderId: String,
    viewModel: ManageOrderViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    LocalBottomNavigationVisibility.current.value = false
    val context = LocalContext.current

    val orderState by viewModel.orderDetailState.collectAsStateWithLifecycle()
    val updateState by viewModel.updateStatusState.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) {
        viewModel.getOrderDetail(orderId)
    }

    LaunchedEffect(updateState) {
        when (updateState) {
            is Resource.Success -> {
                Toast.makeText(context, "Đã xác nhận vận chuyển!", Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateStatusState()
            }
            is Resource.Error -> {
                Toast.makeText(context, "Lỗi: ${(updateState as Resource.Error).error.message}", Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateStatusState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chi tiết đơn hàng",
                        style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                        color = colorResource(R.color.colorSystem_greyscale_0_white)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colorResource(R.color.colorSystem_greyscale_0_white)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button)
                )
            )
        },
        bottomBar = {
            if (orderState is Resource.Success) {
                val order = (orderState as Resource.Success).data
                if (order.status == "PENDING_PACKAGING") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                            .shadow(elevation = 8.dp)
                    ) {
                        FilledButton(
                            text = "Xác nhận vận chuyển",
                            onClick = { viewModel.confirmShipping(order.id) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(colorResource(R.color.colorSystem_background_level_0))
        ) {
            when (val state = orderState) {
                is Resource.Loading -> FullScreenProgressBar()
                is Resource.Success -> {
                    val order = state.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Mã đơn & Trạng thái
                        StatusCard(
                            orderCode = order.orderCode ?: order.id.takeLast(8).uppercase(),
                            status = order.status
                        )

                        // 2. Người nhận
                        if (order.receiver != null) {
                            ReceiverInfoCard(
                                name = order.receiver!!.name,
                                phone = order.receiver!!.phone,
                                address = order.receiver!!.address
                            )
                        }

                        // 3. Sản phẩm
                        ProductListCard(items = order.items ?: emptyList())

                        // 4. Thanh toán
                        PaymentInfoCard(
                            itemTotal = order.totalItemCost,
                            shippingFee = order.totalShippingFee,
                            discount = order.totalVoucherDiscount,
                            totalPayment = order.totalPayment,
                            paymentMethod = order.paymentMethod
                        )

                        // 5. Thời gian (MỚI)
                        TimeInfoCard(
                            createdAt = order.createdAt,
                            updatedAt = order.updatedAt
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Không tải được chi tiết đơn hàng: ${state.error.message}",
                            color = colorResource(R.color.colorSystem_error),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

// --- UI COMPONENTS ---

@Composable
fun StatusCard(orderCode: String, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    tint = colorResource(R.color.colorSystem_heading_button),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mã đơn: #$orderCode",
                    style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                    color = colorResource(R.color.colorSystem_greyscale_900)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = colorResource(R.color.colorSystem_greyscale_100)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(getStatusColor(status).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = getStatusColor(status),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        "Trạng thái đơn hàng",
                        style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                        color = colorResource(R.color.colorSystem_greyscale_500)
                    )
                    Text(
                        text = getStatusText(status),
                        style = CustomTypography.TextSemiBold.copy(fontSize = 16.sp),
                        color = getStatusColor(status)
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiverInfoCard(name: String, phone: String, address: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Địa chỉ nhận hàng",
                style = CustomTypography.TextBold.copy(fontSize = 15.sp),
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(Icons.Default.Person, name)
            InfoRow(Icons.Default.Phone, phone)
            InfoRow(Icons.Default.Place, address)
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = colorResource(R.color.colorSystem_greyscale_500)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
            color = colorResource(R.color.colorSystem_greyscale_900),
            lineHeight = 20.sp
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProductListCard(items: List<com.ptit.domain.entity.order.ProductSKUSnapshotDomainEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sản phẩm (${items.sumOf { it.quantity }})",
                style = CustomTypography.TextBold.copy(fontSize = 15.sp),
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Spacer(modifier = Modifier.height(8.dp))

            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    GlideImage(
                        model = item.image,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, colorResource(R.color.colorSystem_greyscale_100), RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.productName,
                            style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
                            color = colorResource(R.color.colorSystem_greyscale_900),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Phân loại: ${item.skuValue}",
                            style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                            color = colorResource(R.color.colorSystem_greyscale_500)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                item.skuPrice.toPriceFormat(),
                                style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
                                color = colorResource(R.color.colorSystem_greyscale_900)
                            )
                            Text(
                                "x${item.quantity}",
                                style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                                color = colorResource(R.color.colorSystem_greyscale_600)
                            )
                        }
                    }
                }
                if (index < items.lastIndex) {
                    HorizontalDivider(color = colorResource(R.color.colorSystem_greyscale_100))
                }
            }
        }
    }
}

@Composable
fun PaymentInfoCard(itemTotal: Int, shippingFee: Int, discount: Int, totalPayment: Int, paymentMethod: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Chi tiết thanh toán",
                style = CustomTypography.TextBold.copy(fontSize = 15.sp),
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Spacer(modifier = Modifier.height(12.dp))

            PaymentRow("Tổng tiền hàng", itemTotal.toPriceFormat())
            PaymentRow("Phí vận chuyển", shippingFee.toPriceFormat())
            PaymentRow("Giảm giá voucher", "-${discount.toPriceFormat()}", isDiscount = true)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = colorResource(R.color.colorSystem_greyscale_100)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Thành tiền",
                    style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                    color = colorResource(R.color.colorSystem_greyscale_900)
                )
                Text(
                    totalPayment.toPriceFormat(),
                    style = CustomTypography.TextBold.copy(fontSize = 18.sp),
                    color = colorResource(R.color.colorSystem_tint_red)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = colorResource(R.color.colorSystem_background_level_0),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = colorResource(R.color.colorSystem_text_button)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thanh toán $paymentMethod",
                        style = CustomTypography.TextMedium.copy(fontSize = 13.sp),
                        color = colorResource(R.color.colorSystem_text_button)
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentRow(label: String, value: String, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
            color = colorResource(R.color.colorSystem_greyscale_600)
        )
        Text(
            value,
            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
            color = if (isDiscount) colorResource(R.color.colorSystem_tint_green) else colorResource(R.color.colorSystem_greyscale_900)
        )
    }
}

// 👇 COMPONENTS MỚI
@Composable
fun TimeInfoCard(createdAt: String, updatedAt: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Thời gian",
                style = CustomTypography.TextBold.copy(fontSize = 15.sp),
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Spacer(modifier = Modifier.height(12.dp))

            TimeRow(label = "Tạo lúc", value = formatDateTime(createdAt))

            if (!updatedAt.isNullOrEmpty() && updatedAt != createdAt) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = colorResource(R.color.colorSystem_greyscale_100)
                )
                TimeRow(label = "Cập nhật lúc", value = formatDateTime(updatedAt))
            }
        }
    }
}

@Composable
fun TimeRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
            color = colorResource(R.color.colorSystem_greyscale_600)
        )
        Text(
            text = value,
            style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
            color = colorResource(R.color.colorSystem_greyscale_900)
        )
    }
}

// Utils
fun formatDateTime(isoString: String?): String {
    if (isoString.isNullOrEmpty()) return "--"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(isoString)
        val outputFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: isoString
    } catch (e: Exception) {
        isoString
    }
}