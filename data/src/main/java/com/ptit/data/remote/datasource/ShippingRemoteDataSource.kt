package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.ShippingApi
import com.ptit.data.remote.dto.shipping.CalculateShippingFeeRequestDto

import javax.inject.Inject

class ShippingRemoteDataSource @Inject constructor(private val api: ShippingApi) {

    /**
     * Lấy danh sách Tỉnh/Thành phố
     */
    suspend fun getProvinces() = api.getProvinces()

    /**
     * Lấy danh sách Quận/Huyện dựa trên ID Tỉnh
     */
    suspend fun getDistricts(provinceId: Int) = api.getDistricts(provinceId)

    /**
     * Lấy danh sách Phường/Xã dựa trên ID Quận/Huyện
     */
    suspend fun getWards(districtId: Int) = api.getWards(districtId)

    /**
     * ✅ NEW: Tính phí vận chuyển từ GHN API
     */
    suspend fun calculateShippingFee(request: CalculateShippingFeeRequestDto) =
        api.calculateShippingFee(request)
}