package com.ptit.core.account

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

    val isShowProgressBar = rememberState { false }
    val isShowAlertDialog = rememberState { false }
    val isShowAddAddressDialog = rememberState { false }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                // Upload avatar ngay khi chọn
                viewModel.uploadAvatar(it)
            }
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
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onSuccess { _ ->
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Tải ảnh lên thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.resetUploadAvatarState()
                }
                .onError { error ->
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Tải ảnh lên thất bại: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.updateProfileState) {
            it
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onSuccess {
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Cập nhật thông tin thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    onBack()
                }
                .onError { error ->
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Cập nhật thông tin thất bại: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    // Handle create address state
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.createAddressState) {
            it
                .onSuccess {
                    isShowAddAddressDialog.value = false
                    viewModel.resetCreateAddressState()
                    Toast.makeText(
                        context,
                        "Thêm địa chỉ thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onError { exception ->
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
            .padding(horizontal = 16.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with back button and save button
        MaxWidthRow(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (uiModel.value.isChanged) {
                    isShowAlertDialog.value = true
                } else {
                    onBack()
                }
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

        // Avatar section
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            GlideImage(
                model = uiModel.value.avatar.takeIf { it.isNotEmpty() }
                ?: "https://i.pinimg.com/564x/19/b8/d6/19b8d6e9b13eef23ec9c746968bb88b1.jpg",
                contentDescription = "Ảnh đại diện",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.FillBounds,
                transition = MyCrossFade
            ) {
                it.centerCrop()
            }

            IconButton(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.offset(x = 8.dp, y = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Chỉnh sửa ảnh đại diện",
                    tint = colorResource(R.color.colorSystem_heading_button),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Name field
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
            maxLines = 1,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            trailingContent = if (uiModel.value.name.isNotBlank()) {
                {
                    IconButton(onClick = {
                        viewModel.onNameChanged("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Xóa tên"
                        )
                    }
                }
            } else null
        )

        Spacer(Modifier.height(12.dp))

        // Phone number field
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
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Phone
            ),
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            trailingContent = if (uiModel.value.phoneNumber.isNotBlank()) {
                {
                    IconButton(onClick = {
                        viewModel.onPhoneNumberChanged("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Xóa số điện thoại"
                        )
                    }
                }
            } else null
        )

        Spacer(Modifier.height(12.dp))

        // Email field (read-only)
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
            maxLines = 1,
            enabled = false,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Default address section
        Text(
            text = "Địa chỉ mặc định",
            style = CustomTypography.TextMedium,
            fontSize = 16.sp,
            color = colorResource(id = R.color.colorSystem_heading_button),
            modifier = Modifier.fillMaxWidth()
        )

        val defaultAddress = when (val state = addressesState.value) {
            is com.ptit.domain.utils.Resource.Success -> state.data.firstOrNull { it.isDefault }
            else -> null
        }

        FilledTextField(
            value = defaultAddress?.fullAddress ?: "Chưa có",
            onValueChange = { },
            hint = "Địa chỉ",
            singleLine = false,
            maxLines = 3,
            enabled = false,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Add address button
        FilledButton(
            text = "Thêm địa chỉ mới",
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToAddAddress
        )
    }

    // Confirmation dialog
    if (isShowAlertDialog.value) {
        AlertDialog(
            onDismissRequest = {
                isShowAlertDialog.value = false
            },
            confirmButton = {
                FilledButton(
                    text = "Thoát"
                ) {
                    isShowAlertDialog.value = false
                    onBack()
                }
            },
            dismissButton = {
                NeutralButton(
                    text = "Hủy",
                ) {
                    isShowAlertDialog.value = false
                }
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