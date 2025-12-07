package com.ptit.core.shop

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.shop.ShopDomainEntity
import com.ptit.domain.repository.FileUploadRepository
import com.ptit.domain.repository.ShopRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShopUiModel(
    val shop: ShopDomainEntity = ShopDomainEntity(),
)

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val fileUploadRepository: FileUploadRepository
) : ViewModel() {

    // UI state
    private val _uiModel = MutableStateFlow(ShopUiModel())
    val uiModel = _uiModel.asStateFlow()

    // API result states
    private val _shopDetailsState = MutableStateFlow<Resource<ShopDomainEntity>>(Resource.idle())
    val shopDetailsState = _shopDetailsState.asStateFlow()

    private val _createShopState = MutableStateFlow<Resource<ShopDomainEntity>>(Resource.idle())
    val createShopState = _createShopState.asStateFlow()

    private val _updateShopState = MutableStateFlow<Resource<ShopDomainEntity>>(Resource.idle())
    val updateShopState = _updateShopState.asStateFlow()

    private val _uploadImageState = MutableStateFlow<Resource<String>>(Resource.idle())
    val uploadImageState = _uploadImageState.asStateFlow()

    init {
        fetchMyShopDetails()
    }

    // Load existing shop details
    fun fetchMyShopDetails() {
        if (shopDetailsState.value is Resource.Loading) return
        _shopDetailsState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val response = shopRepository.getMyShop()
            Log.d("ShopViewModel", "Shop API Response: $response")
            _shopDetailsState.value = response

            if (response is Resource.Success) {
                _uiModel.value = ShopUiModel(shop = response.data)
            }
        }
    }

    // Create new shop
    fun createShop(
        name: String,
        description: String?,
        address: String?,
        phone: String?,
        avatar: String?
    ) {
        if (_createShopState.value is Resource.Loading) return
        _createShopState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val response = shopRepository.createShop(
                name = name,
                description = description,
                address = address,
                phone = phone,
                avatar = avatar
            )
            Log.d("ShopViewModel", "Shop create response: $response")
            _createShopState.value = response

            if (response is Resource.Success) {
                _uiModel.value = ShopUiModel(shop = response.data)
                fetchMyShopDetails()
            }
        }
    }

    // Update shop details
    fun updateShopDetails(
        name: String?,
        description: String?,
        address: String?,
        phone: String?,
        avatar: String?
    ) {
        if (_updateShopState.value is Resource.Loading) return
        _updateShopState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val response = shopRepository.updateShop(
                name = name,
                description = description,
                address = address,
                phone = phone,
                avatar = avatar
            )
            Log.d("ShopViewModel", "Shop update response: $response")
            _updateShopState.value = response

            if (response is Resource.Success) {
                _uiModel.value = ShopUiModel(shop = response.data)
                fetchMyShopDetails()
            }
        }
    }

    // Upload shop avatar image
    fun uploadShopImage(imageUri: Uri) {
        if (_uploadImageState.value is Resource.Loading) return
        _uploadImageState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val response = fileUploadRepository.uploadSingleFile(imageUri)
            Log.d("ShopViewModel", "Image upload response: $response")
            _uploadImageState.value = response
        }
    }

    @Deprecated(
        message = "This method is deprecated along with createShop. Use SellerRequestViewModel instead.",
        level = DeprecationLevel.WARNING
    )
    fun resetCreateShopState() {
        _updateShopState.value = Resource.idle()
    }

    fun resetUploadImageState() {
        _uploadImageState.value = Resource.idle()
    }
}