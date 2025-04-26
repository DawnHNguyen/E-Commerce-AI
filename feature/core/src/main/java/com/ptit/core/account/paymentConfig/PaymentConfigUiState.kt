package com.ptit.core.account.paymentConfig

data class PaymentConfigUiState(
    val cardNumber: String = "",
    val expirationDate: String = "",
    val cvv: String = "",
    val cardType: String = "",
    val isCardNumberValid: Boolean = true,
    val isExpirationDateValid: Boolean = true,
    val isCvvValid: Boolean = true,
    val useAsDefaultCard: Boolean = false
) {
    val isEnableSaveButton: Boolean
        get() = isCardNumberValid && isExpirationDateValid && isCvvValid &&
                cardNumber.isNotEmpty() && expirationDate.isNotEmpty() && cvv.isNotEmpty()
}
