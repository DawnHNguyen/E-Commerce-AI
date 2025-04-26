package com.ptit.core.account.paymentMethod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.payment.PaymentMethodDomainEntity
import com.ptit.domain.repository.PaymentMethodRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.UnknownException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentMethodsUiState(
    val paymentMethods: List<PaymentMethodDomainEntity> = emptyList(),
)

@HiltViewModel
class PaymentMethodViewModel @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    private val _paymentMethodsState = MutableStateFlow<Resource<List<PaymentMethodDomainEntity>>>(Resource.idle())
    val paymentMethodsState: StateFlow<Resource<List<PaymentMethodDomainEntity>>> = _paymentMethodsState

    private val _deleteState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val deleteState: StateFlow<Resource<Unit>> = _deleteState

    private val _defaultState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val defaultState: StateFlow<Resource<Unit>> = _defaultState

    init {
        loadPaymentMethods()
    }

    private fun loadPaymentMethods() {
        _paymentMethodsState.value = Resource.loading()

        viewModelScope.launch {
            try {
                paymentMethodRepository.getPaymentMethods().collectLatest { paymentMethods ->
                    _paymentMethodsState.value = Resource.Success(paymentMethods)
                    _uiState.value = PaymentMethodsUiState(paymentMethods = paymentMethods)
                }
            } catch (e: Exception) {
                _paymentMethodsState.value = Resource.error(
                    error = UnknownException(
                        message = e.message,
                        requestUrl = "",
                        error = null
                    )
                )
            }
        }
    }

    fun deletePaymentMethod(firstSixNum: String, lastFourNum: String) {
        _deleteState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val result = paymentMethodRepository.deletePaymentMethod(firstSixNum, lastFourNum)
            _deleteState.value = result
        }
    }

    fun setDefaultPaymentMethod(firstSixNum: String, lastFourNum: String) {
        _defaultState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val result = paymentMethodRepository.setDefaultPaymentMethod(firstSixNum, lastFourNum)
            _defaultState.value = result
        }
    }
}
