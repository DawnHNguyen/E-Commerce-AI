package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.ShopApi
import com.ptit.data.remote.dto.shop.CreateShopRequest
import com.ptit.data.remote.dto.shop.ShopResponse
import com.ptit.data.remote.dto.shop.UpdateShopRequest
import com.ptit.domain.utils.Resource
import javax.inject.Inject

class ShopRemoteDataSource @Inject constructor(private val remoteService: ShopApi) {
    suspend fun createShop(request: CreateShopRequest): Resource<ShopResponse> =
        remoteService.createShop(request)

    suspend fun updateShop(request: UpdateShopRequest): Resource<ShopResponse> =
        remoteService.updateShop(request)

    suspend fun getMyShop(): Resource<ShopResponse> =
        remoteService.getMyShop()
}