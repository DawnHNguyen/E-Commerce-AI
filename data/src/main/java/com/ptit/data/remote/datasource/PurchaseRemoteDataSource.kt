package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.PurchaseApi
import javax.inject.Inject

class PurchaseRemoteDataSource @Inject constructor(private val remoteService: PurchaseApi) {
    suspend fun getPurchases(status: Int? = null) = remoteService.getPurchases(status)
}