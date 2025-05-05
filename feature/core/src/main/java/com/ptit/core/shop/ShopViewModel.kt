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
            Log.e("ShopViewModel", "Shop API Response: $response")
            _shopDetailsState.value = response

            if (response is Resource.Success) {
                _uiModel.value = ShopUiModel(shop = response.data)
            }
        }
    }

    // Update shop details
    fun updateShopDetails(updatedShop: ShopDomainEntity) {
        if (_updateShopState.value is Resource.Loading) return
        _updateShopState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val response = shopRepository.updateShop(updatedShop.name, updatedShop.description, updatedShop.address, updatedShop.phone, updatedShop.avatar)
            Log.e("ShopViewModel", "Shop update response: $response")
            _updateShopState.value = response

            if (response is Resource.Success) {
                _uiModel.value = ShopUiModel(shop = response.data)
            }
        }
    }

    // Upload shop avatar image
    fun uploadShopImage(imageUri: Uri) {
        if (_uploadImageState.value is Resource.Loading) return
        _uploadImageState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val response = fileUploadRepository.uploadSingleFile(imageUri)
            Log.e("ShopViewModel", "Image upload response: $response")
            _uploadImageState.value = response
        }
    }
}