package com.ptit.auth.login

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
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
import com.ptit.common.presentation.component.FilledTextField
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
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val viewModel = hiltViewModel<LoginViewModel>()
    val uiModel = viewModel.uiModel.collectAsStateWithLifecycle()
    val isShowProgressBar = rememberState { false }

    // Define colors from resources
    val primaryColor = colorResource(id = R.color.colorSystem_heading_button)

    // Handle login result
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.loginState) {
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
                    onLoginSuccess()
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
            text = "Sign in",
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

            FilledTextField(
                value = uiModel.value.username,
                onValueChange = viewModel::onUsernameChanged,
                modifier = Modifier.fillMaxWidth(),
                hint = "Enter your email",
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true,
                isError = !uiModel.value.isValidUsername,
                errorMessage = "Account not exist",
                trailingContent = {
                    if (uiModel.value.username.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.onUsernameChanged("")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear username"
                            )
                        }
                    }
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

            FilledTextField(
                value = uiModel.value.password,
                onValueChange = viewModel::onPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                hint = "Enter your password",
                visualTransformation = if (uiModel.value.isShowPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                isError = !uiModel.value.isValidPassword,
                trailingContent = {
                    IconButton(onClick = viewModel::toggleShowPassword) {
                        Icon(
                            imageVector = if (uiModel.value.isShowPassword) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = if (uiModel.value.isShowPassword) "Hide password"
                            else "Show password"
                        )
                    }
                },
                errorMessage = "Incorrect password",
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sign in button
        FilledButton(
            onClick = {
                focusManager.clearFocus()
                viewModel.login()
            },
            enabled = uiModel.value.isEnableLoginButton.value,
            text = "Sign in",
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
                text = "Haven\' had an account?",
                color = colorResource(R.color.colorSystem_normal_text),
                style = CustomTypography.TextRegular
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(
                onClick = onNavigateToSignUp,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Sign up",
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