package com.ptit.core.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.payment.PaymentMethodDomainEntity
import com.ptit.domain.repository.PaymentMethodRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseBottomSheetViewModel @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseBottomSheetUiState())
    val uiState: StateFlow<PurchaseBottomSheetUiState> = _uiState.asStateFlow()

    val paymentMethodsState: StateFlow<Resource<List<PaymentMethodDomainEntity>>> = paymentMethodRepository
        .getPaymentMethods()
        .map {
            Resource.Success(it)
        }
        .stateIn(
            viewModelScope,
            initialValue = Resource.idle(),
            started = SharingStarted.WhileSubscribed(5_000)
        )

    init {
        loadDefaultPaymentMethod()
    }

    private fun loadDefaultPaymentMethod() {
        viewModelScope.launch {
            val defaultMethod = paymentMethodRepository.getDefaultPaymentMethod()
            _uiState.update { it.copy(selectedPaymentMethod = defaultMethod) }
        }
    }

    fun selectPaymentMethod(paymentMethod: PaymentMethodDomainEntity) {
        _uiState.update {
            it.copy(
                selectedPaymentMethod = paymentMethod,
                isSelectingPaymentMethod = false
            )
        }
    }

    fun togglePaymentMethodSelection() {
        _uiState.update { it.copy(isSelectingPaymentMethod = !it.isSelectingPaymentMethod) }
    }

    suspend fun getTokenizedPaymentInfo(): String? {
        val selectedMethod = uiState.value.selectedPaymentMethod ?: return null

        return paymentMethodRepository.getPaymentMethodToken(
            selectedMethod.firstSixNum,
            selectedMethod.lastFourNum
        )
    }

    fun setDefaultPaymentMethod() {
        val selectedMethod = uiState.value.selectedPaymentMethod ?: return

        viewModelScope.launch(Dispatchers.IO) {
            paymentMethodRepository.setDefaultPaymentMethod(
                selectedMethod.firstSixNum,
                selectedMethod.lastFourNum
            )
        }
    }

    fun startAuthentication() {
        _uiState.update { it.copy(isAuthenticating = true) }
    }

    fun finishAuthentication() {
        _uiState.update { it.copy(isAuthenticating = false) }
    }
}
