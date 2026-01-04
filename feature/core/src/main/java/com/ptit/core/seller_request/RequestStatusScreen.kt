package com.ptit.core.seller_request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestStatusScreen(
    viewModel: SellerRequestViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToCreateRequest: () -> Unit,
    onNavigateToShop: () -> Unit
) {
    LocalBottomNavigationVisibility.current.value = false

    val myRequestState by viewModel.myRequestState.collectAsStateWithLifecycle()

    // Only navigate to shop when request is APPROVED. Do NOT auto-navigate to create when null;
    // show a friendly card instead so user can choose to create the shop.
    LaunchedEffect(myRequestState) {
        when (val state = myRequestState) {
            is Resource.Success -> {
                val request = state.data
                if (request != null && request.status.name == "APPROVED") {
                    // Request approved → Navigate to Shop
                    onNavigateToShop()
                }
                // For null, PENDING or REJECTED, UI will render appropriate content
            }
            else -> {
                // Loading or Error, do nothing
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Trạng thái yêu cầu",
                        style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
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
                    containerColor = colorResource(R.color.colorSystem_heading_button),
                    titleContentColor = colorResource(R.color.colorSystem_greyscale_0_white)
                )
            )
        }
    ) { paddingValues ->
        when (val state = myRequestState) {
            is Resource.Loading -> {
                FullScreenProgressBar()
            }
            is Resource.Success -> {
                val request = state.data
                if (request == null) {
                    // Show friendly No Shop UI with CTA to create
                    NoShopCard(
                        modifier = Modifier.padding(paddingValues),
                        onCreateShop = onNavigateToCreateRequest
                    )
                } else {
                    // Show the status badge at the top of the screen content, then the details
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorResource(R.color.colorSystem_background_level_0))
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Status badge on top
                        StatusBadge(status = request.status.name)

                        // Then the rest of the content
                        RequestStatusContent(
                            modifier = Modifier.fillMaxSize(),
                            shopName = request.shopName,
                            shopDescription = request.shopDescription,
                            businessLicense = request.businessLicense,
                            taxCode = request.taxCode,
                            status = request.status.name,
                            rejectionReason = request.rejectionReason,
                            createdAt = request.createdAt,
                            onCreateNewRequest = onNavigateToCreateRequest
                        )
                    }
                }
                // APPROVED case handled by LaunchedEffect navigation above
            }
            is Resource.Error -> {
                ErrorState(
                    modifier = Modifier.padding(paddingValues),
                    message = state.error.message ?: "Lỗi không xác định",
                    onRetry = { viewModel.fetchMySellerRequest() }
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun NoShopCard(
    modifier: Modifier = Modifier,
    onCreateShop: () -> Unit
) {
    MaxSizeColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.colorSystem_background_level_0))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.colorSystem_background_level_2))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = "No shop",
                    tint = colorResource(R.color.colorSystem_heading_button),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Bạn chưa có cửa hàng",
                    style = CustomTypography.TextBold.copy(fontSize = 18.sp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tạo cửa hàng để bắt đầu bán hàng trên nền tảng.",
                    style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                    color = colorResource(R.color.colorSystem_text_button),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                FilledButton(
                    text = "Tạo cửa hàng",
                    onClick = onCreateShop,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RequestStatusContent(
    modifier: Modifier = Modifier,
    shopName: String,
    shopDescription: String?,
    businessLicense: String?,
    taxCode: String?,
    status: String,
    rejectionReason: String?,
    createdAt: String,
    onCreateNewRequest: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shop Information
        InfoCard(
            title = "Thông tin cửa hàng",
            content = {
                InfoRow(label = "Tên cửa hàng", value = shopName)
                if (!shopDescription.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(label = "Mô tả", value = shopDescription)
                }
                if (!businessLicense.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(label = "Giấy phép kinh doanh", value = businessLicense)
                }
                if (!taxCode.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(label = "Mã số thuế", value = taxCode)
                }
            }
        )

        // Request Information
        InfoCard(
            title = "Thông tin yêu cầu",
            content = {
                InfoRow(label = "Ngày gửi", value = formatDate(createdAt))
                Spacer(modifier = Modifier.height(8.dp))

                // Status row: highlight when PENDING
                if (status == "PENDING") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF9C4))
                            .padding(8.dp)
                    ) {
                        InfoRow(label = "Trạng thái", value = getStatusDisplayName(status))
                    }
                } else {
                    InfoRow(label = "Trạng thái", value = getStatusDisplayName(status))
                }
            }
        )

        // Rejection Reason (if rejected)
        if (status == "REJECTED" && !rejectionReason.isNullOrBlank()) {
            InfoCard(
                title = "Lý do từ chối",
                backgroundColor = Color(0xFFFFF3E0),
                content = {
                    Text(
                        text = rejectionReason,
                        style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                        color = Color(0xFFE65100)
                    )
                }
            )

            // Allow resubmit if rejected
            FilledButton(
                text = "Gửi yêu cầu mới",
                onClick = onCreateNewRequest,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Status message (keep these messages but they are also shown by StatusBadge on top)
        when (status) {
            "PENDING" -> {
                InfoCard(
                    backgroundColor = Color(0xFFFFF9C4),
                    content = {
                        Text(
                            text = "⏳ Yêu cầu của bạn đang được xem xét. Vui lòng chờ admin duyệt.",
                            style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
                            color = Color(0xFFF57C00)
                        )
                    }
                )
            }
            "APPROVED" -> {
                InfoCard(
                    backgroundColor = Color(0xFFC8E6C9),
                    content = {
                        Text(
                            text = "✅ Yêu cầu của bạn đã được phê duyệt! Bạn có thể bắt đầu bán hàng.",
                            style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
                            color = Color(0xFF2E7D32)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (backgroundColor, textColor, text) = when (status) {
        "PENDING" -> Triple(Color(0xFFFFF9C4), Color(0xFFF57C00), "Đang chờ duyệt")
        "APPROVED" -> Triple(Color(0xFFC8E6C9), Color(0xFF2E7D32), "Đã phê duyệt")
        "REJECTED" -> Triple(Color(0xFFFFCDD2), Color(0xFFC62828), "Đã từ chối")
        else -> Triple(Color(0xFFE0E0E0), Color(0xFF424242), status)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = CustomTypography.TextBold.copy(fontSize = 18.sp),
            color = textColor
        )
    }
}

@Composable
private fun InfoCard(
    title: String? = null,
    backgroundColor: Color = colorResource(R.color.colorSystem_background_level_2),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = CustomTypography.TextBold.copy(fontSize = 12.sp, fontStyle = FontStyle.Italic),
            color = colorResource(R.color.colorSystem_text_button)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
            color = colorResource(R.color.colorSystem_heading_button)
        )
    }
}

@Composable
private fun ErrorState(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: () -> Unit
) {
    MaxSizeColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Lỗi: $message",
            style = CustomTypography.TextMedium.copy(fontSize = 16.sp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        FilledButton(
            text = "Thử lại",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun getStatusDisplayName(status: String): String {
    return when (status) {
        "PENDING" -> "Đang chờ duyệt"
        "APPROVED" -> "Đã phê duyệt"
        "REJECTED" -> "Đã từ chối"
        else -> status
    }
}

private fun formatDate(dateString: String): String {
    if (dateString.isNullOrEmpty()) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (_: Exception) {
        dateString
    }
}
