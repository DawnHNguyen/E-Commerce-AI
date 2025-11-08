// File: com.ptit.core.order.components/CustomerInformationSection.kt (Đã sửa đổi)

package com.ptit.core.order.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.theme.CustomTypography

/**
 * 🧍‍♀️ Thông tin khách hàng (Dạng mở rộng/thu gọn)
 */
@Composable
fun CustomerInformationSection(
    name: TextFieldValue,
    email: String,
    phone: TextFieldValue
) {
    // Hàm tiện ích để che 4 số cuối của SĐT
    fun maskPhoneNumber(phoneNumber: String): String {
        val length = phoneNumber.length
        return when {
            length >= 4 -> {
                "*".repeat(length - 4) + phoneNumber.takeLast(4)
            }
            else -> phoneNumber
        }
    }

    // 🔴 LOGIC EXPAND: State mở/thu gọn
    var isExpanded by remember { mutableStateOf(true) }

    // 💡 Component nội dung: Label và Value cùng hàng
    @Composable
    fun InfoRow(label: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label, // 🔴 LABEL CÓ MÀU XANH NHẠT
                style = CustomTypography.TextRegular.copy(fontSize = 15.sp),
                color = colorResource(R.color.colorSystem_heading_button), // Dùng màu xanh của Heading
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value, // 🔴 VALUE CÙNG HÀNG
                style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
                color = colorResource(R.color.colorSystem_greyscale_1000_black),
                // Căn phải nếu cần thiết, nhưng SpaceBetween đã xử lý
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
    ) {
        // 1. HEADER (Mở/Thu gọn)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded } // 🔴 Thêm Clickable
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Thông tin khách hàng",
                    tint = colorResource(R.color.colorSystem_heading_button),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Thông tin khách hàng",
                    style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )
            }
            // 🔴 Icon mở/thu gọn
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown
                else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (isExpanded) "Thu gọn" else "Mở rộng",
                tint = colorResource(R.color.colorSystem_heading_button)
            )
        }

        // 2. NỘI DUNG MỞ RỘNG
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp) // Khoảng cách giữa các dòng
            ) {
                // 1. Khách hàng (Họ tên)
                InfoRow(
                    label = "Khách hàng",
                    value = name.text
                )

                // 2. Số điện thoại
                InfoRow(
                    label = "Số điện thoại",
                    value = maskPhoneNumber(phone.text)
                )

                // 3. Email
                InfoRow(
                    label = "Email",
                    value = email
                )
            }
        }
    }
}