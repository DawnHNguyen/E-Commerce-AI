package com.ptit.core.shop

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.seller_request.SellerRequestDomainEntity
import com.ptit.domain.entity.shop.ShopDomainEntity
import com.ptit.domain.repository.SellerRequestRepository
import com.ptit.domain.repository.ShopRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ShopEntryViewModel"

/**
 * ViewModel to handle the entry logic for "Tạo cửa hàng" screen
 *
 * Logic flow:
 * 1. Check if user has a shop → Navigate to ShopDetailScreen
 * 2. If no shop, check if user has a seller request → Show request status
 * 3. If no request, show CreateSellerRequestScreen
 */
@HiltViewModel
class ShopEntryViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val sellerRequestRepository: SellerRequestRepository
) : ViewModel() {

    private val _shopState = MutableStateFlow<Resource<ShopDomainEntity>>(Resource.idle())
    val shopState = _shopState.asStateFlow()

    private val _sellerRequestState = MutableStateFlow<Resource<SellerRequestDomainEntity?>>(Resource.idle())
    val sellerRequestState = _sellerRequestState.asStateFlow()

    sealed class NavigationDestination {
        data class ShopDetail(val shopId: String) : NavigationDestination()
        data class RequestStatus(val request: SellerRequestDomainEntity) : NavigationDestination()
        object CreateRequest : NavigationDestination()
    }

    private val _navigationDestination = MutableStateFlow<NavigationDestination?>(null)
    val navigationDestination = _navigationDestination.asStateFlow()

    init {
        checkShopAndRequestStatus()
    }

    /**
     * Step 1: Check shop status
     * Step 2: If no shop, check seller request status
     * Step 3: Determine navigation destination
     */
    fun checkShopAndRequestStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            // Step 1: Check if user has a shop
            _shopState.value = Resource.loading()
            val shopResult = shopRepository.getMyShop()
            _shopState.value = shopResult

            when (shopResult) {
                is Resource.Success -> {
                    // User has a shop → Navigate to ShopDetailScreen
                    Log.d(TAG, "User has shop: ${shopResult.data.id}")
                    _navigationDestination.value = NavigationDestination.ShopDetail(shopResult.data.id)
                }
                is Resource.Error -> {
                    // User doesn't have shop → Check seller request
                    Log.d(TAG, "User doesn't have shop. Checking seller request...")
                    checkSellerRequest()
                }
                else -> {}
            }
        }
    }

    /**
     * Step 2: Check seller request status
     */
    private suspend fun checkSellerRequest() {
        _sellerRequestState.value = Resource.loading()
        val requestResult = sellerRequestRepository.getMySellerRequest()

        // Handle 500 server error gracefully
        val finalResult = when (requestResult) {
            is Resource.Error -> {
                val errorMessage = requestResult.error.message ?: ""
                if (errorMessage.contains("500") || errorMessage.contains("Internal Server Error")) {
                    Log.w(TAG, "Server error when fetching request. Treating as 'no request'.")
                    Resource.success(null)
                } else {
                    requestResult
                }
            }
            else -> requestResult
        }

        _sellerRequestState.value = finalResult

        when (finalResult) {
            is Resource.Success -> {
                val request = finalResult.data
                if (request != null) {
                    // User has a seller request → Show request status
                    Log.d(TAG, "User has seller request with status: ${request.status}")
                    _navigationDestination.value = NavigationDestination.RequestStatus(request)
                } else {
                    // User doesn't have request → Show CreateSellerRequestScreen
                    Log.d(TAG, "User doesn't have seller request. Show create form.")
                    _navigationDestination.value = NavigationDestination.CreateRequest
                }
            }
            is Resource.Error -> {
                // Error fetching request → Default to create form
                Log.e(TAG, "Error fetching seller request: ${finalResult.error.message}")
                _navigationDestination.value = NavigationDestination.CreateRequest
            }
            else -> {}
        }
    }

    fun resetNavigation() {
        _navigationDestination.value = null
    }

    fun refresh() {
        _shopState.value = Resource.idle()
        _sellerRequestState.value = Resource.idle()
        _navigationDestination.value = null
        checkShopAndRequestStatus()
    }
}

