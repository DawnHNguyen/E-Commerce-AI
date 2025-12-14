package com.ptit.core.brand

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.brand.BrandDomainEntity
import com.ptit.domain.entity.brand.BrandListDomainEntity
import com.ptit.domain.entity.brand.CreateBrandRequestDomainEntity
import com.ptit.domain.entity.brand.UpdateBrandRequestDomainEntity
import com.ptit.domain.repository.BrandRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrandViewModel @Inject constructor(
    private val brandRepository: BrandRepository
) : ViewModel() {

    private val _brandsState = MutableStateFlow<Resource<BrandListDomainEntity>>(Resource.idle())
    val brandsState: StateFlow<Resource<BrandListDomainEntity>> = _brandsState.asStateFlow()

    private val _brandDetailState = MutableStateFlow<Resource<BrandDomainEntity>>(Resource.idle())
    val brandDetailState: StateFlow<Resource<BrandDomainEntity>> = _brandDetailState.asStateFlow()

    private val _createBrandState = MutableStateFlow<Resource<BrandDomainEntity>>(Resource.idle())
    val createBrandState: StateFlow<Resource<BrandDomainEntity>> = _createBrandState.asStateFlow()

    private val _updateBrandState = MutableStateFlow<Resource<BrandDomainEntity>>(Resource.idle())
    val updateBrandState: StateFlow<Resource<BrandDomainEntity>> = _updateBrandState.asStateFlow()

    private val _deleteBrandState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val deleteBrandState: StateFlow<Resource<Unit>> = _deleteBrandState.asStateFlow()

    /**
     * Fetch list of brands with pagination
     */
    fun getBrands(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch(Dispatchers.IO) {
            _brandsState.value = Resource.loading()
            val result = brandRepository.getBrands(page, limit)
            _brandsState.value = result
        }
    }

    /**
     * Fetch brand detail by ID
     */
    fun getBrandDetail(brandId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _brandDetailState.value = Resource.loading()
            val result = brandRepository.getBrandDetail(brandId)
            _brandDetailState.value = result
        }
    }

    /**
     * Create new brand
     */
    fun createBrand(request: CreateBrandRequestDomainEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            _createBrandState.value = Resource.loading()
            val result = brandRepository.createBrand(request)
            _createBrandState.value = result

            // Refresh brands list after successful creation
            if (result is Resource.Success) {
                getBrands()
            }
        }
    }

    /**
     * Update existing brand
     */
    fun updateBrand(brandId: String, request: UpdateBrandRequestDomainEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            _updateBrandState.value = Resource.loading()
            val result = brandRepository.updateBrand(brandId, request)
            _updateBrandState.value = result

            // Refresh brands list after successful update
            if (result is Resource.Success) {
                getBrands()
            }
        }
    }

    /**
     * Delete brand
     */
    fun deleteBrand(brandId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _deleteBrandState.value = Resource.loading()
            val result = brandRepository.deleteBrand(brandId)
            _deleteBrandState.value = result

            // Refresh brands list after successful deletion
            if (result is Resource.Success) {
                getBrands()
            }
        }
    }

    /**
     * Reset states
     */
    fun resetCreateBrandState() {
        _createBrandState.value = Resource.idle()
    }

    fun resetUpdateBrandState() {
        _updateBrandState.value = Resource.idle()
    }

    fun resetDeleteBrandState() {
        _deleteBrandState.value = Resource.idle()
    }
}

