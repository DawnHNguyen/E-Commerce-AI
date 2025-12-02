package com.ptit.data.mapping

import com.ptit.data.remote.dto.shipping.CalculateShippingFeeRequestDto
import com.ptit.data.remote.dto.shipping.CalculateShippingFeeResponseDto
import com.ptit.data.remote.dto.shipping.DistrictDto
import com.ptit.data.remote.dto.shipping.ProvinceDto
import com.ptit.data.remote.dto.shipping.WardDto
import com.ptit.domain.entity.shipping.CalculateShippingFeeRequestDomainEntity
import com.ptit.domain.entity.shipping.CalculateShippingFeeResponseDomainEntity
import com.ptit.domain.entity.shipping.DistrictEntity
import com.ptit.domain.entity.shipping.ProvinceEntity
import com.ptit.domain.entity.shipping.WardEntity

fun ProvinceDto.toDomainEntity() = ProvinceEntity(
    id = provinceId,
    name = provinceName
)

fun DistrictDto.toDomainEntity() = DistrictEntity(
    id = districtId,
    provinceId = provinceId,
    name = districtName
)

fun WardDto.toDomainEntity() = WardEntity(
    code = wardCode,
    districtId = districtId,
    name = wardName
)

// ✅ NEW: Calculate Shipping Fee Mapping
fun CalculateShippingFeeRequestDomainEntity.toDto() = CalculateShippingFeeRequestDto(
    height = height,
    weight = weight,
    length = length,
    width = width,
    serviceTypeId = serviceTypeId,
    serviceId = serviceId,
    wardCode = wardCode,
    districtId = districtId,
    provinceId = provinceId,
    insuranceValue = insuranceValue,
    coupon = coupon,
    codValue = codValue
)

fun CalculateShippingFeeResponseDto.toDomainEntity() = CalculateShippingFeeResponseDomainEntity(
    total = total,
    serviceFee = serviceFee,
    insuranceFee = insuranceFee,
    pickStationFee = pickStationFee ?: 0,
    couponValue = couponValue ?: 0,
    r2sFee = r2sFee ?: 0,
    codFee = codFee ?: 0,
    pickRemoteAreasFee = pickRemoteAreasFee ?: 0,
    deliverRemoteAreasFee = deliverRemoteAreasFee ?: 0,
    codFailedFee = codFailedFee ?: 0
)
