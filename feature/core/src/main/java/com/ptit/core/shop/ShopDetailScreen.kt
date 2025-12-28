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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ptit.common.R
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.utils.Resource
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailScreen(
    shopId: String? = null,
    sellerRequestViewModel: com.ptit.core.seller_request.SellerRequestViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEditShop: () -> Unit,
    onNavigateToProductList: () -> Unit,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToPromotions: (String) -> Unit = {},
    onNavigateToOverview: () -> Unit = {}
) {
    val myRequestState by sellerRequestViewModel.myRequestState.collectAsStateWithLifecycle()

    // Refresh data on screen load
    LaunchedEffect(shopId) {
        sellerRequestViewModel.fetchMySellerRequest()
    }

    // Snackbar host + coroutine scope for safe navigation error handling
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
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
                    containerColor = colorResource(R.color.colorSystem_heading_button),
                    titleContentColor = colorResource(R.color.colorSystem_greyscale_0_white)
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) } // added host
    ) { paddingValues ->
        if (myRequestState is Resource.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(R.color.colorSystem_background_level_0))
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colorResource(R.color.colorSystem_heading_button)
                )
            }
        } else {
            // Lấy thông tin từ seller request (nếu có)
            val sellerRequest = (myRequestState as? Resource.Success)?.data
            val shopName = sellerRequest?.shopName ?: "Cửa hàng của tôi"
            val shopDescription = sellerRequest?.shopDescription

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(R.color.colorSystem_background_level_0))
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
                        // Shop Avatar (default icon)
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

                        // Shop Name
                        Text(
                            text = shopName,
                            style = CustomTypography.TextBold.copy(fontSize = 22.sp),
                            color = colorResource(R.color.colorSystem_greyscale_0_white)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Status Badge
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

                // Shop Information
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

                    // Seller Request Info Card (nếu có)
                    if (sellerRequest != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = colorResource(R.color.colorSystem_background_level_2)
                            ),
                            shape = RoundedCornerShape(16.dp)
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

                    // Menu Grid - 5 items
                    Text(
                        "Quản lý cửa hàng",
                        style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                        color = colorResource(R.color.colorSystem_heading_button),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Row 1: 3 items
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MenuCard(
                            icon = Icons.Default.Inventory,
                            title = "Sản phẩm",
                            onClick = onNavigateToProductList,
                            modifier = Modifier.weight(1f)
                        )
                        MenuCard(
                            icon = Icons.Default.Category,
                            title = "Danh mục",
                            onClick = onNavigateToCategories,
                            modifier = Modifier.weight(1f)
                        )
                        MenuCard(
                            icon = Icons.Default.ShoppingBag,
                            title = "Đơn hàng",
                            onClick = onNavigateToOrders,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: 2 items
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MenuCard(
                            icon = Icons.Default.LocalOffer,
                            title = "Khuyến mãi",
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
                                        // Prevent crash — show a friendly message instead
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Không thể chuyển đến Khuyến mãi: ${e.message ?: "Lỗi không xác định"}")
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MenuCard(
                            icon = Icons.Default.BarChart,
                            title = "Thống kê",
                            onClick = onNavigateToOverview,
                            modifier = Modifier.weight(1f)
                        )
                        // Empty space to balance
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

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
        shape = RoundedCornerShape(16.dp)
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

@Composable
private fun MenuCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.colorSystem_background_level_2)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = colorResource(R.color.colorSystem_heading_button),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                title,
                style = CustomTypography.TextSemiBold.copy(fontSize = 14.sp),
                color = colorResource(R.color.colorSystem_greyscale_900)
            )
        }
    }
}
