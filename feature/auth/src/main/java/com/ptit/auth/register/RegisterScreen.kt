package com.ptit.auth.register

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.component.BaseTextField
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.domain.utils.StatusCodeCategory
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess

@Composable
fun RegisterScreen(
    onNavigateToSignIn: () -> Unit,
    onRegisterSuccess: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val primaryColor = colorResource(id = R.color.colorSystem_heading_button)

    val viewModel = hiltViewModel<RegisterViewModel>()
    val uiModel = viewModel.uiModel.collectAsStateWithLifecycle()
    val isShowProgressBar = rememberState { false }

    // Handle registration success
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.registerState) {
            it
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onError {
                    isShowProgressBar.value = false
                    if (it.statusCodeCategory == StatusCodeCategory.SERVER_ERROR)
                        Toast.makeText(
                            context,
                            "Unexpected error occurred. Please try again later.",
                            Toast.LENGTH_SHORT
                        ).show()
                }
                .onSuccess {
                    onRegisterSuccess()
                }
        }
    }

    MaxSizeColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(colorResource(R.color.colorSystem_background_level_0))
            .padding(horizontal = 24.dp)
            .statusBarsPadding()
    ) {
        // Title
        Text(
            text = "Sign up",
            color = primaryColor,
            style = CustomTypography.TextBold,
            fontSize = 24.sp,
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Username field
        Column {
            Text(
                text = "Email",
                color = primaryColor,
                style = CustomTypography.TextMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            BaseTextField(
                value = uiModel.value.email,
                onValueChange = viewModel::onEmailChanged,
                modifier = Modifier.fillMaxWidth(),
                hint = "Enter your email",
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true,
                isError = !uiModel.value.isValidEmail,
                errorMessage = when(uiModel.value.emailErrorType){
                    RegisterUiModel.EmailErrorType.INVALID_EMAIL -> "Invalid email"
                    RegisterUiModel.EmailErrorType.EMAIL_ALREADY_EXISTS -> "Email already exists"
                    else -> ""
                }
            )
        }

        Spacer(Modifier.height(11.dp))

        // Password field
        Column {
            Text(
                text = "Password",
                color = primaryColor,
                style = CustomTypography.TextMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            BaseTextField(
                value = uiModel.value.password,
                onValueChange = viewModel::onPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                hint = "Enter your password",
                visualTransformation = if (uiModel.value.isShowPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                isError = !uiModel.value.isValidPassword,
                trailingIcon = {
                    IconButton(onClick = viewModel::toggleShowPassword) {
                        Icon(
                            imageVector = if (uiModel.value.isShowPassword) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = if (uiModel.value.isShowPassword) "Hide password"
                            else "Show password"
                        )
                    }
                },
                errorMessage = "Password must be at least 6 characters",
            )
        }

        Spacer(Modifier.height(11.dp))

        Column {
            Text(
                text = "Confirm Password",
                color = primaryColor,
                style = CustomTypography.TextMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            BaseTextField(
                value = uiModel.value.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                hint = "Confirm your password",
                visualTransformation = if (uiModel.value.isShowConfirmPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                isError = !uiModel.value.isValidConfirmPassword,
                trailingIcon = {
                    IconButton(onClick = viewModel::toggleShowConfirmPassword) {
                        Icon(
                            imageVector = if (uiModel.value.isShowConfirmPassword) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = if (uiModel.value.isShowConfirmPassword) "Hide password"
                            else "Show password"
                        )
                    }
                },
                errorMessage = "Password not match",
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sign in button
        FilledButton(
            onClick = {
                focusManager.clearFocus()
                viewModel.register()
            },
            enabled = uiModel.value.isEnableRegisterButton.value,
            text = "Sign up",
            modifier = Modifier.fillMaxWidth()
        )

        // Or divider
        Text(
            text = "Or",
            color = colorResource(R.color.colorSystem_normal_text),
            style = CustomTypography.TextRegular,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Sign up row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Already have an account?",
                color = colorResource(R.color.colorSystem_normal_text),
                style = CustomTypography.TextRegular
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(
                onClick = onNavigateToSignIn,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Sign in",
                    color = primaryColor,
                    style = CustomTypography.TextMedium
                )
            }
        }
    }

    // Show loading indicator when logging in
    if (isShowProgressBar.value) {
        FullScreenProgressBar()
    }
}