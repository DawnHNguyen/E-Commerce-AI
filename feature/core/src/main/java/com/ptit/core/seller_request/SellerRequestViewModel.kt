package com.ptit.core.seller_request

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.seller_request.SellerRequestDomainEntity
import com.ptit.domain.repository.FileUploadRepository
import com.ptit.domain.repository.SellerRequestRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SellerRequestViewModel"

@HiltViewModel
class SellerRequestViewModel @Inject constructor(
    private val sellerRequestRepository: SellerRequestRepository,
    private val fileUploadRepository: FileUploadRepository
) : ViewModel() {

    // Create Seller Request state
    private val _createRequestState = MutableStateFlow<Resource<SellerRequestDomainEntity>>(Resource.idle())
    val createRequestState = _createRequestState.asStateFlow()

    // Get My Request state
    private val _myRequestState = MutableStateFlow<Resource<SellerRequestDomainEntity?>>(Resource.idle())
    val myRequestState = _myRequestState.asStateFlow()

    // Upload image state
    private val _uploadImageState = MutableStateFlow<Resource<String>>(Resource.idle())
    val uploadImageState = _uploadImageState.asStateFlow()

    init {
        fetchMySellerRequest()
    }

    // Load user's current seller request
    fun fetchMySellerRequest() {
        if (_myRequestState.value is Resource.Loading) return

        viewModelScope.launch(Dispatchers.IO) {
            _myRequestState.value = Resource.loading()
            val result = sellerRequestRepository.getMySellerRequest()

            // Handle result with fallback for server errors
            _myRequestState.value = when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "Fetch my request success: ${result.data}")
                    result
                }
                is Resource.Error -> {
                    val errorMessage = result.error.message ?: ""
                    Log.e(TAG, "Fetch my request error: $errorMessage", result.error)

                    // Fallback: If it's a 500 server error, treat as "no request yet"
                    // This allows user to continue using the app while backend fixes the issue
                    if (errorMessage.contains("500") || errorMessage.contains("Internal Server Error")) {
                        Log.w(TAG, "Server error detected. Treating as 'no request' to allow user to continue.")
                        Resource.success(null)
                    } else {
                        result
                    }
                }
                else -> result
            }
        }
    }
    fun createSellerRequest(
        shopName: String,
        shopDescription: String?,
        businessLicense: String?,
        taxCode: String?
    ) {
        if (_createRequestState.value is Resource.Loading) return

        viewModelScope.launch(Dispatchers.IO) {
            _createRequestState.value = Resource.loading()
            val result = sellerRequestRepository.createSellerRequest(
                shopName = shopName,
                shopDescription = shopDescription,
                businessLicense = businessLicense,
                taxCode = taxCode
            )
            _createRequestState.value = result

            // Refresh my request after creating
            if (result is Resource.Success) {
                fetchMySellerRequest()
            }
        }
    }

    // Upload shop image/document
    fun uploadImage(uri: Uri) {
        if (_uploadImageState.value is Resource.Loading) return

        viewModelScope.launch(Dispatchers.IO) {
            _uploadImageState.value = Resource.loading()
            val result = fileUploadRepository.uploadSingleFile(uri)
            _uploadImageState.value = result
        }
    }

    // Reset states
    fun resetCreateRequestState() {
        _createRequestState.value = Resource.idle()
    }

    fun resetUploadImageState() {
        _uploadImageState.value = Resource.idle()
    }
}

