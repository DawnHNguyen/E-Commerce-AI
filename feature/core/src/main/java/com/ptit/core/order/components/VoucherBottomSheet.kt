package com.ptit.core.order.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
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
    onConfirm: () -> Unit,
    // Trạng thái Voucher sẽ được quản lý sau
    isVoucherListLoaded: Boolean = false,
    onVoucherCodeApply: (String) -> Unit = {}
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
                    onClick = { onVoucherCodeApply(voucherCode.text) },
                    modifier = Modifier.height(50.dp),
                    enabled = voucherCode.text.isNotBlank()
                )
            }

            Spacer(Modifier.height(32.dp))

            // 3. Nội dung chính / Thông báo lỗi
            if (!isVoucherListLoaded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Cancel, // Icon lỗi
                        contentDescription = null,
                        tint = colorResource(R.color.colorSystem_tint_red),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Không thể tải danh sách voucher. Vui lòng thử lại.",
                        style = CustomTypography.TextMedium,
                        color = colorResource(R.color.colorSystem_tint_red),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // TODO: Hiển thị danh sách voucher đã tải
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    // items(vouchers) { ... }
                    item { Text("Danh sách Voucher ở đây") } // Placeholder
                }
            }

            Spacer(Modifier.height(32.dp))

            // 4. Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Hủy")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.colorSystem_tint_red)
                )) {
                    Text("Xác nhận")
                }
            }
        }
    }

}
@Composable
fun VoucherSelectorRow(onClick: () -> Unit) {
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
            Text(
                "Voucher Sàn",
                style = CustomTypography.TextRegular.copy(fontSize = 15.sp),
                color = colorResource(R.color.colorSystem_normal_text)
            )
        }
        Text(
            "Chọn hoặc nhập mã",
            style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
            color = colorResource(R.color.colorSystem_heading_button)
        )
    }
}