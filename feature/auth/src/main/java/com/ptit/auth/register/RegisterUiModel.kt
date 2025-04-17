package com.ptit.auth.register

data class RegisterUiModel(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isValidPassword: Boolean = true,
    val isValidEmail: Boolean = true,
    val isValidConfirmPassword: Boolean = true,
    val isShowPassword: Boolean = false,
    val isShowConfirmPassword: Boolean = false,
    val emailErrorType: EmailErrorType = EmailErrorType.NONE,
) {
    val isValid = lazy {
        isValidEmail && isValidPassword && isValidConfirmPassword
    }

    val isEnableRegisterButton = lazy {
        isValid.value
                && email.isNotBlank()
                && password.isNotBlank()
                && confirmPassword.isNotBlank()
    }

    enum class EmailErrorType {
        NONE,
        INVALID_EMAIL,
        EMAIL_ALREADY_EXISTS,
    }
}
