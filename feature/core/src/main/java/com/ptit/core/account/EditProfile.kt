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
import com.ptit.common.presentation.component.FilledTextField
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
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
) {
    LocalBottomNavigationVisibility.current.value = false

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val viewModel = hiltViewModel<AccountViewModel>(viewModelStoreOwner = backStackEntry)
    val uiModel = viewModel.updateProfileUiModel.collectAsStateWithLifecycle()

    val isShowProgressBar = rememberState { false }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                viewModel.onAvatarChanged(it)
            }
        }
    )

    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.updateProfileState) {
            it
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onSuccess {
                    isShowProgressBar.value = false
                    onBack()
                }
                .onError {
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Cập nhật thông tin thất bại",
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
        MaxWidthRow(
            modifier = Modifier
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
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
                enabled = uiModel.value.isChanged && uiModel.value.isValid.value,
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


        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            GlideImage(
                model = uiModel.value.avatar.takeIf { it.path?.isNotEmpty() == true }
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
                modifier = Modifier
                    .offset(x = 8.dp, y = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Chỉnh sửa hồ sơ",
                    tint = colorResource(R.color.colorSystem_heading_button),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

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
                            contentDescription = "Clear name"
                        )
                    }
                }
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
            value = uiModel.value.phone,
            onValueChange = viewModel::onPhoneChanged,
            hint = "Nhập số điện thoại",
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Phone
            ),
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            trailingContent = if (uiModel.value.phone.isNotBlank()) {
                {
                    IconButton(onClick = {
                        viewModel.onPhoneChanged("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear phone"
                        )
                    }
                }
            } else null
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Địa chỉ",
            style = CustomTypography.TextMedium,
            fontSize = 16.sp,
            color = colorResource(id = R.color.colorSystem_heading_button),
            modifier = Modifier.fillMaxWidth()
        )

        FilledTextField(
            value = uiModel.value.address,
            onValueChange = viewModel::onAddressChanged,
            hint = "Nhập địa chỉ",
            minLines = 5,
            maxLines = 5,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            trailingContent = if (uiModel.value.address.isNotBlank()) {
                {
                    IconButton(onClick = {
                        viewModel.onAddressChanged("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear address"
                        )
                    }
                }
            } else null
        )
    }

}