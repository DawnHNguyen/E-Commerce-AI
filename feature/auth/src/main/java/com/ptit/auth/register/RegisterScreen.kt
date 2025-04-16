package com.ptit.auth.register

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.ptit.common.presentation.MaxSizeBox
import com.ptit.common.presentation.MaxWidthColumn
import com.ptit.common.presentation.component.BaseTextField
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
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

    val viewModel = hiltViewModel<RegisterViewModel>()
    // Collect UI state
    val uiState = viewModel.uiModel.collectAsStateWithLifecycle()
    val isShowProgressBar = rememberState { false }

    // Define colors from resources
    val primaryColor = colorResource(id = R.color.colorSystem_heading_button)
    val textFieldColor = colorResource(id = R.color.colorSystem_text_field)
    val errorColor = Color.Red

    // Handle registration success
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.registerState) {
            it
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onError {
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Fail to register",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onSuccess {
                    onRegisterSuccess()
                }
        }
    }

    MaxSizeBox(
        modifier = Modifier
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        MaxWidthColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Sign up",
                color = primaryColor,
                style = CustomTypography.TextBold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 64.dp)
            )

            // Email field
            Column {
                Text(
                    text = "Email",
                    color = primaryColor,
                    style = CustomTypography.TextMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BaseTextField(
                    value = uiState.value.email,
                    onValueChange = viewModel::onEmailChanged,
                    modifier = Modifier.fillMaxWidth(),
                    hint = "Enter your email",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    isError = !uiState.value.isValidEmail
                )

                if (!uiState.value.isValidEmail) {
                    Text(
                        text = "Please enter a valid email address",
                        color = errorColor,
                        style = CustomTypography.TextRegular.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            // Password field
            Column {
                Text(
                    text = "Password",
                    color = primaryColor,
                    style = CustomTypography.TextMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BaseTextField(
                    value = uiState.value.password,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    hint = "Enter your password",
                    visualTransformation = if (uiState.value.isShowPassword) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    isError = !uiState.value.isValidPassword,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleShowPassword() }) {
                            Icon(
                                imageVector = if (uiState.value.isShowPassword) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (uiState.value.isShowPassword) "Hide password"
                                else "Show password"
                            )
                        }
                    }
                )

                if (!uiState.value.isValidPassword) {
                    Text(
                        text = "Password must be at least 6 characters",
                        color = errorColor,
                        style = CustomTypography.TextRegular.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            // Confirm password field
            Column {
                Text(
                    text = "Confirm password",
                    color = primaryColor,
                    style = CustomTypography.TextMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BaseTextField(
                    value = uiState.value.confirmPassword,
                    onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    hint = "Confirm your password",
                    visualTransformation = if (uiState.value.isShowConfirmPassword) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    isError = !uiState.value.isValidConfirmPassword,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleShowConfirmPassword() }) {
                            Icon(
                                imageVector = if (uiState.value.isShowConfirmPassword) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (uiState.value.isShowConfirmPassword) "Hide password"
                                else "Show password"
                            )
                        }
                    }
                )

                if (!uiState.value.isValidConfirmPassword) {
                    Text(
                        text = "Passwords don't match",
                        color = errorColor,
                        style = CustomTypography.TextRegular.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign up button
            Button(
                onClick = { viewModel.register() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    disabledContainerColor = primaryColor.copy(alpha = 0.5f)
                ),
                enabled = uiState.value.isEnableRegisterButton.value
            ) {
                Text(
                    text = "Sign up",
                    color = Color.White,
                    style = CustomTypography.Button
                )
            }

            // Or divider
            Text(
                text = "Or",
                color = Color.Gray,
                style = CustomTypography.TextRegular,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Sign in row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Have an account?",
                    color = Color.Gray,
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

        // Show loading indicator when registering
        if (isShowProgressBar.value) {
            FullScreenProgressBar()
        }
    }
}