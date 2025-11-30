package com.ptit.domain.repository

import com.ptit.domain.entity.shipping.CalculateShippingFeeRequestDomainEntity
import com.ptit.domain.entity.shipping.CalculateShippingFeeResponseDomainEntity
import com.ptit.domain.entity.shipping.DistrictEntity
import com.ptit.domain.entity.shipping.ProvinceEntity
import com.ptit.domain.entity.shipping.WardEntity
import com.ptit.domain.utils.Resource

interface ShippingRepository {
    suspend fun getProvinces(): Resource<List<ProvinceEntity>>
    suspend fun getDistricts(provinceId: Int): Resource<List<DistrictEntity>>
    suspend fun getWards(districtId: Int): Resource<List<WardEntity>>

    // ✅ NEW: Calculate shipping fee from GHN
    suspend fun calculateShippingFee(
        request: CalculateShippingFeeRequestDomainEntity
    ): Resource<CalculateShippingFeeResponseDomainEntity>
}