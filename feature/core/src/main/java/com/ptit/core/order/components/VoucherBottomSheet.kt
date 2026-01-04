package com.ptit.core.order.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.discount.DiscountDomainEntity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    availableVouchers: List<DiscountDomainEntity>, // Danh sách voucher từ API
    selectedVoucher: DiscountDomainEntity?,
    isLoading: Boolean,
    voucherError: String?,
    onApplyCode: (String) -> Unit,
    onSelectVoucher: (DiscountDomainEntity) -> Unit,
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
                .navigationBarsPadding() // Tránh bị che bởi thanh điều hướng
        ) {
            // --- 1. HEADER ---
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
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color.Gray)
                }
            }
            Spacer(Modifier.height(16.dp))

            // --- 2. INPUT NHẬP MÃ ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = voucherCode,
                    onValueChange = { voucherCode = it },
                    placeholder = { Text("Nhập mã voucher", fontSize = 14.sp, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                        unfocusedBorderColor = colorResource(R.color.colorSystem_greyscale_300),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
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
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(Modifier.height(16.dp))

            // --- 3. DANH SÁCH VOUCHER ---
            Text(
                text = "Mã giảm giá khả dụng",
                style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
                color = colorResource(R.color.colorSystem_normal_text)
            )
            Spacer(Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorResource(R.color.colorSystem_heading_button))
                    }
                }
                availableVouchers.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.LocalOffer, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Chưa có voucher nào", style = CustomTypography.TextRegular, color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(availableVouchers.size) { index ->
                            val voucher = availableVouchers[index]
                            val isSelected = selectedVoucher?.id == voucher.id

                            VoucherItemRow(
                                voucher = voucher,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelected) onRemoveVoucher() else onSelectVoucher(voucher)
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// --- ITEM VOUCHER (ĐÃ SỬA GIAO DIỆN) ---
@Composable
fun VoucherItemRow(
    voucher: DiscountDomainEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (isSelected) 1.dp else 0.5.dp,
            color = if (isSelected) colorResource(R.color.colorSystem_heading_button) else Color.LightGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Radio Button (Trái)
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = colorResource(R.color.colorSystem_heading_button),
                    unselectedColor = Color.Gray
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Thông tin Voucher (Giữa)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Tên voucher
                Text(
                    text = voucher.name,
                    style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Mã voucher (nhỏ)
                Text(
                    text = voucher.code,
                    style = CustomTypography.TextMedium.copy(fontSize = 12.sp),
                    color = colorResource(R.color.colorSystem_heading_button),
                    modifier = Modifier
                        .background(colorResource(R.color.colorSystem_heading_button).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Hạn sử dụng
                Text(
                    text = "HSD: ${voucher.endDate.take(10)}",
                    style = CustomTypography.TextSmall.copy(fontSize = 11.sp),
                    color = Color.Gray
                )
            }

            // 3. Giá trị giảm (Phải)
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (voucher.discountType == "PERCENTAGE") {
                        "Giảm ${voucher.value.toInt()}%"
                    } else {
                        "-${formatMoney(voucher.value.toInt())}"
                    },
                    style = CustomTypography.TextBold.copy(fontSize = 15.sp),
                    color = colorResource(R.color.colorSystem_error) // Màu đỏ
                )

                if (voucher.minOrderValue > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Đơn > ${formatMoney(voucher.minOrderValue.toInt())}",
                        style = CustomTypography.TextSmall.copy(fontSize = 10.sp),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
// 🛒 Platform Voucher Row (Dùng ở dưới cùng màn hình)
@Composable
fun PlatformVoucherRow(
    selectedVoucher: DiscountDomainEntity?,
    onClick: () -> Unit
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
                Icons.Default.ConfirmationNumber,
                contentDescription = "Shoppie Voucher",
                tint = colorResource(R.color.colorSystem_heading_button),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    "Shoppie Voucher",
                    style = CustomTypography.TextRegular.copy(fontSize = 15.sp)
                )
                if (selectedVoucher != null) {
                    Text(
                        selectedVoucher.code,
                        style = CustomTypography.TextSmall.copy(fontSize = 12.sp),
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                }
            }
        }
        Text(
            if (selectedVoucher != null) "-${if(selectedVoucher.discountType == "PERCENTAGE") "${selectedVoucher.value.toInt()}%" else "${selectedVoucher.value.toInt()}"}" else "Chọn mã",
            style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
            color = colorResource(R.color.colorSystem_heading_button)
        )
    }
}

// Helper format tiền tệ
private fun formatMoney(amount: Int): String {
    val symbols = DecimalFormatSymbols(Locale.getDefault())
    symbols.groupingSeparator = '.'
    val formatter = DecimalFormat("#,##0", symbols)
    return "${formatter.format(amount)}đ"
}