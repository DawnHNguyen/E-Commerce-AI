package com.ptit.domain.entity.payment

import com.recurly.androidsdk.data.model.CreditCardsParameters

data class PaymentMethodDomainEntity(
    val firstSixNum: String,
    val lastFourNum: String,
    val cardType: CreditCardsParameters,
    val token: String,
    val isDefault: Boolean = false
)
