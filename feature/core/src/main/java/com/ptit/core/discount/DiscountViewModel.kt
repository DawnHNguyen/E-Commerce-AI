package com.ptit.core.discount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.discount.CreateDiscountRequestDomainEntity
import com.ptit.domain.entity.discount.DiscountDomainEntity
import com.ptit.domain.entity.discount.DiscountListDomainEntity
import com.ptit.domain.entity.discount.UpdateDiscountRequestDomainEntity
import com.ptit.domain.repository.DiscountRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscountViewModel @Inject constructor(
    private val discountRepository: DiscountRepository
) : ViewModel() {

    private val _discountsState = MutableStateFlow<Resource<DiscountListDomainEntity>>(Resource.Idle)
    val discountsState: StateFlow<Resource<DiscountListDomainEntity>> = _discountsState.asStateFlow()

    private val _discountDetailState = MutableStateFlow<Resource<DiscountDomainEntity>>(Resource.Idle)
    val discountDetailState: StateFlow<Resource<DiscountDomainEntity>> = _discountDetailState.asStateFlow()

    private val _createDiscountState = MutableStateFlow<Resource<DiscountDomainEntity>>(Resource.Idle)
    val createDiscountState: StateFlow<Resource<DiscountDomainEntity>> = _createDiscountState.asStateFlow()

    private val _updateDiscountState = MutableStateFlow<Resource<DiscountDomainEntity>>(Resource.Idle)
    val updateDiscountState: StateFlow<Resource<DiscountDomainEntity>> = _updateDiscountState.asStateFlow()

    private val _deleteDiscountState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val deleteDiscountState: StateFlow<Resource<Unit>> = _deleteDiscountState.asStateFlow()

    fun getShopDiscounts(
        page: Int = 1,
        limit: Int = 100,
        createdById: String
    ) {
        viewModelScope.launch {
            _discountsState.value = Resource.Loading()
            val result = discountRepository.getShopDiscounts(
                page = page,
                limit = limit,
                createdById = createdById
            )
            _discountsState.value = result
        }
    }

    fun getDiscountDetail(discountId: String) {
        viewModelScope.launch {
            _discountDetailState.value = Resource.Loading()
            val result = discountRepository.getDiscountDetail(discountId)
            _discountDetailState.value = result
        }
    }

    fun createDiscount(request: CreateDiscountRequestDomainEntity) {
        viewModelScope.launch {
            _createDiscountState.value = Resource.Loading()
            val result = discountRepository.createDiscount(request)
            _createDiscountState.value = result
        }
    }

    fun updateDiscount(discountId: String, request: UpdateDiscountRequestDomainEntity) {
        viewModelScope.launch {
            _updateDiscountState.value = Resource.Loading()
            val result = discountRepository.updateDiscount(discountId, request)
            _updateDiscountState.value = result
        }
    }

    fun deleteDiscount(discountId: String) {
        viewModelScope.launch {
            _deleteDiscountState.value = Resource.Loading()
            val result = discountRepository.deleteDiscount(discountId)
            _deleteDiscountState.value = result
        }
    }

    fun resetCreateDiscountState() {
        _createDiscountState.value = Resource.Idle
    }

    fun resetUpdateDiscountState() {
        _updateDiscountState.value = Resource.Idle
    }

    fun resetDeleteDiscountState() {
        _deleteDiscountState.value = Resource.Idle
    }
}

