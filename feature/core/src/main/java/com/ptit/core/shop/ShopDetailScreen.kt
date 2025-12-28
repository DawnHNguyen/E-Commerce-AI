package com.ptit.core.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ptit.common.R
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.utils.Resource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailScreen(
    shopId: String? = null,
    sellerRequestViewModel: com.ptit.core.seller_request.SellerRequestViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEditShop: () -> Unit,
    onNavigateToProductList: () -> Unit,
    onNavigateToOrders: () -> Unit = {},
    onNavigateToPromotions: (String) -> Unit = {},
    onNavigateToOverview: () -> Unit = {}
) {
    val myRequestState by sellerRequestViewModel.myRequestState.collectAsStateWithLifecycle()

    // Refresh data on screen load
    LaunchedEffect(shopId) {
        sellerRequestViewModel.fetchMySellerRequest()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            // FIX: Dùng Surface bao ngoài để màu xanh tràn lên status bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(R.color.colorSystem_heading_button),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    TopAppBar(
                        title = {
                            Text(
                                "Cửa hàng của tôi",
                                style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                                color = colorResource(R.color.colorSystem_greyscale_0_white)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Quay lại",
                                    tint = colorResource(R.color.colorSystem_greyscale_0_white)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = onNavigateToEditShop) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Chỉnh sửa",
                                    tint = colorResource(R.color.colorSystem_greyscale_0_white)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent, // Để màu của Surface hiện lên
                            titleContentColor = colorResource(R.color.colorSystem_greyscale_0_white)
                        )
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colorResource(R.color.colorSystem_background_level_0)
    ) { paddingValues ->
        if (myRequestState is Resource.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colorResource(R.color.colorSystem_heading_button)
                )
            }
        } else {
            val sellerRequest = (myRequestState as? Resource.Success)?.data
            val shopName = sellerRequest?.shopName ?: "Cửa hàng của tôi"
            val shopDescription = sellerRequest?.shopDescription

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with shop info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    colorResource(R.color.colorSystem_heading_button),
                                    colorResource(R.color.colorSystem_background_level_0)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Shop Avatar
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(4.dp, colorResource(R.color.colorSystem_greyscale_0_white), CircleShape)
                                .background(colorResource(R.color.colorSystem_text_field)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Store,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = colorResource(R.color.colorSystem_heading_button)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = shopName,
                            style = CustomTypography.TextBold.copy(fontSize = 22.sp),
                            color = colorResource(R.color.colorSystem_greyscale_0_white)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colorResource(R.color.colorSystem_success).copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = colorResource(R.color.colorSystem_success)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Đã phê duyệt",
                                    style = CustomTypography.TextMedium.copy(fontSize = 12.sp),
                                    color = colorResource(R.color.colorSystem_success)
                                )
                            }
                        }
                    }
                }

                // Shop Information Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Description Card
                    if (!shopDescription.isNullOrEmpty()) {
                        InfoCard(
                            icon = Icons.Default.Description,
                            title = "Mô tả",
                            content = shopDescription
                        )
                    }

                    // Business Info
                    if (sellerRequest != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = colorResource(R.color.colorSystem_background_level_2)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Business,
                                        contentDescription = null,
                                        tint = colorResource(R.color.colorSystem_heading_button)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "Thông tin doanh nghiệp",
                                        style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                                        color = colorResource(R.color.colorSystem_heading_button)
                                    )
                                }

                                HorizontalDivider(color = colorResource(R.color.colorSystem_stroke))

                                if (!sellerRequest.businessLicense.isNullOrEmpty()) {
                                    ContactInfoRow(
                                        icon = Icons.Default.Description,
                                        label = "Giấy phép kinh doanh",
                                        value = sellerRequest.businessLicense ?: ""
                                    )
                                }

                                if (!sellerRequest.taxCode.isNullOrEmpty()) {
                                    ContactInfoRow(
                                        icon = Icons.Default.Numbers,
                                        label = "Mã số thuế",
                                        value = sellerRequest.taxCode ?: ""
                                    )
                                }
                            }
                        }
                    }

                    // --- MENU GRID SECTION ---
                    Text(
                        "Quản lý cửa hàng",
                        style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                        color = colorResource(R.color.colorSystem_heading_button),
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )

                    // Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ColorfulMenuCard(
                            icon = Icons.Default.Inventory,
                            title = "Sản phẩm",
                            // Màu xanh dương nhạt cho nền icon
                            iconBgColor = Color(0xFFE3F2FD),
                            iconColor = Color(0xFF1976D2),
                            onClick = onNavigateToProductList,
                            modifier = Modifier.weight(1f)
                        )

                        ColorfulMenuCard(
                            icon = Icons.Default.ShoppingBag,
                            title = "Đơn hàng",
                            // Màu cam nhạt
                            iconBgColor = Color(0xFFFFF3E0),
                            iconColor = Color(0xFFF57C00),
                            onClick = onNavigateToOrders,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ColorfulMenuCard(
                            icon = Icons.Default.LocalOffer,
                            title = "Khuyến mãi",
                            // Màu hồng nhạt
                            iconBgColor = Color(0xFFFCE4EC),
                            iconColor = Color(0xFFC2185B),
                            onClick = {
                                val actualShopId = sellerRequest?.id ?: shopId ?: ""
                                if (actualShopId.isBlank()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Không có shop Id để xem khuyến mãi")
                                    }
                                } else {
                                    try {
                                        onNavigateToPromotions(actualShopId)
                                    } catch (e: Exception) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Lỗi: ${e.message}")
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        ColorfulMenuCard(
                            icon = Icons.Default.BarChart,
                            title = "Thống kê",
                            // Màu tím nhạt
                            iconBgColor = Color(0xFFF3E5F5),
                            iconColor = Color(0xFF7B1FA2),
                            onClick = onNavigateToOverview,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// Helper composable mới cho thẻ Menu đẹp hơn
@Composable
private fun ColorfulMenuCard(
    icon: ImageVector,
    title: String,
    iconBgColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f), // Giữ tỉ lệ vuông
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.colorSystem_greyscale_0_white)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Vòng tròn nền màu cho icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color = iconBgColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
                color = colorResource(R.color.colorSystem_heading_button),
                maxLines = 1
            )
        }
    }
}

// Giữ nguyên InfoCard cũ
@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.colorSystem_background_level_2)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = colorResource(R.color.colorSystem_heading_button)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    title,
                    style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                content,
                style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                color = colorResource(R.color.colorSystem_greyscale_600)
            )
        }
    }
}

// Giữ nguyên ContactInfoRow
@Composable
private fun ContactInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = colorResource(R.color.colorSystem_text_button),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = CustomTypography.TextMedium.copy(fontSize = 12.sp),
                color = colorResource(R.color.colorSystem_greyscale_500)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = CustomTypography.TextSemiBold.copy(fontSize = 14.sp),
                color = colorResource(R.color.colorSystem_greyscale_900)
            )
        }
    }
}