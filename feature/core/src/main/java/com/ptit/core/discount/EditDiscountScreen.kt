package com.ptit.core.discount

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDiscountScreen(
    discountId: String,
    shopId: String,
    viewModel: EditDiscountViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Load data on start
    LaunchedEffect(discountId) {
        viewModel.loadDiscountDetail(discountId)
    }

    // Handle events
    LaunchedEffect(uiState.isUpdateSuccess, uiState.error) {
        if (uiState.isUpdateSuccess) {
            Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
            viewModel.resetState()
        }
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
        }
    }

    // Dropdown States
    var showTypeMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chỉnh sửa Voucher",
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
                )
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.updateDiscount(discountId, shopId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button)
                ),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = colorResource(R.color.colorSystem_greyscale_0_white))
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lưu thay đổi", style = CustomTypography.TextBold)
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.name.isEmpty()) {
            // Loading initial data
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorResource(R.color.colorSystem_heading_button))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(R.color.colorSystem_background_level_0))
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // === Thông tin cơ bản ===
                Text(
                    "Thông tin cơ bản",
                    style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )

                // Tên
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Tên chương trình khuyến mãi") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputColors()
                )

                // Mã code (Có thể cho phép sửa hoặc readOnly tùy logic business)
                OutlinedTextField(
                    value = uiState.code,
                    onValueChange = viewModel::onCodeChange,
                    label = { Text("Mã Voucher") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputColors(),
                    // readOnly = true // Bỏ comment nếu không muốn cho sửa mã
                )

                // Mô tả
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = inputColors()
                )

                // === Thiết lập giảm giá ===
                Text(
                    "Thiết lập giảm giá",
                    style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )

                // Loại giảm giá (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = showTypeMenu,
                    onExpandedChange = { showTypeMenu = it }
                ) {
                    OutlinedTextField(
                        value = if (uiState.discountType == DiscountConstants.DISCOUNT_TYPE_PERCENTAGE) "Theo phần trăm (%)" else "Số tiền cố định (VNĐ)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loại giảm giá") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = inputColors()
                    )
                    ExposedDropdownMenu(
                        expanded = showTypeMenu,
                        onDismissRequest = { showTypeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Theo phần trăm (%)") },
                            onClick = {
                                viewModel.onDiscountTypeChange(DiscountConstants.DISCOUNT_TYPE_PERCENTAGE)
                                showTypeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Số tiền cố định (VNĐ)") },
                            onClick = {
                                viewModel.onDiscountTypeChange(DiscountConstants.DISCOUNT_TYPE_FIXED)
                                showTypeMenu = false
                            }
                        )
                    }
                }

                // Giá trị giảm
                OutlinedTextField(
                    value = uiState.value,
                    onValueChange = viewModel::onValueChange,
                    label = { Text(if (uiState.discountType == DiscountConstants.DISCOUNT_TYPE_PERCENTAGE) "Mức giảm (%)" else "Số tiền giảm (VNĐ)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = inputColors()
                )

                // Giảm tối đa (Nếu là %)
                if (uiState.discountType == DiscountConstants.DISCOUNT_TYPE_PERCENTAGE) {
                    OutlinedTextField(
                        value = uiState.maxDiscountValue,
                        onValueChange = viewModel::onMaxDiscountChange,
                        label = { Text("Giảm tối đa (VNĐ)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = inputColors()
                    )
                }

                // Đơn tối thiểu
                OutlinedTextField(
                    value = uiState.minOrderValue,
                    onValueChange = viewModel::onMinOrderChange,
                    label = { Text("Đơn hàng tối thiểu (VNĐ)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = inputColors()
                )

                // === Giới hạn sử dụng ===
                Text(
                    "Giới hạn sử dụng",
                    style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.maxUses,
                        onValueChange = viewModel::onMaxUsesChange,
                        label = { Text("Tổng lượt") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = inputColors()
                    )
                    OutlinedTextField(
                        value = uiState.maxUsesPerUser,
                        onValueChange = viewModel::onMaxUsesPerUserChange,
                        label = { Text("Lượt/Người") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = inputColors()
                    )
                }

                // === Thời gian & Trạng thái ===
                Text(
                    "Thời gian & Trạng thái",
                    style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )

                // Date Time Pickers
                DateTimePickerField(
                    label = "Bắt đầu",
                    value = uiState.startDate,
                    onValueChange = viewModel::onStartDateChange
                )

                DateTimePickerField(
                    label = "Kết thúc",
                    value = uiState.endDate,
                    onValueChange = viewModel::onEndDateChange
                )

                // Status Toggle Switch
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
                                if (uiState.discountStatus == DiscountConstants.DISCOUNT_STATUS_ACTIVE) "Đang hoạt động" else "Tạm ngưng",
                                style = CustomTypography.TextSmall,
                                color = if (uiState.discountStatus == DiscountConstants.DISCOUNT_STATUS_ACTIVE)
                                    colorResource(R.color.colorSystem_success)
                                else
                                    colorResource(R.color.colorSystem_error)
                            )
                        }
                        Switch(
                            checked = uiState.discountStatus == DiscountConstants.DISCOUNT_STATUS_ACTIVE,
                            onCheckedChange = { isActive ->
                                viewModel.onStatusChange(
                                    if (isActive) DiscountConstants.DISCOUNT_STATUS_ACTIVE
                                    else DiscountConstants.DISCOUNT_STATUS_INACTIVE
                                )
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

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
    focusedLabelColor = colorResource(R.color.colorSystem_heading_button),
    cursorColor = colorResource(R.color.colorSystem_heading_button)
)
