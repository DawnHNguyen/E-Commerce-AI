package com.ptit.data.remote.api

import com.ptit.data.remote.dto.shop.CreateShopRequest
import com.ptit.data.remote.dto.shop.ShopResponse
import com.ptit.data.remote.dto.shop.UpdateShopRequest
import com.ptit.domain.utils.Resource
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ShopApi {
    @POST("shops")
    suspend fun createShop(
       @Body request: CreateShopRequest,
    ): Resource<ShopResponse>

    @PUT("shops/me")
    suspend fun updateShop(
       @Body request: UpdateShopRequest,
    ): Resource<ShopResponse>

    @GET("shops/me")
    suspend fun getMyShop(): Resource<ShopResponse>
}