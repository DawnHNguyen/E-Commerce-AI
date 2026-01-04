package com.ptit.core.discount

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ptit.common.R
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.core.discount.component.DateTimePickerField
import com.ptit.domain.constants.DiscountConstants
import com.ptit.domain.entity.discount.CreateDiscountRequestDomainEntity
import com.ptit.domain.entity.discount.UpdateDiscountRequestDomainEntity
import com.ptit.domain.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountFormScreen(
    viewModel: DiscountViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    discountId: String? = null,
    shopId: String
) {
    val createDiscountState by viewModel.createDiscountState.collectAsStateWithLifecycle()
    val updateDiscountState by viewModel.updateDiscountState.collectAsStateWithLifecycle()
    val discountDetailState by viewModel.discountDetailState.collectAsStateWithLifecycle()

    // Form states
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var minOrderValue by remember { mutableStateOf("") }
    var maxDiscountValue by remember { mutableStateOf("") }
    var maxUses by remember { mutableStateOf("") }
    var maxUsesPerUser by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf(DiscountConstants.DISCOUNT_TYPE_PERCENTAGE) }
    var discountStatus by remember { mutableStateOf(DiscountConstants.DISCOUNT_STATUS_ACTIVE) }
    var discountApplyType by remember { mutableStateOf(DiscountConstants.DISCOUNT_APPLY_TYPE_ALL) }
    var voucherType by remember { mutableStateOf(DiscountConstants.VOUCHER_TYPE_SHOP) }
    var displayType by remember { mutableStateOf(DiscountConstants.DISPLAY_TYPE_PUBLIC) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    var showDiscountTypeMenu by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }
    var showApplyTypeMenu by remember { mutableStateOf(false) }
    var showVoucherTypeMenu by remember { mutableStateOf(false) }
    var showDisplayTypeMenu by remember { mutableStateOf(false) }

    val isEditMode = discountId != null

    // Load discount data if editing
    LaunchedEffect(discountId) {
        if (discountId != null) {
            viewModel.getDiscountDetail(discountId)
        }
    }

    // Populate form with discount data
    LaunchedEffect(discountDetailState) {
        if (discountDetailState is Resource.Success) {
            val discount = (discountDetailState as Resource.Success).data
            discount?.let {
                name = it.name
                description = it.description ?: ""
                code = it.code
                value = it.value.toString()
                minOrderValue = it.minOrderValue.toString()
                maxDiscountValue = it.maxDiscountValue?.toString() ?: ""
                maxUses = it.maxUses.toString()
                maxUsesPerUser = it.maxUsesPerUser.toString()
                discountType = it.discountType
                discountStatus = it.discountStatus
                discountApplyType = it.discountApplyType
                voucherType = it.voucherType
                displayType = it.displayType
                startDate = it.startDate
                endDate = it.endDate
            }
        }
    }

    // Handle create/update success
    LaunchedEffect(createDiscountState, updateDiscountState) {
        if (createDiscountState is Resource.Success || updateDiscountState is Resource.Success) {
            onNavigateBack()
            if (createDiscountState is Resource.Success) {
                viewModel.resetCreateDiscountState()
            } else {
                viewModel.resetUpdateDiscountState()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Chỉnh sửa Khuyến mãi" else "Tạo Khuyến mãi",
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.colorSystem_background_level_0))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên khuyến mãi *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                        focusedLabelColor = colorResource(R.color.colorSystem_heading_button)
                    )
                )

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                        focusedLabelColor = colorResource(R.color.colorSystem_heading_button)
                    )
                )

                // Code field
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        if (it.length <= 5 && it.all { char -> char.isLetterOrDigit() }) {
                            code = it.uppercase()
                        }
                    },
                    label = { Text("Mã voucher * (1-5 ký tự)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                        focusedLabelColor = colorResource(R.color.colorSystem_heading_button)
                    ),
                    supportingText = {
                        Text("Chỉ chữ cái (A-Z) và số (0-9), tối đa 5 ký tự")
                    }
                )

                // Discount Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = showDiscountTypeMenu,
                    onExpandedChange = { showDiscountTypeMenu = it }
                ) {
                    OutlinedTextField(
                        value = getDiscountTypeLabel(discountType),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loại giảm giá *") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                            focusedLabelColor = colorResource(R.color.colorSystem_heading_button)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showDiscountTypeMenu,
                        onDismissRequest = { showDiscountTypeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Phần trăm (%)") },
                            onClick = {
                                discountType = DiscountConstants.DISCOUNT_TYPE_PERCENTAGE
                                showDiscountTypeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Số tiền cố định") },
                            onClick = {
                                discountType = DiscountConstants.DISCOUNT_TYPE_FIXED
                                showDiscountTypeMenu = false
                            }
                        )
                    }
                }

                // Value field
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it.filter { char -> char.isDigit() || char == '.' } },
                    label = {
                        Text(if (discountType == DiscountConstants.DISCOUNT_TYPE_PERCENTAGE)
                            "Giá trị giảm (%) *" else "Giá trị giảm (VNĐ) *")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                        focusedLabelColor = colorResource(R.color.colorSystem_heading_button)
                    )
                )

                // Min Order Value
                OutlinedTextField(
                    value = minOrderValue,
                    onValueChange = { minOrderValue = it.filter { char -> char.isDigit() } },
                    label = { Text("Giá trị đơn hàng tối thiểu (VNĐ) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                        focusedLabelColor = colorResource(R.color.colorSystem_heading_button)
                    )
                )

                // Max Discount Value (for percentage type)
                if (discountType == DiscountConstants.DISCOUNT_TYPE_PERCENTAGE) {
                    OutlinedTextField(
                        value = maxDiscountValue,
                        onValueChange = { maxDiscountValue = it.filter { char -> char.isDigit() } },
                        label = { Text("Giảm tối đa (VNĐ)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                            focusedLabelColor = colorResource(R.color.colorSystem_heading_button)
                        )
                    )
                }

                // Max Uses
                OutlinedTextField(
                    value = maxUses,
                    onValueChange = { maxUses = it.filter { char -> char.isDigit() } },
                    label = { Text("Số lần sử dụng tối đa *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                        focusedLabelColor = colorResource(R.color.colorSystem_heading_button)
                    )
                )

                // Max Uses Per User
                OutlinedTextField(
                    value = maxUsesPerUser,
                    onValueChange = { maxUsesPerUser = it.filter { char -> char.isDigit() } },
                    label = { Text("Số lần dùng/người *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                        focusedLabelColor = colorResource(R.color.colorSystem_heading_button)
                    )
                )

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = showStatusMenu,
                    onExpandedChange = { showStatusMenu = it }
                ) {
                    OutlinedTextField(
                        value = getStatusLabel(discountStatus),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Trạng thái *") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                            focusedLabelColor = colorResource(R.color.colorSystem_heading_button)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Đang hoạt động") },
                            onClick = {
                                discountStatus = DiscountConstants.DISCOUNT_STATUS_ACTIVE
                                showStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Tạm ngưng") },
                            onClick = {
                                discountStatus = DiscountConstants.DISCOUNT_STATUS_INACTIVE
                                showStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sắp diễn ra") },
                            onClick = {
                                discountStatus = DiscountConstants.DISCOUNT_STATUS_SCHEDULED
                                showStatusMenu = false
                            }
                        )
                    }
                }

                // Apply Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = showApplyTypeMenu,
                    onExpandedChange = { showApplyTypeMenu = it }
                ) {
                    OutlinedTextField(
                        value = getApplyTypeLabel(discountApplyType),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Áp dụng cho *") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                            focusedLabelColor = colorResource(R.color.colorSystem_heading_button)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showApplyTypeMenu,
                        onDismissRequest = { showApplyTypeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tất cả sản phẩm") },
                            onClick = {
                                discountApplyType = DiscountConstants.DISCOUNT_APPLY_TYPE_ALL
                                showApplyTypeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sản phẩm cụ thể") },
                            onClick = {
                                discountApplyType = DiscountConstants.DISCOUNT_APPLY_TYPE_SPECIFIC
                                showApplyTypeMenu = false
                            }
                        )
                    }
                }

                // Voucher Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = showVoucherTypeMenu,
                    onExpandedChange = { showVoucherTypeMenu = it }
                ) {
                    OutlinedTextField(
                        value = getVoucherTypeLabel(voucherType),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loại voucher *") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                            focusedLabelColor = colorResource(R.color.colorSystem_heading_button)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showVoucherTypeMenu,
                        onDismissRequest = { showVoucherTypeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Voucher Shop") },
                            onClick = {
                                voucherType = DiscountConstants.VOUCHER_TYPE_SHOP
                                showVoucherTypeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Voucher Sản phẩm") },
                            onClick = {
                                voucherType = DiscountConstants.VOUCHER_TYPE_PRODUCT
                                showVoucherTypeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Voucher Sàn") },
                            onClick = {
                                voucherType = DiscountConstants.VOUCHER_TYPE_PLATFORM
                                showVoucherTypeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Voucher Danh mục") },
                            onClick = {
                                voucherType = DiscountConstants.VOUCHER_TYPE_CATEGORY
                                showVoucherTypeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Voucher Thương hiệu") },
                            onClick = {
                                voucherType = DiscountConstants.VOUCHER_TYPE_BRAND
                                showVoucherTypeMenu = false
                            }
                        )
                    }
                }

                // Display Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = showDisplayTypeMenu,
                    onExpandedChange = { showDisplayTypeMenu = it }
                ) {
                    OutlinedTextField(
                        value = getDisplayTypeLabel(displayType),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hiển thị *") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                            focusedLabelColor = colorResource(R.color.colorSystem_heading_button)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showDisplayTypeMenu,
                        onDismissRequest = { showDisplayTypeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Công khai") },
                            onClick = {
                                displayType = DiscountConstants.DISPLAY_TYPE_PUBLIC
                                showDisplayTypeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Riêng tư") },
                            onClick = {
                                displayType = DiscountConstants.DISPLAY_TYPE_PRIVATE
                                showDisplayTypeMenu = false
                            }
                        )
                    }
                }

                // Date Time Pickers
                Text(
                    "Thời gian hiệu lực",
                    style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                    color = colorResource(R.color.colorSystem_heading_button),
                    modifier = Modifier.padding(top = 16.dp)
                )

                DateTimePickerField(
                    label = "Ngày bắt đầu",
                    value = startDate,
                    onValueChange = { startDate = it }
                )

                DateTimePickerField(
                    label = "Ngày kết thúc",
                    value = endDate,
                    onValueChange = { endDate = it }
                )

                // Status Toggle Switch
                Text(
                    "Trạng thái",
                    style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                    color = colorResource(R.color.colorSystem_heading_button),
                    modifier = Modifier.padding(top = 16.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.colorSystem_greyscale_0_white)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Trạng thái hoạt động",
                                style = CustomTypography.TextBold.copy(fontSize = 16.sp)
                            )
                            Text(
                                if (discountStatus == DiscountConstants.DISCOUNT_STATUS_ACTIVE) "Đang hoạt động" else "Tạm ngưng",
                                style = CustomTypography.TextSmall,
                                color = if (discountStatus == DiscountConstants.DISCOUNT_STATUS_ACTIVE)
                                    colorResource(R.color.colorSystem_tint_green)
                                else
                                    colorResource(R.color.colorSystem_tint_red)
                            )
                        }
                        Switch(
                            checked = discountStatus == DiscountConstants.DISCOUNT_STATUS_ACTIVE,
                            onCheckedChange = { isActive ->
                                discountStatus = if (isActive) DiscountConstants.DISCOUNT_STATUS_ACTIVE
                                else DiscountConstants.DISCOUNT_STATUS_INACTIVE
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorResource(R.color.colorSystem_greyscale_0_white),
                                checkedTrackColor = colorResource(R.color.colorSystem_heading_button),
                                uncheckedThumbColor = colorResource(R.color.colorSystem_greyscale_0_white),
                                uncheckedTrackColor = colorResource(R.color.colorSystem_greyscale_400)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit button
                Button(
                    onClick = {
                        if (isEditMode && discountId != null) {
                            val request = UpdateDiscountRequestDomainEntity(
                                name = name,
                                description = description.ifEmpty { null },
                                value = value.toDoubleOrNull() ?: 0.0,
                            code = code,
                            startDate = startDate,
                            endDate = endDate,
                            maxUsesPerUser = maxUsesPerUser.toIntOrNull() ?: 1,
                            minOrderValue = minOrderValue.toDoubleOrNull() ?: 0.0,
                            maxUses = maxUses.toIntOrNull() ?: 0,
                            maxDiscountValue = maxDiscountValue.toDoubleOrNull() ?: 10000000.0,
                            displayType = displayType,
                            voucherType = voucherType,
                            isPlatform = false,
                            shopId = shopId,
                            discountApplyType = discountApplyType,
                            discountStatus = discountStatus,
                            discountType = discountType
                        )
                        viewModel.updateDiscount(discountId, request)
                        } else {
                            // Tính toán maxDiscountValue: nếu null hoặc rỗng thì dùng default 1,000,000 cho PERCENTAGE
                            val maxDiscountValueParsed = maxDiscountValue.toDoubleOrNull()
                                ?: if (discountType == DiscountConstants.DISCOUNT_TYPE_PERCENTAGE) 1000000.0 else null

                            val request = CreateDiscountRequestDomainEntity(
                                name = name,
                                description = description.ifEmpty { null },
                                value = value.toDoubleOrNull() ?: 0.0,
                                code = code,
                                startDate = startDate,
                                endDate = endDate,
                                maxUsesPerUser = maxUsesPerUser.toIntOrNull() ?: 1,
                                minOrderValue = minOrderValue.toDoubleOrNull() ?: 0.0,
                                maxUses = maxUses.toIntOrNull() ?: 0,
                                maxDiscountValue = maxDiscountValueParsed,
                                displayType = displayType,
                                voucherType = voucherType,
                                isPlatform = false,
                                shopId = shopId,
                                discountApplyType = discountApplyType,
                                discountStatus = discountStatus,
                                discountType = discountType
                            )
                            viewModel.createDiscount(request)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.colorSystem_heading_button)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = createDiscountState !is Resource.Loading && updateDiscountState !is Resource.Loading
                ) {
                    if (createDiscountState is Resource.Loading || updateDiscountState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colorResource(R.color.colorSystem_greyscale_0_white)
                        )
                    } else {
                        Text(
                            if (isEditMode) "Cập nhật" else "Tạo khuyến mãi",
                            style = CustomTypography.TextBold.copy(fontSize = 16.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CreateDiscountRequestDomainEntity(
    name: String,
    description: String?,
    value: Double,
    code: String,
    startDate: String,
    endDate: String,
    maxUsesPerUser: Int,
    minOrderValue: Double,
    maxUses: Int,
    maxDiscountValue: Double?,
    displayType: String,
    voucherType: String,
    isPlatform: Boolean,
    shopId: String,
    discountApplyType: String,
    discountStatus: String,
    discountType: String
) {
    TODO("Not yet implemented")
}

private fun getDiscountTypeLabel(type: String): String = when (type) {
    DiscountConstants.DISCOUNT_TYPE_PERCENTAGE -> "Phần trăm (%)"
    DiscountConstants.DISCOUNT_TYPE_FIXED -> "Số tiền cố định"
    else -> "Không xác định"
}

private fun getStatusLabel(status: String): String = when (status) {
    DiscountConstants.DISCOUNT_STATUS_ACTIVE -> "Đang hoạt động"
    DiscountConstants.DISCOUNT_STATUS_INACTIVE -> "Tạm ngưng"
    DiscountConstants.DISCOUNT_STATUS_EXPIRED -> "Đã hết hạn"
    DiscountConstants.DISCOUNT_STATUS_SCHEDULED -> "Sắp diễn ra"
    else -> "Không xác định"
}

private fun getApplyTypeLabel(type: String): String = when (type) {
    DiscountConstants.DISCOUNT_APPLY_TYPE_ALL -> "Tất cả sản phẩm"
    DiscountConstants.DISCOUNT_APPLY_TYPE_SPECIFIC -> "Sản phẩm cụ thể"
    else -> "Không xác định"
}

private fun getVoucherTypeLabel(type: String): String = when (type) {
    DiscountConstants.VOUCHER_TYPE_SHOP -> "Voucher Shop"
    DiscountConstants.VOUCHER_TYPE_PRODUCT -> "Voucher Sản phẩm"
    DiscountConstants.VOUCHER_TYPE_PLATFORM -> "Voucher Sàn"
    DiscountConstants.VOUCHER_TYPE_CATEGORY -> "Voucher Danh mục"
    DiscountConstants.VOUCHER_TYPE_BRAND -> "Voucher Thương hiệu"
    else -> "Không xác định"
}

private fun getDisplayTypeLabel(type: String): String = when (type) {
    DiscountConstants.DISPLAY_TYPE_PUBLIC -> "Công khai"
    DiscountConstants.DISPLAY_TYPE_PRIVATE -> "Riêng tư"
    else -> "Không xác định"
}

