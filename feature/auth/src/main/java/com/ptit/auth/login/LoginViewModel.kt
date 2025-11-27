package com.ptit.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.repository.AuthRepository
import com.ptit.domain.utils.BadRequestException
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import com.ptit.domain.utils.onError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _uiModel = MutableStateFlow(LoginUiModel())
    val uiModel = _uiModel.asStateFlow()

    // LoginState chỉ lưu trạng thái Resource<Unit> (đơn giản cho UI)
    private val _loginState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val loginState = _loginState.asStateFlow()

    fun onUsernameChanged(username: String) {
        _uiModel.update {
            it.copy(
                username = username,
                isValidUsername = true,
                isValidPassword = true,
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiModel.update {
            it.copy(
                password = password,
                isValidUsername = true,
                isValidPassword = true,
            )
        }
    }

    fun toggleShowPassword() {
        _uiModel.update {
            it.copy(
                isShowPassword = !it.isShowPassword,
            )
        }
    }

    private fun validateEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validatePassword(password: String): Boolean {
        return password.isNotBlank() && password.length >= 6
    }

    fun login() {
        // Ngăn double-click login
        if (loginState.value is Resource.Loading) return

        _loginState.value = Resource.loading()

        val username = uiModel.value.username
        val password = uiModel.value.password
        val isValidUsername = validateEmail(username)
        val isValidPassword = validatePassword(password)

        _uiModel.update {
            it.copy(
                isValidUsername = isValidUsername,
                isValidPassword = isValidPassword,
            )
        }

        if (uiModel.value.isValid.value) {
            viewModelScope.launch(Dispatchers.IO) {
                val response = repository
                    .login(
                        email = username,
                        password = password,
                    )
                    // Xử lý lỗi xác thực
                    .onError { exception ->
                        val message = exception.message ?: ""
                        if ("email" in message) {
                            _uiModel.update { it.copy(isValidUsername = false) }
                        } else if ("password" in message) {
                            _uiModel.update { it.copy(isValidPassword = false) }
                        }
                    }
                    // Map từ AuthToken -> Unit cho UI (chỉ cần biết login thành công)
                    .map { Unit }

                // Cập nhật state
                _loginState.update { response }
            }
        } else {
            // Nếu form invalid
            _loginState.update {
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