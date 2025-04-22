package com.ptit.data.remote.dto.cart

import com.google.gson.annotations.SerializedName

data class DeletePurchaseResponseDto(
    @SerializedName("deleted_count")
    val deletedCount: Int
)