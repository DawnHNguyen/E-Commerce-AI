package com.ptit.core.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.repository.AuthRepository
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.repository.UserRepository
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
    // ✅ NEW: Order statistics
    val totalOrders: Int = 0,
    val totalSpent: Int = 0,
    val isLoadingStats: Boolean = false
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
        // Note: avatar can be empty string, backend will accept it
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

data class ChangePasswordUiModel(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
) {
    val isValid by lazy {
        currentPassword.isNotBlank() &&
                newPassword.isNotBlank() &&
                newPassword.length >= 6 &&
                confirmPassword.isNotBlank() &&
                newPassword == confirmPassword
    }
}

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val addressRepository: com.ptit.domain.repository.AddressRepository,
    private val shippingRepository: com.ptit.domain.repository.ShippingRepository,
    private val fileUploadRepository: com.ptit.domain.repository.FileUploadRepository
) : ViewModel() {

    private val _uiModel = MutableStateFlow(AccountUiModel())
    val uiModel = _uiModel.asStateFlow()

    private val _updateProfileUiModel = MutableStateFlow(UpdateProfileUiModel())
    val updateProfileUiModel = _updateProfileUiModel.asStateFlow()

    private val _changePasswordUiModel = MutableStateFlow(ChangePasswordUiModel())
    val changePasswordUiModel = _changePasswordUiModel.asStateFlow()

    private val _userProfileState = MutableStateFlow<Resource<UserDomainEntity>>(Resource.idle())
    val userProfileState = _userProfileState.asStateFlow()

    private val _logoutState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val logoutState = _logoutState.asStateFlow()

    private val _updateProfileState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val updateProfileState = _updateProfileState.asStateFlow()

    private val _changePasswordState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val changePasswordState = _changePasswordState.asStateFlow()

    private val _uploadAvatarState = MutableStateFlow<Resource<String>>(Resource.idle())
    val uploadAvatarState = _uploadAvatarState.asStateFlow()

    // Address states
    private val _addressesState = MutableStateFlow<Resource<List<com.ptit.domain.entity.address.AddressDomainEntity>>>(Resource.idle())
    val addressesState = _addressesState.asStateFlow()

    private val _createAddressState = MutableStateFlow<Resource<com.ptit.domain.entity.address.AddressDomainEntity>>(Resource.idle())
    val createAddressState = _createAddressState.asStateFlow()

    // Shipping states for address selection
    private val _addressShippingState = MutableStateFlow(AddressShippingState())
    val addressShippingState = _addressShippingState.asStateFlow()

    init {
        fetchUserProfile()
        loadOrderStatistics()
        loadProvinces() // Load provinces when ViewModel is created
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

    // ✅ NEW: Load order statistics
    fun loadOrderStatistics() {
        _uiModel.update { it.copy(isLoadingStats = true) }

        viewModelScope.launch(Dispatchers.IO) {
            when (val result = orderRepository.getOrders(
                status = null,
                page = 1,
                limit = 1000 // Get all orders for total count
            )) {
                is Resource.Success -> {
                    val orders = result.data.data  // ✅ Fixed: Use .data instead of .orders

                    // Only count orders that are not PENDING, CANCELLED, or RETURNED
                    val excludedStatuses = setOf("PENDING", "PENDING_PAYMENT", "CANCELLED", "RETURNED")
                    val validOrders = orders.filter { order ->
                        !excludedStatuses.contains(order.status.uppercase())
                    }

                    val totalOrders = validOrders.size
                    val totalSpent = validOrders.sumOf { it.totalPayment }

                    _uiModel.update {
                        it.copy(
                            totalOrders = totalOrders,
                            totalSpent = totalSpent,
                            isLoadingStats = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiModel.update { it.copy(isLoadingStats = false) }
                }
                else -> {
                    _uiModel.update { it.copy(isLoadingStats = false) }
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

    fun uploadAvatar(uri: android.net.Uri) {
        if (_uploadAvatarState.value is Resource.Loading) return

        viewModelScope.launch(Dispatchers.IO) {
            _uploadAvatarState.value = Resource.loading()
            val result = fileUploadRepository.uploadSingleFile(uri)
            _uploadAvatarState.value = result

            // Nếu upload thành công, tự động update avatar URL
            result.onSuccess { url ->
                _updateProfileUiModel.update {
                    it.copy(avatar = url)
                }
            }
        }
    }

    fun resetUploadAvatarState() {
        _uploadAvatarState.value = Resource.idle()
    }

    fun updateProfile() {
        if (updateProfileState.value is Resource.Loading) return
        _updateProfileState.value = Resource.loading()
        val uiModel = updateProfileUiModel.value

        viewModelScope.launch(Dispatchers.IO) {
            // ✅ Always send all fields (backend requires all three)
            val result = userRepository.updateUserProfile(
                name = uiModel.name,
                phoneNumber = uiModel.phoneNumber,
                avatar = uiModel.avatar,
            )

            _updateProfileState.value = result

            if (result is Resource.Success) {
                fetchUserProfile()
            }
        }
    }

    // Change Password methods
    fun onChangePassword() {
        _changePasswordState.value = Resource.idle()
        _changePasswordUiModel.value = ChangePasswordUiModel()
    }

    fun onCurrentPasswordChanged(password: String) {
        _changePasswordUiModel.update {
            it.copy(currentPassword = password)
        }
    }

    fun onNewPasswordChanged(password: String) {
        _changePasswordUiModel.update {
            it.copy(newPassword = password)
        }
    }

    fun onConfirmPasswordChanged(password: String) {
        _changePasswordUiModel.update {
            it.copy(confirmPassword = password)
        }
    }

    fun changePassword() {
        if (changePasswordState.value is Resource.Loading) return
        val uiModel = changePasswordUiModel.value

        if (!uiModel.isValid) return

        _changePasswordState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val result = userRepository.changePassword(
                currentPassword = uiModel.currentPassword,
                newPassword = uiModel.newPassword,
                confirmNewPassword = uiModel.confirmPassword
            )

            _changePasswordState.value = result

            if (result is Resource.Success) {
                // Reset form after success
                _changePasswordUiModel.value = ChangePasswordUiModel()
            }
        }
    }

    // Address methods
    fun fetchAddresses() {
        if (addressesState.value is Resource.Loading) return
        _addressesState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val result = addressRepository.getAddresses()
            _addressesState.value = result
        }
    }

    fun createAddress(
        name: String,
        recipient: String?,
        phoneNumber: String?,
        provinceId: Int,
        districtId: Int,
        wardCode: String,
        street: String,
        addressType: String,
        isDefault: Boolean
    ) {
        if (createAddressState.value is Resource.Loading) return
        _createAddressState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val result = addressRepository.createAddress(
                name = name,
                recipient = recipient,
                phoneNumber = phoneNumber,
                provinceId = provinceId,
                districtId = districtId,
                wardCode = wardCode,
                street = street,
                addressType = addressType,
                isDefault = isDefault
            )

            _createAddressState.value = result

            if (result is Resource.Success) {
                // Refresh addresses list after creating new address
                fetchAddresses()
            }
        }
    }

    fun resetCreateAddressState() {
        _createAddressState.value = Resource.idle()
    }

    // Shipping address selection methods
    private fun loadProvinces() {
        viewModelScope.launch(Dispatchers.IO) {
            _addressShippingState.update { it.copy(provincesLoading = true) }
            when (val result = shippingRepository.getProvinces()) {
                is Resource.Success -> _addressShippingState.update {
                    it.copy(provinces = result.data, provincesLoading = false)
                }
                is Resource.Error -> {
                    _addressShippingState.update { it.copy(provincesLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun selectProvinceForAddress(province: com.ptit.domain.entity.shipping.ProvinceEntity?) {
        if (province == null) {
            _addressShippingState.update {
                it.copy(
                    selectedProvince = null,
                    selectedDistrict = null,
                    selectedWard = null,
                    districts = emptyList(),
                    wards = emptyList()
                )
            }
            return
        }
        if (province.id == _addressShippingState.value.selectedProvince?.id) return

        _addressShippingState.update {
            it.copy(
                selectedProvince = province,
                selectedDistrict = null,
                selectedWard = null,
                districts = emptyList(),
                wards = emptyList()
            )
        }
        loadDistrictsForAddress(province.id)
    }

    private fun loadDistrictsForAddress(provinceId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _addressShippingState.update { it.copy(districtsLoading = true) }
            when (val result = shippingRepository.getDistricts(provinceId)) {
                is Resource.Success -> _addressShippingState.update {
                    it.copy(districts = result.data, districtsLoading = false)
                }
                is Resource.Error -> {
                    _addressShippingState.update { it.copy(districtsLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun selectDistrictForAddress(district: com.ptit.domain.entity.shipping.DistrictEntity?) {
        if (district == null) {
            _addressShippingState.update {
                it.copy(selectedDistrict = null, selectedWard = null, wards = emptyList())
            }
            return
        }
        if (district.id == _addressShippingState.value.selectedDistrict?.id) return

        _addressShippingState.update {
            it.copy(
                selectedDistrict = district,
                selectedWard = null,
                wards = emptyList()
            )
        }
        loadWardsForAddress(district.id)
    }

    private fun loadWardsForAddress(districtId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _addressShippingState.update { it.copy(wardsLoading = true) }
            when (val result = shippingRepository.getWards(districtId)) {
                is Resource.Success -> _addressShippingState.update {
                    it.copy(wards = result.data, wardsLoading = false)
                }
                is Resource.Error -> {
                    _addressShippingState.update { it.copy(wardsLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun selectWardForAddress(ward: com.ptit.domain.entity.shipping.WardEntity?) {
        _addressShippingState.update { it.copy(selectedWard = ward) }
    }

    fun resetAddressShippingState() {
        _addressShippingState.update {
            AddressShippingState(provinces = it.provinces) // Keep provinces loaded
        }
    }

    data class AddressShippingState(
        val provinces: List<com.ptit.domain.entity.shipping.ProvinceEntity> = emptyList(),
        val districts: List<com.ptit.domain.entity.shipping.DistrictEntity> = emptyList(),
        val wards: List<com.ptit.domain.entity.shipping.WardEntity> = emptyList(),
        val selectedProvince: com.ptit.domain.entity.shipping.ProvinceEntity? = null,
        val selectedDistrict: com.ptit.domain.entity.shipping.DistrictEntity? = null,
        val selectedWard: com.ptit.domain.entity.shipping.WardEntity? = null,
        val provincesLoading: Boolean = false,
        val districtsLoading: Boolean = false,
        val wardsLoading: Boolean = false
    )
}