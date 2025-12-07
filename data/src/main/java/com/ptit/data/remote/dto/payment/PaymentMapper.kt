package com.ptit.data.remote.dto.payment

import com.ptit.domain.entity.payment.BillingInfoDomainEntity
import com.ptit.domain.entity.payment.ProcessPaymentDomainEntity

fun ProcessPaymentResponseDto.toDomain(): ProcessPaymentDomainEntity {
    return ProcessPaymentDomainEntity(
        success = data.success,
        transactionId = data.transactionId,
        accountId = data.accountId,
        status = data.status,
        message = message
    )
}

fun BillingInfoDto.toDomain(): BillingInfoDomainEntity {
    return BillingInfoDomainEntity(
        id = id,
        cardType = cardType,
        lastFour = lastFour,
        expMonth = expMonth,
        expYear = expYear,
        updatedAt = updatedAt
    )
}

fun GetBillingInfoResponseDto.toDomain(): List<BillingInfoDomainEntity> {
    return data.map { it.toDomain() }
}

