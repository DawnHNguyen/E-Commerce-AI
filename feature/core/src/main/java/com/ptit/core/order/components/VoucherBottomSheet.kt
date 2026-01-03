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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    availableVouchers: List<DiscountDomainEntity>,
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
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Chọn Mã Giảm Giá",
                    style = CustomTypography.TextBold.copy(fontSize = 18.sp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng")
                }
            }
            Spacer(Modifier.height(16.dp))

            // Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        focusedContainerColor = colorResource(R.color.colorSystem_background_level_0),
                        unfocusedContainerColor = colorResource(R.color.colorSystem_background_level_0),
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

            // Content
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorResource(R.color.colorSystem_heading_button))
                    }
                }
                availableVouchers.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().height(200.dp).padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.LocalOffer, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Chưa có voucher phù hợp", style = CustomTypography.TextMedium, color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(availableVouchers.size) { index ->
                            val voucher = availableVouchers[index]
                            VoucherItem(
                                voucher = voucher,
                                isSelected = selectedVoucher?.id == voucher.id,
                                onClick = { onSelectVoucher(voucher); onDismiss() }
                            )
                        }
                    }
                }
            }

            // Selected & Remove
            if (selectedVoucher != null) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.LightGray)
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Đang chọn:", style = CustomTypography.TextSmall, color = Color.Gray)
                        Text(selectedVoucher.code, style = CustomTypography.TextBold)
                    }
                    TextButton(onClick = onRemoveVoucher) {
                        Text("Bỏ chọn", color = colorResource(R.color.colorSystem_error))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            FilledButton(text = "Đóng", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
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

@Composable
fun VoucherItem(
    voucher: DiscountDomainEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colorResource(R.color.colorSystem_heading_button).copy(alpha = 0.1f) else colorResource(R.color.colorSystem_background_level_1)
        ),
        border = if (isSelected) BorderStroke(1.dp, colorResource(R.color.colorSystem_heading_button)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(voucher.code, style = CustomTypography.TextBold, color = colorResource(R.color.colorSystem_heading_button))
                Text(voucher.name, style = CustomTypography.TextRegular.copy(fontSize = 14.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (voucher.discountType == "PERCENTAGE") "Giảm ${voucher.value.toInt()}%" else "Giảm ${voucher.value.toInt()}đ",
                    style = CustomTypography.TextSemiBold, color = colorResource(R.color.colorSystem_error)
                )
                Text("HSD: ${voucher.endDate.take(10)}", style = CustomTypography.TextSmall, color = Color.Gray)
            }
            if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = colorResource(R.color.colorSystem_heading_button))
        }
    }
}

// 🔴 SHOP ORDER SECTION CÓ VOUCHER
@Composable
fun ShopOrderSection(
    shopName: String,
    cartItems: List<com.ptit.domain.entity.cart.CartItemDomainEntity>,
    selectedShopVoucher: DiscountDomainEntity?,
    onSelectVoucherClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("🏪", style = CustomTypography.TextSemiBold.copy(fontSize = 18.sp))
            Spacer(Modifier.width(8.dp))
            Text(shopName, style = CustomTypography.TextBold.copy(fontSize = 16.sp), color = colorResource(R.color.colorSystem_heading_button))
        }

        Spacer(Modifier.height(12.dp))

        // Items
        if (cartItems.isEmpty()) {
            Text("Không có sản phẩm", style = CustomTypography.TextRegular, color = Color.Gray)
        } else {
            cartItems.forEach { item ->
                SharedCartItemRow(cartItem = item)
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

        // Voucher Row inside Shop
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectVoucherClick() }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Đã sửa 'orange_primary' thành 'colorSystem_text_button'
                Icon(Icons.Default.LocalOffer, null, tint = colorResource(R.color.colorSystem_text_button), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Shop Voucher", style = CustomTypography.TextMedium.copy(fontSize = 14.sp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selectedShopVoucher?.code ?: "Chọn hoặc nhập mã",
                    style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                    // Đã sửa màu text
                    color = if(selectedShopVoucher != null) colorResource(R.color.colorSystem_text_button) else Color.Gray,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 120.dp)
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
            }
        }
    }
}