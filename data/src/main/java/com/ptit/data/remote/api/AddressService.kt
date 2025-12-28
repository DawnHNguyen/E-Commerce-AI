package com.ptit.data.remote.api

import com.ptit.data.remote.dto.address.AddressRootResponse
import com.ptit.data.remote.dto.address.CreateAddressRequest
import com.ptit.data.remote.dto.address.CreateAddressResponse
import retrofit2.http.*

interface AddressService {

    @GET("profile/addresses")
    suspend fun getAddresses(): AddressRootResponse

    @POST("profile/addresses")
    suspend fun createAddress(
        @Body request: CreateAddressRequest
    ): CreateAddressResponse

    @DELETE("profile/addresses/{addressId}")
    suspend fun deleteAddress(
        @Path("addressId") addressId: String
    ): CreateAddressResponse
}

