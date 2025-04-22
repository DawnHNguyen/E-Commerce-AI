package com.ptit.data.remote.api

import com.ptit.data.remote.dto.cart.PurchaseDto
import com.ptit.data.remote.dto.cart.UpdatePurchaseRequestDto
import com.ptit.data.remote.dto.cart.DeletePurchaseResponseDto
import com.ptit.domain.utils.Resource
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PUT
import retrofit2.http.Query

interface PurchaseApi {
    @GET("purchases")
    suspend fun getPurchases(
        @Query("status") status: Int? = null
    ): Resource<List<PurchaseDto>>

    @PUT("purchases/update-purchase")
    suspend fun updatePurchase(
        @Body updatePurchaseRequest: UpdatePurchaseRequestDto
    ): Resource<PurchaseDto>

//    @DELETE("purchases")
//    suspend fun deletePurchases(
//        @Body purchaseIds: List<String>
//    ): Resource<DeletePurchaseResponseDto>


    // Thay đổi từ @DELETE sang @HTTP với method = "DELETE" và hasBody = true
    @HTTP(method = "DELETE", hasBody = true, path = "purchases")
    suspend fun deletePurchases(
        @Body purchaseIds: List<String>
    ): Resource<DeletePurchaseResponseDto>
}