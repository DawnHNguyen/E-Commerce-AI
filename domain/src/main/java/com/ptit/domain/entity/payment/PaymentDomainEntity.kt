package com.ptit.domain.entity.payment

data class ProcessPaymentDomainEntity(
    val success: Boolean,
    val transactionId: String?,
    val accountId: String?,
    val status: String,
    val message: String
)

data class BillingInfoDomainEntity(
    val id: String,
    val cardType: String?,
    val lastFour: String?,
    val expMonth: Int?,
    val expYear: Int?,
    val updatedAt: String?
)

