package com.ptit.domain.repository

import com.ptit.domain.entity.payment.BillingInfoDomainEntity
import com.ptit.domain.entity.payment.PaymentMethodDomainEntity
import com.ptit.domain.entity.payment.ProcessPaymentDomainEntity
import com.ptit.domain.utils.Resource
import kotlinx.coroutines.flow.Flow

interface PaymentMethodRepository {
    suspend fun savePaymentMethod(paymentMethod: PaymentMethodDomainEntity): Resource<Unit>
    fun getPaymentMethods(): Flow<List<PaymentMethodDomainEntity>>
    suspend fun getDefaultPaymentMethod(): PaymentMethodDomainEntity?
    suspend fun deletePaymentMethod(firstSixNum: String, lastFourNum: String): Resource<Unit>
    suspend fun getPaymentMethodToken(firstSixNum: String, lastFourNum: String): String?
    suspend fun setDefaultPaymentMethod(firstSixNum: String, lastFourNum: String): Resource<Unit>

    // Recurly payment processing
    suspend fun processPayment(orderId: String, tokenId: String, currency: String = "VND"): Resource<ProcessPaymentDomainEntity>
    suspend fun getBillingInfo(): Resource<List<BillingInfoDomainEntity>>
}
