package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.ShopApi
import com.ptit.data.remote.dto.shop.CreateAndUpdateShopRequest
import javax.inject.Inject

class ShopRemoteDataSource @Inject constructor(private val remoteService: ShopApi) {
    suspend fun updateShop(createAndUpdateShopRequest: CreateAndUpdateShopRequest) =
        remoteService.updateShop(createAndUpdateShopRequest = createAndUpdateShopRequest)
    suspend fun getMyShop() = remoteService.getMyShop()
}