package com.ptit.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.repository.AuthRepository
import com.ptit.domain.utils.BadRequestException
import com.ptit.domain.utils.Resource
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

    private val _loginState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val loginState = _loginState.asStateFlow()

    fun onUsernameChanged(username: String) {
        _uiModel.update {
            it.copy(
                username = username,
                isValidUsername = true,
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiModel.update {
            it.copy(
                password = password,
                isValidUsername = true,
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

    private fun validateEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun login() {
        if (loginState.value is Resource.Loading) return
        _loginState.value = Resource.loading()
        val username = uiModel.value.username
        val password = uiModel.value.password
        val isValidUsername = validateEmail(username)

        _uiModel.update {
            it.copy(
                isValidUsername = isValidUsername,
            )
        }
        if (uiModel.value.isValid.value) {
            viewModelScope.launch(Dispatchers.IO) {
                _loginState.update {
                    repository.login(
                        email = username,
                        password = password,
                    )
                }
            }
        } else {
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