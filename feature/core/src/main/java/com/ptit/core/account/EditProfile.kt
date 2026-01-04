package com.ptit.core.account

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.MyCrossFade
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FilledTextField
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.component.NeutralButton
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun EditProfile(
    backStackEntry: NavBackStackEntry,
    onBack: () -> Unit,
    onNavigateToAddAddress: () -> Unit,
) {
    LocalBottomNavigationVisibility.current.value = false

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val viewModel = hiltViewModel<AccountViewModel>(viewModelStoreOwner = backStackEntry)
    val uiModel = viewModel.updateProfileUiModel.collectAsStateWithLifecycle()
    val addressesState = viewModel.addressesState.collectAsStateWithLifecycle()

    // 👇 State xóa địa chỉ
    val deleteAddressState by viewModel.deleteAddressState.collectAsStateWithLifecycle()

    val isShowProgressBar = rememberState { false }
    val isShowAlertDialog = rememberState { false }
    // (Bỏ isShowAddAddressDialog vì dùng navigation riêng)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.uploadAvatar(it) }
        }
    )

    // Fetch addresses on first load
    LaunchedEffect(Unit) {
        viewModel.fetchAddresses()
    }

    // Handle upload avatar state
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.uploadAvatarState) {
            it
                .onLoading { isShowProgressBar.value = true }
                .onSuccess {
                    isShowProgressBar.value = false
                    Toast.makeText(context, "Tải ảnh lên thành công", Toast.LENGTH_SHORT).show()
                    viewModel.resetUploadAvatarState()
                }
                .onError { error ->
                    isShowProgressBar.value = false
                    Toast.makeText(context, "Tải ảnh lên thất bại: ${error.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // Handle update profile state
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.updateProfileState) {
            it
                .onLoading { isShowProgressBar.value = true }
                .onSuccess {
                    isShowProgressBar.value = false
                    Toast.makeText(context, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show()
                    onBack()
                }
                .onError { error ->
                    isShowProgressBar.value = false
                    Toast.makeText(context, "Cập nhật thông tin thất bại: ${error.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // 👇 Handle delete address state
    LaunchedEffect(deleteAddressState) {
        deleteAddressState
            .onLoading { isShowProgressBar.value = true }
            .onSuccess {
                isShowProgressBar.value = false
                Toast.makeText(context, "Đã xóa địa chỉ", Toast.LENGTH_SHORT).show()
                viewModel.resetDeleteAddressState()
            }
            .onError { error ->
                isShowProgressBar.value = false
                Toast.makeText(context, "Xóa thất bại: ${error.message}", Toast.LENGTH_SHORT).show()
                viewModel.resetDeleteAddressState()
            }
    }

    MaxSizeColumn(
        modifier = Modifier
            .background(color = colorResource(R.color.colorSystem_background_level_0))
            .padding(horizontal = 16.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header (Giữ nguyên) ---
        MaxWidthRow(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (uiModel.value.isChanged) isShowAlertDialog.value = true else onBack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = colorResource(id = R.color.colorSystem_heading_button)
                )
            }

            Text(
                text = "Cập nhật thông tin",
                style = CustomTypography.TextBold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )

            TextButton(
                onClick = viewModel::updateProfile,
                enabled = uiModel.value.isChanged && uiModel.value.isValid,
                colors = ButtonDefaults.textButtonColors(
                    disabledContentColor = colorResource(id = R.color.colorSystem_normal_text),
                    contentColor = colorResource(id = R.color.colorSystem_heading_button)
                )
            ) {
                Text(
                    text = "Lưu",
                    style = CustomTypography.TextMedium,
                    fontSize = 16.sp,
                )
            }
        }

        // --- Avatar (Giữ nguyên) ---
        Box(contentAlignment = Alignment.BottomEnd) {
            GlideImage(
                model = uiModel.value.avatar.takeIf { it.isNotEmpty() }
                    ?: "https://i.pinimg.com/564x/19/b8/d6/19b8d6e9b13eef23ec9c746968bb88b1.jpg",
                contentDescription = "Ảnh đại diện",
                modifier = Modifier.size(100.dp).clip(CircleShape),
                contentScale = ContentScale.FillBounds,
                transition = MyCrossFade
            ) { it.centerCrop() }

            IconButton(
                onClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.offset(x = 8.dp, y = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Chỉnh sửa",
                    tint = colorResource(R.color.colorSystem_heading_button),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // --- Fields (Name, Phone, Email) Giữ nguyên ---
        Text(
            text = "Họ và tên",
            style = CustomTypography.TextMedium,
            fontSize = 16.sp,
            color = colorResource(id = R.color.colorSystem_heading_button),
            modifier = Modifier.fillMaxWidth()
        )
        FilledTextField(
            value = uiModel.value.name,
            onValueChange = viewModel::onNameChanged,
            hint = "Nhập họ và tên",
            singleLine = true,
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
            trailingContent = if (uiModel.value.name.isNotBlank()) {
                { IconButton(onClick = { viewModel.onNameChanged("") }) { Icon(Icons.Default.Clear, "Xóa tên") } }
            } else null
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Số điện thoại",
            style = CustomTypography.TextMedium,
            fontSize = 16.sp,
            color = colorResource(id = R.color.colorSystem_heading_button),
            modifier = Modifier.fillMaxWidth()
        )
        FilledTextField(
            value = uiModel.value.phoneNumber,
            onValueChange = viewModel::onPhoneNumberChanged,
            hint = "Nhập số điện thoại",
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
            trailingContent = if (uiModel.value.phoneNumber.isNotBlank()) {
                { IconButton(onClick = { viewModel.onPhoneNumberChanged("") }) { Icon(Icons.Default.Clear, "Xóa SĐT") } }
            } else null
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Email",
            style = CustomTypography.TextMedium,
            fontSize = 16.sp,
            color = colorResource(id = R.color.colorSystem_heading_button),
            modifier = Modifier.fillMaxWidth()
        )
        FilledTextField(
            value = uiModel.value.original.email,
            onValueChange = { },
            hint = "Email",
            singleLine = true,
            enabled = false,
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // --- 👇 DANH SÁCH ĐỊA CHỈ (CẬP NHẬT MỚI) ---
        Text(
            text = "Danh sách địa chỉ",
            style = CustomTypography.TextMedium,
            fontSize = 16.sp,
            color = colorResource(id = R.color.colorSystem_heading_button),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        when (val state = addressesState.value) {
            is com.ptit.domain.utils.Resource.Loading -> {
                Text(
                    text = "Đang tải địa chỉ...",
                    style = CustomTypography.TextRegular,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.colorSystem_greyscale_500)
                )
            }
            is com.ptit.domain.utils.Resource.Success -> {
                val addresses = state.data
                if (addresses.isEmpty()) {
                    Text(
                        text = "Chưa có địa chỉ nào",
                        style = CustomTypography.TextRegular,
                        color = colorResource(id = R.color.colorSystem_greyscale_500),
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Dùng Column để hiển thị list (vì bên ngoài đã có MaxSizeColumn scrollable)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        addresses.forEach { address ->
                            AddressItem(
                                address = address,
                                onDelete = { viewModel.deleteAddress(address.id) }
                            )
                        }
                    }
                }
            }
            is com.ptit.domain.utils.Resource.Error -> {
                Text(
                    text = "Lỗi tải địa chỉ",
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
            else -> {}
        }

        Spacer(Modifier.height(16.dp))

        // Add address button
        FilledButton(
            text = "Thêm địa chỉ mới",
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToAddAddress
        )

        Spacer(Modifier.height(24.dp))
    }

    // Dialog xác nhận thoát (Giữ nguyên)
    if (isShowAlertDialog.value) {
        AlertDialog(
            onDismissRequest = { isShowAlertDialog.value = false },
            confirmButton = {
                FilledButton(text = "Thoát") {
                    isShowAlertDialog.value = false
                    onBack()
                }
            },
            dismissButton = {
                NeutralButton(text = "Hủy") { isShowAlertDialog.value = false }
            },
            title = {
                Text(
                    text = "Bạn có chắc chắn muốn thoát không?",
                    style = CustomTypography.TextMedium,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.colorSystem_heading_button)
                )
            },
            text = {
                Text(
                    text = "Tất cả thay đổi sẽ không được lưu lại",
                    style = CustomTypography.TextMedium,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.colorSystem_normal_text)
                )
            },
        )
    }
}

// 👇 COMPOSABLE HIỂN THỊ ITEM ĐỊA CHỈ
@Composable
fun AddressItem(
    address: com.ptit.domain.entity.address.AddressDomainEntity,
    onDelete: () -> Unit
) {
    val primaryColor = colorResource(id = R.color.colorSystem_heading_button)
    val borderColor = colorResource(id = R.color.colorSystem_stroke)

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (address.isDefault) primaryColor else borderColor,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Dòng 1: Tên người nhận + SĐT + Tag Mặc định
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = address.recipientName,
                        style = CustomTypography.TextBold,
                        fontSize = 14.sp,
                        color = primaryColor
                    )

                    if (address.phoneNumber.isNotBlank()) {
                        Text(
                            text = " | ${address.phoneNumber}",
                            style = CustomTypography.TextRegular,
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.colorSystem_normal_text)
                        )
                    }

                    if (address.isDefault) {
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = "[Mặc định]",
                            style = CustomTypography.TextRegular,
                            fontSize = 12.sp,
                            color = colorResource(id = R.color.colorSystem_tint_red)
                        )
                    }
                }

                // Dòng 2: Tên gợi nhớ (VD: Nhà riêng) - Nếu khác với tên người nhận
                if (address.label.isNotBlank() && address.label != address.recipientName && address.label != "Địa chỉ") {
                    Text(
                        text = "(${address.label})",
                        style = CustomTypography.TextRegular,
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.colorSystem_greyscale_500),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                // Dòng 3: Địa chỉ chi tiết
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = address.fullAddress,
                    style = CustomTypography.TextRegular,
                    fontSize = 13.sp,
                    color = colorResource(id = R.color.colorSystem_normal_text),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Nút xóa (Hiển thị icon thùng rác)
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Xóa địa chỉ",
                    tint = colorResource(id = R.color.colorSystem_greyscale_500),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}