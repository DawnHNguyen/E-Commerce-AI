package com.ptit.data.remote.api

import com.ptit.data.remote.dto.cart.PurchaseDto
import com.ptit.domain.utils.Resource
import retrofit2.http.GET
import retrofit2.http.Query

interface PurchaseApi {
    @GET("purchases")
    suspend fun getPurchases(
        @Query("status") status: Int? = null
    ): Resource<List<PurchaseDto>>
}