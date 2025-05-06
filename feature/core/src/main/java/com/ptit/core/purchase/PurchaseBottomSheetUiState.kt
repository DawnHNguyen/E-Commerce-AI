package com.ptit.core.purchase

import com.ptit.domain.entity.payment.PaymentMethodDomainEntity

data class PurchaseBottomSheetUiState(
    val selectedPaymentMethod: PaymentMethodDomainEntity? = null,
    val isSelectingPaymentMethod: Boolean = false,
    val isAuthenticating: Boolean = false
)
