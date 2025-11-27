package com.ptit.core.order.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.component.FilledTextField
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.shipping.DistrictEntity
import com.ptit.domain.entity.shipping.ProvinceEntity
import com.ptit.domain.entity.shipping.WardEntity


@Composable
private fun ShippingTextField(
    label: String,
    value: TextFieldValue,
    hint: String,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label, // 🔴 LABEL
            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
            color = colorResource(R.color.colorSystem_greyscale_700)
        )
        Spacer(Modifier.height(4.dp))
        // Sử dụng FilledTextField đã định nghĩa
        FilledTextField(
            value = value,
            hint = hint,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            // Thêm các tham số khác nếu cần, ví dụ: singleLine = true
        )
    }
}

/**
 * 📦 Thông tin vận chuyển (người nhận và địa chỉ GHN)
 * Dạng mở rộng (Expandable Card)
 */
@Composable
fun ShippingAddressSection(
    // 🧍 Thông tin người nhận
    receiverName: TextFieldValue,
    receiverPhone: TextFieldValue,
    onNameChange: (TextFieldValue) -> Unit,
    onPhoneChange: (TextFieldValue) -> Unit,

    // 📍 Dữ liệu GHN
    provinces: List<ProvinceEntity>,
    districts: List<DistrictEntity>,
    wards: List<WardEntity>,
    selectedProvince: ProvinceEntity?,
    selectedDistrict: DistrictEntity?,
    selectedWard: WardEntity?,

    // 🔴 BỔ SUNG CÁC THAM SỐ LOADING STATE
    provincesLoading: Boolean,
    districtsLoading: Boolean,
    wardsLoading: Boolean,

    onSelectProvince: (ProvinceEntity?) -> Unit,
    onSelectDistrict: (DistrictEntity?) -> Unit,
    onSelectWard: (WardEntity?) -> Unit,

    // 🏠 Địa chỉ cụ thể
    detailAddress: TextFieldValue,
    onDetailAddressChange: (TextFieldValue) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    val isProvinceSelected = selectedProvince != null
    val isDistrictSelected = selectedDistrict != null


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
    ) {
        // 1. HEADER (Dạng Tab V)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 🔴 NHÓM 1: ICON + TEXT (Căn lề trái)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                // Modifier.weight(1f) có thể cần thiết nếu bạn muốn nhóm này chiếm nhiều không gian hơn
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn, // Icon Địa điểm
                    contentDescription = "Thông tin nhận hàng",
                    tint = colorResource(R.color.colorSystem_heading_button),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Thông tin nhận hàng",
                    style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )
            }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 💡 THÊM: Dòng note đỏ
                Text(
                    text = "Vui lòng nhập địa chỉ trước khi sát nhập",
                    style = CustomTypography.TextMedium,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth()
                )

                // Tên người nhận và SĐT
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ShippingTextField(
                        label = "Khách hàng",
                        value = receiverName,
                        hint = "Nhập tên người nhận",
                        onValueChange = onNameChange,
                        modifier = Modifier.weight(1f)
                    )
                    ShippingTextField(
                        label = "Số điện thoại",
                        value = receiverPhone,
                        hint = "Nhập số điện thoại người nhận",
                        onValueChange = onPhoneChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                AddressDropdownSearchField(
                    label = "Tỉnh/Thành phố",
                    items = provinces,
                    selectedItem = selectedProvince,
                    isLoading = provincesLoading,
                    enabled = !provincesLoading,
                    onItemSelected = onSelectProvince ,
                    itemNameSelector = { it.name },
                    modifier = Modifier.fillMaxWidth()
                )
                AddressDropdownSearchField(
                    label = "Quận/Huyện",
                    items = districts,
                    selectedItem = selectedDistrict,
                    isLoading = districtsLoading,
                    enabled = isProvinceSelected && !districtsLoading,
                    onItemSelected = onSelectDistrict ,
                    itemNameSelector = { it.name },
                    modifier = Modifier.fillMaxWidth()
                )
                // Phường/Xã
                AddressDropdownSearchField(
                    label = "Phường/Xã",
                    items = wards,
                    selectedItem = selectedWard,
                    isLoading = wardsLoading,
                    enabled = isDistrictSelected && !wardsLoading,
                    onItemSelected = onSelectWard ,
                    itemNameSelector = { it.name },
                    modifier = Modifier.fillMaxWidth()
                )


                // Địa chỉ cụ thể
                ShippingTextField(
                    label = "Dịa chỉ cụ thể",
                    value = detailAddress,
                    hint = "Số nhà, tên đường, khu vực...",
                    onDetailAddressChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Component Dropdown/Autocomplete cho địa chỉ (Tỉnh/Huyện/Xã)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> AddressDropdownSearchField(
    label: String,
    items: List<T>,
    selectedItem: T?,
    isLoading: Boolean,
    enabled: Boolean,
    onItemSelected: (T?) -> Unit,
    itemNameSelector: (T) -> String,
    modifier: Modifier = Modifier
) {
    // Trạng thái cho việc nhập và tìm kiếm
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(TextFieldValue(selectedItem?.let(itemNameSelector) ?: "")) }

    // 🔴 1. Cập nhật searchText khi selectedItem thay đổi (Sau khi chọn hoặc ViewModel cập nhật)
    LaunchedEffect(selectedItem) {
        searchText = TextFieldValue(selectedItem?.let(itemNameSelector) ?: "")
    }

    // Lọc danh sách dựa trên searchText
    val filteredItems = remember(items, searchText.text) {
        items.filter {
            itemNameSelector(it).contains(searchText.text, ignoreCase = true)
        }
    }


    Column(modifier = modifier) {
        Text(
            text = label,
            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
            color = colorResource(R.color.colorSystem_normal_text)
        )
        Spacer(Modifier.height(4.dp))

        ExposedDropdownMenuBox(
            expanded = expanded && filteredItems.isNotEmpty(), // Chỉ mở nếu có kết quả
            onExpandedChange = {
                if (enabled) {
                    expanded = !expanded
                }
            }
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = isLoading,
                enabled = enabled,
                value = searchText,
                onValueChange = { newValue ->
                    searchText = newValue
                    // Mở menu khi bắt đầu nhập (nếu chưa mở)
                    if (newValue.text.isNotEmpty() && !expanded) {
                        expanded = true
                    }
                    // Nếu giá trị nhập không khớp với item đã chọn, hủy chọn item đó
                    if (selectedItem != null && itemNameSelector(selectedItem) != newValue.text) {
                        onItemSelected(null) // ✅ Gọi an toàn với null
                    }
                },
                placeholder = { Text(if (isLoading) "Đang tải..." else "Chọn $label") },
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    } else {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                        )
                    }
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(
                    focusedContainerColor = colorResource(R.color.colorSystem_background_level_0),
                    unfocusedContainerColor = colorResource(R.color.colorSystem_background_level_0)
                )
            )

            ExposedDropdownMenu(
                expanded = expanded && filteredItems.isNotEmpty(),
                onDismissRequest = { expanded = false },
            ) {
                filteredItems.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = itemNameSelector(selectionOption),
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            onItemSelected(selectionOption)
                            expanded = false
                            searchText = TextFieldValue(itemNameSelector(selectionOption))
                        },
                        contentPadding = PaddingValues(16.dp)
                    )
                }
            }
        }
    }
}