package com.ptit.data.remote.api

import com.ptit.data.remote.dto.shop.CreateAndUpdateShopRequest
import com.ptit.data.remote.dto.shop.CreateAndUpdateShopResponse
import com.ptit.domain.utils.Resource
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ShopApi {
    @PUT("/admin/shop")
    suspend fun updateShop(
       @Body createAndUpdateShopRequest: CreateAndUpdateShopRequest,
    ): Resource<CreateAndUpdateShopResponse>

    @GET("/admin/shop/me")
    suspend fun getMyShop(): Resource<CreateAndUpdateShopResponse>
}