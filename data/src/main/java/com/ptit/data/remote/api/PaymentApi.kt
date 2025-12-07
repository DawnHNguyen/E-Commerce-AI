package com.ptit.data.remote.api

import com.ptit.data.remote.dto.payment.GetBillingInfoResponseDto
import com.ptit.data.remote.dto.payment.ProcessPaymentRequestDto
import com.ptit.data.remote.dto.payment.ProcessPaymentResponseDto
import com.ptit.domain.utils.Resource
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PaymentApi {

    @GET("payment/recurly/billing-info")
    suspend fun getBillingInfo(): Resource<GetBillingInfoResponseDto>

    @POST("payment/recurly/process-payment")
    suspend fun processPayment(
        @Body request: ProcessPaymentRequestDto
    ): Resource<ProcessPaymentResponseDto>
}

