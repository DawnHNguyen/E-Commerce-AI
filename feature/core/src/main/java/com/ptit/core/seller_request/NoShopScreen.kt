package com.ptit.core.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.theme.CustomTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoShopScreen(
    onNavigateToCreateShop: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cửa hàng",
                        // 👇 Tăng cỡ chữ lên 20.sp và chuyển màu chữ sang trắng
                        style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            // 👇 Chuyển màu icon sang trắng
                            tint = Color.White
                        )
                    }
                },
                // 👇 Thêm cấu hình màu sắc cho TopBar
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button), // Màu nền xanh
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colorResource(R.color.colorSystem_background_level_0))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon minh họa
            Image(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                colorFilter = ColorFilter.tint(colorResource(R.color.colorSystem_greyscale_400))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tiêu đề body
            Text(
                text = "Bạn chưa có cửa hàng",
                style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                color = colorResource(R.color.colorSystem_heading_button)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mô tả
            Text(
                text = "Đăng ký trở thành người bán ngay hôm nay để tiếp cận hàng triệu khách hàng và gia tăng doanh số!",
                style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                color = colorResource(R.color.colorSystem_greyscale_600),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Nút tạo cửa hàng
            Button(
                onClick = onNavigateToCreateShop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Tạo cửa hàng ngay",
                    style = CustomTypography.TextSemiBold.copy(fontSize = 16.sp)
                )
            }
        }
    }
}