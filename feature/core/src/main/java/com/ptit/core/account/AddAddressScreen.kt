package com.ptit.core.account

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FilledTextField
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess

@Composable
fun AddAddressScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val shippingState by viewModel.addressShippingState.collectAsState()

    val addressName = rememberState { "" }
    val recipient = rememberState { "" }
    val phoneNumber = rememberState { "" }
    val street = rememberState { "" }
    val addressType = rememberState { "HOME" }
    val isDefault = rememberState { false }
    val isLoading = rememberState { false }

    val isValid by remember {
        derivedStateOf {
            addressName.value.isNotBlank() &&
            shippingState.selectedProvince != null &&
            shippingState.selectedDistrict != null &&
            shippingState.selectedWard != null &&
            street.value.isNotBlank()
        }
    }

    // Handle create address result
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.createAddressState) {
            it
                .onLoading {
                    isLoading.value = true
                }
                .onSuccess {
                    isLoading.value = false
                    Toast.makeText(
                        context,
                        "Thêm địa chỉ thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.resetCreateAddressState()
                    viewModel.resetAddressShippingState()
                    onBack()
                }
                .onError { exception ->
                    isLoading.value = false
                    Toast.makeText(
                        context,
                        "Thêm địa chỉ thất bại: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    MaxSizeColumn(
        modifier = Modifier
            .background(color = colorResource(R.color.colorSystem_background_level_0))
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.resetAddressShippingState()
                onBack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = colorResource(R.color.colorSystem_heading_button)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "Thêm địa chỉ",
                    style = CustomTypography.TextBold,
                    fontSize = 18.sp,
                    color = colorResource(R.color.colorSystem_heading_button)
                )
                Text(
                    text = "Thay đổi thông tin địa chỉ nhận hàng",
                    style = CustomTypography.TextRegular,
                    fontSize = 12.sp,
                    color = colorResource(R.color.colorSystem_normal_text)
                )
            }
        }

        HorizontalDivider(color = colorResource(R.color.colorSystem_background_level_2))

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Province (required)
            Text(
                text = "Tỉnh/Thành phố *",
                style = CustomTypography.TextMedium,
                fontSize = 14.sp,
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Spacer(modifier = Modifier.height(8.dp))
            AddressDropdown(
                selectedText = shippingState.selectedProvince?.name ?: "Chọn Tỉnh/Thành phố",
                isLoading = shippingState.provincesLoading,
                items = shippingState.provinces,
                onItemSelected = viewModel::selectProvinceForAddress,
                itemLabel = { it.name }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // District (required)
            Text(
                text = "Quận/Huyện *",
                style = CustomTypography.TextMedium,
                fontSize = 14.sp,
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Spacer(modifier = Modifier.height(8.dp))
            AddressDropdown(
                selectedText = shippingState.selectedDistrict?.name ?: "Chọn Quận/Huyện",
                isLoading = shippingState.districtsLoading,
                items = shippingState.districts,
                onItemSelected = viewModel::selectDistrictForAddress,
                itemLabel = { it.name },
                enabled = shippingState.selectedProvince != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ward (required)
            Text(
                text = "Phường/Xã *",
                style = CustomTypography.TextMedium,
                fontSize = 14.sp,
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Spacer(modifier = Modifier.height(8.dp))
            AddressDropdown(
                selectedText = shippingState.selectedWard?.name ?: "Chọn Phường/Xã",
                isLoading = shippingState.wardsLoading,
                items = shippingState.wards,
                onItemSelected = viewModel::selectWardForAddress,
                itemLabel = { it.name },
                enabled = shippingState.selectedDistrict != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Street (required)
            Text(
                text = "Địa chỉ nhà *",
                style = CustomTypography.TextMedium,
                fontSize = 14.sp,
                color = colorResource(R.color.colorSystem_heading_button)
            )
            FilledTextField(
                value = street.value,
                onValueChange = { street.value = it },
                hint = "Nhập địa chỉ nhà",
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Address Name (required)
            Text(
                text = "Tên gợi nhớ *",
                style = CustomTypography.TextMedium,
                fontSize = 14.sp,
                color = colorResource(R.color.colorSystem_heading_button)
            )
            FilledTextField(
                value = addressName.value,
                onValueChange = { addressName.value = it },
                hint = "Nhập tên gợi nhớ",
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recipient (optional)
            Text(
                text = "Người nhận",
                style = CustomTypography.TextMedium,
                fontSize = 14.sp,
                color = colorResource(R.color.colorSystem_heading_button)
            )
            FilledTextField(
                value = recipient.value,
                onValueChange = { recipient.value = it },
                hint = "Nhập người nhận",
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone number (optional)
            Text(
                text = "Số điện thoại",
                style = CustomTypography.TextMedium,
                fontSize = 14.sp,
                color = colorResource(R.color.colorSystem_heading_button)
            )
            FilledTextField(
                value = phoneNumber.value,
                onValueChange = { phoneNumber.value = it },
                hint = "Nhập số điện thoại",
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Address Type (required)
            Text(
                text = "Loại địa chỉ *",
                style = CustomTypography.TextMedium,
                fontSize = 14.sp,
                color = colorResource(R.color.colorSystem_heading_button)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("HOME" to "Nhà", "OFFICE" to "Văn phòng").forEach { (type, label) ->
                    FilterChip(
                        selected = addressType.value == type,
                        onClick = { addressType.value = type },
                        label = {
                            Text(
                                text = label,
                                style = CustomTypography.TextMedium,
                                fontSize = 14.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorResource(R.color.colorSystem_heading_button),
                            selectedLabelColor = colorResource(R.color.colorSystem_background_level_0)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Set as default switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Đặt làm địa chỉ mặc định",
                    style = CustomTypography.TextMedium,
                    fontSize = 14.sp,
                    color = colorResource(R.color.colorSystem_normal_text)
                )
                Switch(
                    checked = isDefault.value,
                    onCheckedChange = { isDefault.value = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorResource(R.color.colorSystem_background_level_0),
                        checkedTrackColor = colorResource(R.color.colorSystem_heading_button)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom button
        FilledButton(
            text = "Lưu",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            enabled = isValid && !isLoading.value,
            onClick = {
                viewModel.createAddress(
                    name = addressName.value,
                    recipient = recipient.value.takeIf { it.isNotBlank() },
                    phoneNumber = phoneNumber.value.takeIf { it.isNotBlank() },
                    provinceId = shippingState.selectedProvince!!.id,
                    districtId = shippingState.selectedDistrict!!.id,
                    wardCode = shippingState.selectedWard!!.code,
                    street = street.value,
                    addressType = addressType.value,
                    isDefault = isDefault.value
                )
            }
        )
    }
}

@Composable
private fun <T> AddressDropdown(
    selectedText: String,
    isLoading: Boolean,
    items: List<T>,
    onItemSelected: (T?) -> Unit,
    itemLabel: (T) -> String,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (enabled) colorResource(R.color.colorSystem_background_level_2)
                else colorResource(R.color.colorSystem_background_level_1)
            )
            .clickable(enabled = enabled && !isLoading) { expanded = true }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colorResource(R.color.colorSystem_heading_button),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = selectedText,
                    style = CustomTypography.TextMedium,
                    fontSize = 14.sp,
                    color = if (selectedText.startsWith("Chọn"))
                        colorResource(R.color.colorSystem_text_button)
                    else
                        colorResource(R.color.colorSystem_heading_button)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = colorResource(R.color.colorSystem_normal_text)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 300.dp)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = itemLabel(item),
                            style = CustomTypography.TextMedium,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

