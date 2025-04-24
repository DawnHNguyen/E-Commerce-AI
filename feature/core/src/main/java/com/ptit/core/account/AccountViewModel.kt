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

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiModel = MutableStateFlow(AccountUiModel())
    val uiModel = _uiModel.asStateFlow()

    private val _userProfileState = MutableStateFlow<Resource<UserDomainEntity>>(Resource.idle())
    val userProfileState = _userProfileState.asStateFlow()
    
    private val _logoutState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val logoutState = _logoutState.asStateFlow()

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
}
