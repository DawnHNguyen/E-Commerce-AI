package com.ptit.domain.entity.shipping

data class CalculateShippingFeeRequestDomainEntity(
    val height: Double,
    val weight: Double,
    val length: Double,
    val width: Double,
    val serviceTypeId: Int? = 2,
    val serviceId: Int? = null,
    val wardCode: String,
    val districtId: Int,
    val provinceId: Int,
    val insuranceValue: Int? = null,
    val coupon: String? = null,
    val codValue: Int? = null
)

data class CalculateShippingFeeResponseDomainEntity(
    val total: Int,
    val serviceFee: Int,
    val insuranceFee: Int,
    val pickStationFee: Int = 0,
    val couponValue: Int = 0,
    val r2sFee: Int = 0,
    val codFee: Int = 0,
    val pickRemoteAreasFee: Int = 0,
    val deliverRemoteAreasFee: Int = 0,
    val codFailedFee: Int = 0
)

