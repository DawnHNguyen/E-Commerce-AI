package com.ptit.auth.login

data class LoginUiModel(
    val username: String = "",
    val password: String = "",
    val isValidUsername: Boolean = true,
    val isValidPassword: Boolean = true,
    val isShowPassword: Boolean = false,
) {
    val isValid = lazy {
        isValidUsername && isValidPassword
    }

    val isEnableLoginButton = lazy {
        isValid.value && username.isNotBlank()
    }
}