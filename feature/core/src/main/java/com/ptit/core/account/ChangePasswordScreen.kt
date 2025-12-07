package com.ptit.core.account

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FilledTextField
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess

@Composable
fun ChangePasswordScreen(
    backStackEntry: NavBackStackEntry,
    onBack: () -> Unit,
) {
    LocalBottomNavigationVisibility.current.value = false

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val viewModel = hiltViewModel<AccountViewModel>(viewModelStoreOwner = backStackEntry)
    val uiModel = viewModel.changePasswordUiModel.collectAsStateWithLifecycle()

    val isShowProgressBar = rememberState { false }

    // Password visibility states
    val currentPasswordVisible = rememberState { false }
    val newPasswordVisible = rememberState { false }
    val confirmPasswordVisible = rememberState { false }

    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.changePasswordState) {
            it
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onSuccess {
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Đổi mật khẩu thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    onBack()
                }
                .onError { error ->
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        error.message ?: "Đổi mật khẩu thất bại",
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
        // Header
        MaxWidthRow(
            modifier = Modifier.padding(vertical = 12.dp),
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
                text = "Đổi mật khẩu",
                style = CustomTypography.TextBold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )

            // Placeholder for symmetry
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(Modifier.height(24.dp))

        // Current Password field
        Text(
            text = "Mật khẩu hiện tại",
            style = CustomTypography.TextMedium,
            fontSize = 16.sp,
            color = colorResource(id = R.color.colorSystem_heading_button),
            modifier = Modifier.fillMaxWidth()
        )

        FilledTextField(
            value = uiModel.value.currentPassword,
            onValueChange = viewModel::onCurrentPasswordChanged,
            hint = "Nhập mật khẩu hiện tại",
            singleLine = true,
            maxLines = 1,
            visualTransformation = if (currentPasswordVisible.value)
                VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            trailingContent = {
                Row {
                    if (uiModel.value.currentPassword.isNotBlank()) {
                        IconButton(onClick = { viewModel.onCurrentPasswordChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Xóa"
                            )
                        }
                    }
                    IconButton(onClick = { currentPasswordVisible.value = !currentPasswordVisible.value }) {
                        Icon(
                            imageVector = if (currentPasswordVisible.value)
                                Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (currentPasswordVisible.value) "Ẩn" else "Hiện"
                        )
                    }
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        // New Password field
        Text(
            text = "Mật khẩu mới",
            style = CustomTypography.TextMedium,
            fontSize = 16.sp,
            color = colorResource(id = R.color.colorSystem_heading_button),
            modifier = Modifier.fillMaxWidth()
        )

        FilledTextField(
            value = uiModel.value.newPassword,
            onValueChange = viewModel::onNewPasswordChanged,
            hint = "Nhập mật khẩu mới (tối thiểu 6 ký tự)",
            singleLine = true,
            maxLines = 1,
            visualTransformation = if (newPasswordVisible.value)
                VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            trailingContent = {
                Row {
                    if (uiModel.value.newPassword.isNotBlank()) {
                        IconButton(onClick = { viewModel.onNewPasswordChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Xóa"
                            )
                        }
                    }
                    IconButton(onClick = { newPasswordVisible.value = !newPasswordVisible.value }) {
                        Icon(
                            imageVector = if (newPasswordVisible.value)
                                Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (newPasswordVisible.value) "Ẩn" else "Hiện"
                        )
                    }
                }
            },
            isError = uiModel.value.newPassword.isNotBlank() && uiModel.value.newPassword.length < 6,
            errorMessage = if (uiModel.value.newPassword.isNotBlank() && uiModel.value.newPassword.length < 6)
                "Mật khẩu phải có ít nhất 6 ký tự" else ""
        )

        Spacer(Modifier.height(16.dp))

        // Confirm Password field
        Text(
            text = "Xác nhận mật khẩu mới",
            style = CustomTypography.TextMedium,
            fontSize = 16.sp,
            color = colorResource(id = R.color.colorSystem_heading_button),
            modifier = Modifier.fillMaxWidth()
        )

        FilledTextField(
            value = uiModel.value.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChanged,
            hint = "Nhập lại mật khẩu mới",
            singleLine = true,
            maxLines = 1,
            visualTransformation = if (confirmPasswordVisible.value)
                VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            trailingContent = {
                Row {
                    if (uiModel.value.confirmPassword.isNotBlank()) {
                        IconButton(onClick = { viewModel.onConfirmPasswordChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Xóa"
                            )
                        }
                    }
                    IconButton(onClick = { confirmPasswordVisible.value = !confirmPasswordVisible.value }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible.value)
                                Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible.value) "Ẩn" else "Hiện"
                        )
                    }
                }
            },
            isError = uiModel.value.confirmPassword.isNotBlank() &&
                      uiModel.value.confirmPassword != uiModel.value.newPassword,
            errorMessage = if (uiModel.value.confirmPassword.isNotBlank() &&
                              uiModel.value.confirmPassword != uiModel.value.newPassword)
                "Mật khẩu xác nhận không khớp" else ""
        )

        Spacer(Modifier.height(16.dp))

        // Push button to bottom
        Spacer(Modifier.weight(1f))

        // Submit button at bottom
        FilledButton(
            text = "Đổi mật khẩu",
            onClick = viewModel::changePassword,
            enabled = uiModel.value.isValid && !isShowProgressBar.value,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        if (isShowProgressBar.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.colorSystem_heading_button)
                )
            }
        }
    }
}

