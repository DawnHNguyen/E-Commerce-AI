package com.ptit.core.account

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.repository.AuthRepository
import com.ptit.domain.repository.UserRepository
import com.ptit.domain.usecase.UpdateProfileUseCase
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountUiModel(
    val user: UserDomainEntity = UserDomainEntity(),
    val isSettingsExpanded: Boolean = false,
)

data class UpdateProfileUiModel(
    val original: UserDomainEntity = UserDomainEntity(),
    val name: String = original.name,
    val phone: String = original.phone,
    val avatar: Uri = original.avatar.toUri(),
    val address: String = original.address,
) {
    val isChangedName get() = lazy { name != original.name }

    val isChangedPhone get() = lazy { phone != original.phone }
    val isChangedAvatar get() = lazy { avatar != original.avatar.toUri() }
    val isChangedAddress get() = lazy { address != original.address }
    val isChanged by lazy {
        isChangedName.value ||
                isChangedPhone.value ||
                isChangedAvatar.value ||
                isChangedAddress.value
    }
    val isValid
        get() = lazy {
            name.isNotBlank()
        }
}

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val updateProfileUseCase: UpdateProfileUseCase,
) : ViewModel() {

    private val _uiModel = MutableStateFlow(AccountUiModel())
    val uiModel = _uiModel.asStateFlow()

    private val _updateProfileUiModel = MutableStateFlow(UpdateProfileUiModel())
    val updateProfileUiModel = _updateProfileUiModel.asStateFlow()

    private val _userProfileState = MutableStateFlow<Resource<UserDomainEntity>>(Resource.idle())
    val userProfileState = _userProfileState.asStateFlow()

    private val _logoutState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val logoutState = _logoutState.asStateFlow()

    private val _updateProfileState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val updateProfileState = _updateProfileState.asStateFlow()

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        if (userProfileState.value is Resource.Loading) return
        _userProfileState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val response = userRepository.getUserProfile()
            _userProfileState.value = response

            if (response is Resource.Success) {
                _uiModel.update {
                    it.copy(user = response.data)
                }
            }
        }
    }

    fun toggleSettingsExpanded() {
        _uiModel.update {
            it.copy(isSettingsExpanded = !it.isSettingsExpanded)
        }
    }

    fun logout() {
        if (logoutState.value is Resource.Loading) return
        _logoutState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val response = authRepository.logout()
            _logoutState.value = response
        }
    }

    fun onEditProfile() {
        _updateProfileState.value = Resource.idle()
        _updateProfileUiModel.value = UpdateProfileUiModel(
            original = uiModel.value.user
        )
    }

    fun onNameChanged(name: String) {
        _updateProfileUiModel.update {
            it.copy(name = name)
        }
    }

    fun onPhoneChanged(phone: String) {
        _updateProfileUiModel.update {
            it.copy(phone = phone)
        }
    }

    fun onAvatarChanged(avatar: Uri) {
        _updateProfileUiModel.update {
            it.copy(avatar = avatar)
        }
    }

    fun onAddressChanged(address: String) {
        _updateProfileUiModel.update {
            it.copy(address = address)
        }
    }

    fun updateProfile() {
        if (updateProfileState.value is Resource.Loading) return
        _updateProfileState.value = Resource.loading()
        val uiModel = updateProfileUiModel.value

        val name = if (uiModel.isChangedName.value) {
            uiModel.name
        } else {
            null
        }

        val phone = if (uiModel.isChangedPhone.value) {
            uiModel.phone
        } else {
            null
        }

        val avatar = if (uiModel.isChangedAvatar.value) {
            uiModel.avatar
        } else {
            null
        }

        val address = if (uiModel.isChangedAddress.value) {
            uiModel.address
        } else {
            null
        }

        viewModelScope.launch(Dispatchers.IO) {
            _updateProfileState.update {
                updateProfileUseCase(
                    name = name,
                    phone = phone,
                    avatar = avatar,
                    address = address,
                )
                    .onSuccess {
                        fetchUserProfile()
                    }
            }
        }
    }
}
