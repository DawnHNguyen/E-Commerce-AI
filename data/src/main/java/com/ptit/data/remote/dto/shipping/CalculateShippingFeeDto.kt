package com.ptit.data.remote.dto.shipping

import com.google.gson.annotations.SerializedName

data class CalculateShippingFeeRequestDto(
    @SerializedName("height") val height: Double,
    @SerializedName("weight") val weight: Double,
    @SerializedName("length") val length: Double,
    @SerializedName("width") val width: Double,
    @SerializedName("service_type_id") val serviceTypeId: Int? = 2,
    @SerializedName("service_id") val serviceId: Int? = null,
    @SerializedName("wardCode") val wardCode: String,
    @SerializedName("districtId") val districtId: Int,
    @SerializedName("provinceId") val provinceId: Int,
    @SerializedName("insurance_value") val insuranceValue: Int? = null,
    @SerializedName("coupon") val coupon: String? = null,
    @SerializedName("cod_value") val codValue: Int? = null
)

data class CalculateShippingFeeResponseDto(
    @SerializedName("total") val total: Int,
    @SerializedName("service_fee") val serviceFee: Int,
    @SerializedName("insurance_fee") val insuranceFee: Int,
    @SerializedName("pick_station_fee") val pickStationFee: Int? = 0,
    @SerializedName("coupon_value") val couponValue: Int? = 0,
    @SerializedName("r2s_fee") val r2sFee: Int? = 0,
    @SerializedName("cod_fee") val codFee: Int? = 0,
    @SerializedName("pick_remote_areas_fee") val pickRemoteAreasFee: Int? = 0,
    @SerializedName("deliver_remote_areas_fee") val deliverRemoteAreasFee: Int? = 0,
    @SerializedName("cod_failed_fee") val codFailedFee: Int? = 0
)

