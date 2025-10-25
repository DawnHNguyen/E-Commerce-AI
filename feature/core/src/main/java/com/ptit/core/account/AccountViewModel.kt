package com.ptit.core.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.repository.AuthRepository
import com.ptit.domain.repository.UserRepository
import com.ptit.domain.utils.Resource
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
    val name: String = "",
    val phoneNumber: String = "",
    val avatar: String = "",
) {
    val isChangedName by lazy { name != original.name }
    val isChangedPhoneNumber by lazy { phoneNumber != original.phoneNumber }
    val isChangedAvatar by lazy { avatar != original.avatar }

    val isChanged by lazy {
        isChangedName ||
                isChangedPhoneNumber ||
                isChangedAvatar
    }

    val isValid by lazy {
        name.isNotBlank() && phoneNumber.isNotBlank()
    }

    companion object {
        fun fromUser(user: UserDomainEntity): UpdateProfileUiModel {
            return UpdateProfileUiModel(
                original = user,
                name = user.name,
                phoneNumber = user.phoneNumber,
                avatar = user.avatar
            )
        }
    }
}

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
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
        _updateProfileUiModel.value = UpdateProfileUiModel.fromUser(uiModel.value.user)
    }

    fun onNameChanged(name: String) {
        _updateProfileUiModel.update {
            it.copy(name = name)
        }
    }

    fun onPhoneNumberChanged(phoneNumber: String) {
        _updateProfileUiModel.update {
            it.copy(phoneNumber = phoneNumber)
        }
    }

    fun onAvatarChanged(avatar: String) {
        _updateProfileUiModel.update {
            it.copy(avatar = avatar)
        }
    }

    fun updateProfile() {
        if (updateProfileState.value is Resource.Loading) return
        _updateProfileState.value = Resource.loading()
        val uiModel = updateProfileUiModel.value

        viewModelScope.launch(Dispatchers.IO) {
            val result = userRepository.updateUserProfile(
                name = if (uiModel.isChangedName) uiModel.name else null,
                phoneNumber = if (uiModel.isChangedPhoneNumber) uiModel.phoneNumber else null,
                avatar = if (uiModel.isChangedAvatar) uiModel.avatar else null,
            )

            _updateProfileState.value = result

            if (result is Resource.Success) {
                fetchUserProfile()
            }
        }
    }
}