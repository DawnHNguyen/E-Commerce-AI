package com.ptit.auth.login

data class LoginUiModel(
    val username: String = "",
    val password: String = "",
    val isValidUsername: Boolean = true,
    val isShowPassword: Boolean = false,
) {
    val isValidPassword: Boolean = password.isNotBlank()

    val isValid = lazy {
        isValidUsername
    }

    val isEnableLoginButton = lazy {
        isValidPassword
                && isValid.value
                && username.isNotBlank()
    }
}