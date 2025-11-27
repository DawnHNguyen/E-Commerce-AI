package com.ptit.data.remote.api

import com.ptit.data.remote.dto.common.UserDto
import com.ptit.data.remote.dto.shipping.DistrictDto
import com.ptit.data.remote.dto.shipping.ProvinceDto
import com.ptit.data.remote.dto.shipping.WardDto
import com.ptit.domain.utils.Resource
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface ShippingApi {
    // Backend: @Get('address/provinces') -> Trả về List<ProvinceSchema>
    @GET("shipping/ghn/address/provinces")
    suspend fun getProvinces(): Resource<List<ProvinceDto>>

    // Backend: @Get('address/districts') -> Trả về List<DistrictSchema>
    @GET("shipping/ghn/address/districts")
    suspend fun getDistricts(@Query("provinceId") provinceId: Int): Resource<List<DistrictDto>>

    // Backend: @Get('address/wards') -> Trả về List<WardSchema>
    @GET("shipping/ghn/address/wards")
    suspend fun getWards(@Query("districtId") districtId: Int): Resource<List<WardDto>>
}