package com.ptit.core.discount

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ptit.common.R
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.constants.DiscountConstants
import com.ptit.domain.entity.discount.DiscountDomainEntity
import com.ptit.domain.utils.Resource
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountListScreen(
    viewModel: DiscountViewModel = hiltViewModel(),
    accountViewModel: com.ptit.core.account.AccountViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDiscountForm: (discountId: String?, shopId: String) -> Unit,
    onNavigateToDiscountDetail: (String) -> Unit = {},
    shopId: String
) {
    val discountsState by viewModel.discountsState.collectAsStateWithLifecycle()
    val deleteDiscountState by viewModel.deleteDiscountState.collectAsStateWithLifecycle()
    val accountUiModel by accountViewModel.uiModel.collectAsStateWithLifecycle()

    var selectedDiscount by remember { mutableStateOf<DiscountDomainEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Get userId from AccountViewModel
    val userId = accountUiModel.user.id

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.getShopDiscounts(
                page = 1,
                limit = 100,
                createdById = userId
            )
        }
    }

    // Handle delete success
    LaunchedEffect(deleteDiscountState) {
        if (deleteDiscountState is Resource.Success<*>) {
            // Refresh list
            if (userId.isNotEmpty()) {
                viewModel.getShopDiscounts(
                    page = 1,
                    limit = 100,
                    createdById = userId
                )
            }
            viewModel.resetDeleteDiscountState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quản lý Khuyến mãi",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button),
                    titleContentColor = colorResource(R.color.colorSystem_greyscale_0_white)
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToDiscountForm(null, shopId) },
                containerColor = colorResource(R.color.colorSystem_heading_button),
                contentColor = colorResource(R.color.colorSystem_greyscale_0_white)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm voucher")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.colorSystem_background_level_0))
                .padding(paddingValues)
        ) {
            when (discountsState) {
                is Resource.Loading<*> -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(R.color.colorSystem_heading_button)
                        )
                    }
                }

                is Resource.Success<*> -> {
                    val discounts = (discountsState as Resource.Success<com.ptit.domain.entity.discount.DiscountListDomainEntity>).data?.data ?: emptyList()

                    if (discounts.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = colorResource(R.color.colorSystem_greyscale_400)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Chưa có khuyến mãi nào",
                                    style = CustomTypography.TextBold.copy(fontSize = 18.sp),
                                    color = colorResource(R.color.colorSystem_greyscale_600)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Nhấn nút + để tạo khuyến mãi mới",
                                    style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                                    color = colorResource(R.color.colorSystem_greyscale_500)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(discounts) { discount ->
                                DiscountCard(
                                    discount = discount,
                                    onEdit = { onNavigateToDiscountForm(discount.id, shopId) },
                                    onDelete = {
                                        selectedDiscount = discount
                                        showDeleteDialog = true
                                    },
                                    onClick = { onNavigateToDiscountDetail(discount.id) }
                                )
                            }
                        }
                    }
                }

                is Resource.Error<*> -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = colorResource(R.color.colorSystem_error)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Có lỗi xảy ra",
                                style = CustomTypography.TextBold.copy(fontSize = 18.sp),
                                color = colorResource(R.color.colorSystem_error)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                (discountsState as? Resource.Error<*>)?.error?.message ?: "Không thể tải danh sách khuyến mãi",
                                style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                                color = colorResource(R.color.colorSystem_greyscale_600)
                            )
                        }
                    }
                }

                else -> {}
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog && selectedDiscount != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        "Xác nhận xóa",
                        style = CustomTypography.TextBold.copy(fontSize = 18.sp)
                    )
                },
                text = {
                    Text(
                        "Bạn có chắc chắn muốn xóa khuyến mãi \"${selectedDiscount?.name}\"?",
                        style = CustomTypography.TextRegular.copy(fontSize = 14.sp)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedDiscount?.let { discount ->
                                viewModel.deleteDiscount(discount.id)
                            }
                            showDeleteDialog = false
                            selectedDiscount = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = colorResource(R.color.colorSystem_error)
                        )
                    ) {
                        Text("Xóa")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Composable
fun DiscountCard(
    discount: DiscountDomainEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.colorSystem_background_level_2)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with name and menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = colorResource(R.color.colorSystem_heading_button),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = discount.name,
                            style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                            color = colorResource(R.color.colorSystem_heading_button),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = discount.code,
                            style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
                            color = colorResource(R.color.colorSystem_greyscale_600)
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = colorResource(R.color.colorSystem_greyscale_600)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sửa") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Xóa") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = colorResource(R.color.colorSystem_error)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Discount value
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colorResource(R.color.colorSystem_heading_button).copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (discount.discountType == DiscountConstants.DISCOUNT_TYPE_PERCENTAGE) {
                        Text(
                            text = "Giảm ${discount.value.toInt()}%",
                            style = CustomTypography.TextBold.copy(fontSize = 18.sp),
                            color = colorResource(R.color.colorSystem_heading_button)
                        )
                    } else {
                        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                        Text(
                            text = "Giảm ${formatter.format(discount.value)}",
                            style = CustomTypography.TextBold.copy(fontSize = 18.sp),
                            color = colorResource(R.color.colorSystem_heading_button)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status and dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DiscountStatusChip(status = discount.discountStatus)

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatDate(discount.startDate),
                        style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                        color = colorResource(R.color.colorSystem_greyscale_600)
                    )
                    Text(
                        text = "- ${formatDate(discount.endDate)}",
                        style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                        color = colorResource(R.color.colorSystem_greyscale_600)
                    )
                }
            }

            // Usage info
            if (discount.maxUses > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (discount.currentUses.toFloat() / discount.maxUses.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = colorResource(R.color.colorSystem_heading_button),
                    trackColor = colorResource(R.color.colorSystem_greyscale_300)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Đã dùng: ${discount.currentUses}/${discount.maxUses}",
                    style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                    color = colorResource(R.color.colorSystem_greyscale_600)
                )
            }
        }
    }
}

@Composable
fun DiscountStatusChip(status: String) {
    val (backgroundColor, textColor, text) = when (status) {
        DiscountConstants.DISCOUNT_STATUS_ACTIVE -> Triple(
            colorResource(R.color.colorSystem_success).copy(alpha = 0.2f),
            colorResource(R.color.colorSystem_success),
            "Đang hoạt động"
        )
        DiscountConstants.DISCOUNT_STATUS_INACTIVE -> Triple(
            colorResource(R.color.colorSystem_greyscale_400).copy(alpha = 0.2f),
            colorResource(R.color.colorSystem_greyscale_600),
            "Tạm ngưng"
        )
        DiscountConstants.DISCOUNT_STATUS_EXPIRED -> Triple(
            colorResource(R.color.colorSystem_error).copy(alpha = 0.2f),
            colorResource(R.color.colorSystem_error),
            "Đã hết hạn"
        )
        DiscountConstants.DISCOUNT_STATUS_SCHEDULED -> Triple(
            colorResource(R.color.colorSystem_heading_button).copy(alpha = 0.2f),
            colorResource(R.color.colorSystem_heading_button),
            "Sắp diễn ra"
        )
        else -> Triple(
            colorResource(R.color.colorSystem_greyscale_400).copy(alpha = 0.2f),
            colorResource(R.color.colorSystem_greyscale_600),
            "Không xác định"
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            style = CustomTypography.TextMedium.copy(fontSize = 12.sp),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateString
    }
}

