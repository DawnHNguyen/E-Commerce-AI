package com.ptit.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.repository.AuthRepository
import com.ptit.domain.utils.BadRequestException
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.onError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _uiModel = MutableStateFlow(RegisterUiModel())
    val uiModel = _uiModel.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val registerState = _registerState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiModel.update {
            it.copy(
                email = email,
                isValidEmail = true,
                isValidPassword = true,
                isValidConfirmPassword = true,
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiModel.update {
            it.copy(
                password = password,
                isValidEmail = true,
                isValidPassword = true,
                isValidConfirmPassword = true,
            )
        }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiModel.update {
            it.copy(
                confirmPassword = confirmPassword,
                isValidEmail = true,
                isValidPassword = true,
                isValidConfirmPassword = true,
            )
        }
    }

    fun toggleShowPassword() {
        _uiModel.update {
            it.copy(
                isShowPassword = it.isShowPassword.not(),
            )
        }
    }

    fun toggleShowConfirmPassword() {
        _uiModel.update {
            it.copy(
                isShowConfirmPassword = it.isShowConfirmPassword.not(),
            )
        }
    }

    private fun validateEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validatePassword(password: String): Boolean {
        return password.isNotBlank() && password.length >= 6
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        return confirmPassword.isNotBlank() && password == confirmPassword
    }

    fun register() {
        if (registerState.value is Resource.Loading) return
        _registerState.value = Resource.loading()
        val email = uiModel.value.email
        val password = uiModel.value.password
        val confirmPassword = uiModel.value.confirmPassword
        val isValidEmail = validateEmail(email)
        val isValidPassword = validatePassword(password)
        val isValidConfirmPassword = validateConfirmPassword(password, confirmPassword)

        _uiModel.update {
            it.copy(
                isValidEmail = isValidEmail,
                isValidPassword = isValidPassword,
                isValidConfirmPassword = isValidConfirmPassword,
                emailErrorType = if (isValidEmail) RegisterUiModel.EmailErrorType.NONE else RegisterUiModel.EmailErrorType.INVALID_EMAIL,
            )
        }
        if (uiModel.value.isValid.value) {
            viewModelScope.launch(Dispatchers.IO) {
                val response = repository.register(
                    email = email,
                    password = password,
                ).onError {
                    if (it.error?.message?.contains("email") == true) {
                        _uiModel.update {
                            it.copy(
                                isValidEmail = false,
                                emailErrorType = RegisterUiModel.EmailErrorType.EMAIL_ALREADY_EXISTS,
                            )
                        }
                    }
                }

                _registerState.update { response }
            }
        } else {
            _registerState.update {
                Resource.error(
                    BadRequestException(
                        error = null,
                        requestUrl = ""
                    )
                )
            }
        }
    }
}