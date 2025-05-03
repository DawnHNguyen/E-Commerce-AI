package com.ptit.core.shop

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.shop.ShopDomainEntity
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
    private val shopRepository: ShopRepository
) : ViewModel() {

    // UI state
    private val _uiModel = MutableStateFlow(ShopUiModel())
    val uiModel = _uiModel.asStateFlow()

    // API result states
    private val _shopDetailsState = MutableStateFlow<Resource<ShopDomainEntity>>(Resource.idle())
    val shopDetailsState= _shopDetailsState.asStateFlow()

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


}
