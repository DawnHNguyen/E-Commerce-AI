package com.ptit.data.remote.dto.payment

import com.google.gson.annotations.SerializedName

// Request to process payment
data class ProcessPaymentRequestDto(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("tokenId") val tokenId: String,
    @SerializedName("currency") val currency: String = "VND",
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("description") val description: String? = null
)

// Payment response data
data class PaymentDataDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("transactionId") val transactionId: String?,
    @SerializedName("accountId") val accountId: String?,
    @SerializedName("status") val status: String
)

// Process payment response
data class ProcessPaymentResponseDto(
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PaymentDataDto
)

// Billing info response
data class BillingInfoDto(
    @SerializedName("id") val id: String,
    @SerializedName("cardType") val cardType: String?,
    @SerializedName("lastFour") val lastFour: String?,
    @SerializedName("expMonth") val expMonth: Int?,
    @SerializedName("expYear") val expYear: Int?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class GetBillingInfoResponseDto(
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<BillingInfoDto>
)

