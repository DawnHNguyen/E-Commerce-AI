package com.ptit.core.order.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.theme.CustomTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    availableVouchers: List<com.ptit.domain.entity.discount.DiscountDomainEntity>,
    selectedVoucher: com.ptit.domain.entity.discount.DiscountDomainEntity?,
    isLoading: Boolean,
    voucherError: String?,
    onApplyCode: (String) -> Unit,
    onSelectVoucher: (com.ptit.domain.entity.discount.DiscountDomainEntity) -> Unit,
    onRemoveVoucher: () -> Unit
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var voucherCode by remember { mutableStateOf(TextFieldValue("")) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = colorResource(R.color.colorSystem_background_level_0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Voucher từ ShopPie",
                    style = CustomTypography.TextBold.copy(fontSize = 18.sp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng")
                }
            }
            Spacer(Modifier.height(16.dp))

            // 2. Input Mã Voucher
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SỬ DỤNG OutlinedTextField chuẩn vì FilledTextField tùy chỉnh của bạn phức tạp
                OutlinedTextField(
                    value = voucherCode,
                    onValueChange = { voucherCode = it },
                    placeholder = { Text("Nhập mã voucher") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                        unfocusedBorderColor = colorResource(R.color.colorSystem_greyscale_300),

                        // 🔴 CÁC MÀU NỀN CẦN THIẾT
                        focusedContainerColor = colorResource(R.color.colorSystem_background_level_0),
                        unfocusedContainerColor = colorResource(R.color.colorSystem_background_level_0),
                        disabledContainerColor = colorResource(R.color.colorSystem_background_level_0),

                        // Màu chữ
                        focusedTextColor = colorResource(R.color.colorSystem_normal_text),
                        unfocusedTextColor = colorResource(R.color.colorSystem_normal_text),
                        disabledTextColor = colorResource(R.color.colorSystem_normal_text),
                    )
                )
                Spacer(Modifier.width(8.dp))
                FilledButton(
                    text = "Áp dụng",
                    onClick = {
                        onApplyCode(voucherCode.text)
                        voucherCode = TextFieldValue("")
                    },
                    modifier = Modifier.height(50.dp),
                    enabled = voucherCode.text.isNotBlank() && !isLoading
                )
            }

            // Show error message if any
            if (voucherError != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = voucherError,
                    style = CustomTypography.TextSmall,
                    color = colorResource(R.color.colorSystem_error),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // 3. Voucher list content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(R.color.colorSystem_heading_button)
                        )
                    }
                }
                availableVouchers.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.LocalOffer,
                            contentDescription = null,
                            tint = colorResource(R.color.colorSystem_greyscale_400),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Không có voucher khả dụng",
                            style = CustomTypography.TextMedium,
                            color = colorResource(R.color.colorSystem_greyscale_500),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(availableVouchers.size) { index ->
                            val voucher = availableVouchers[index]
                            VoucherItem(
                                voucher = voucher,
                                isSelected = selectedVoucher?.id == voucher.id,
                                onClick = {
                                    onSelectVoucher(voucher)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4. Selected voucher display
            if (selectedVoucher != null) {
                HorizontalDivider(color = colorResource(R.color.colorSystem_greyscale_200))
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voucher đã chọn:",
                            style = CustomTypography.TextSmall,
                            color = colorResource(R.color.colorSystem_greyscale_600)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = selectedVoucher.code,
                            style = CustomTypography.TextBold,
                            color = colorResource(R.color.colorSystem_heading_button)
                        )
                    }
                    TextButton(onClick = {
                        onRemoveVoucher()
                    }) {
                        Text(
                            "Xóa",
                            color = colorResource(R.color.colorSystem_error)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 5. Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledButton(
                    text = "Đóng",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

}
@Composable
fun VoucherSelectorRow(
    onClick: () -> Unit,
    selectedVoucher: com.ptit.domain.entity.discount.DiscountDomainEntity? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.LocalOffer,
                contentDescription = "Voucher Sàn",
                tint = colorResource(R.color.colorSystem_heading_button),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    "Voucher Sàn",
                    style = CustomTypography.TextRegular.copy(fontSize = 15.sp),
                    color = colorResource(R.color.colorSystem_normal_text)
                )
                if (selectedVoucher != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        selectedVoucher.code,
                        style = CustomTypography.TextSmall.copy(fontSize = 12.sp),
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                }
            }
        }
        Text(
            if (selectedVoucher != null) "Đổi mã" else "Chọn hoặc nhập mã",
            style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
            color = colorResource(R.color.colorSystem_heading_button)
        )
    }
}

@Composable
fun VoucherItem(
    voucher: com.ptit.domain.entity.discount.DiscountDomainEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                colorResource(R.color.colorSystem_heading_button).copy(alpha = 0.1f)
            else
                colorResource(R.color.colorSystem_background_level_1)
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, colorResource(R.color.colorSystem_heading_button))
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Voucher info
            Column(modifier = Modifier.weight(1f)) {
                // Voucher code
                Text(
                    text = voucher.code,
                    style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )

                Spacer(Modifier.height(4.dp))

                // Voucher name
                Text(
                    text = voucher.name,
                    style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                    color = colorResource(R.color.colorSystem_normal_text),
                    maxLines = 1
                )

                Spacer(Modifier.height(8.dp))

                // Discount value
                Text(
                    text = if (voucher.discountType == "PERCENTAGE") {
                        "Giảm ${voucher.value.toInt()}%"
                    } else {
                        "Giảm ${(voucher.value.toInt() / 1000)}K"
                    },
                    style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
                    color = colorResource(R.color.colorSystem_error)
                )

                // Min order value
                if (voucher.minOrderValue > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Đơn tối thiểu: ${(voucher.minOrderValue.toInt() / 1000)}K",
                        style = CustomTypography.TextSmall.copy(fontSize = 12.sp),
                        color = colorResource(R.color.colorSystem_greyscale_600)
                    )
                }

                // Validity period
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "HSD: ${formatDate(voucher.endDate)}",
                    style = CustomTypography.TextSmall.copy(fontSize = 12.sp),
                    color = colorResource(R.color.colorSystem_greyscale_600)
                )
            }

            // Right side - Select indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = colorResource(R.color.colorSystem_heading_button),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Helper function to format date
private fun formatDate(dateString: String): String {
    return try {
        val date = java.time.LocalDateTime.parse(dateString, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
        date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (_: Exception) {
        dateString.substring(0, 10)
    }
}

