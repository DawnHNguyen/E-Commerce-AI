package com.ptit.data.remote.dto.shipping

import com.google.gson.annotations.SerializedName

// 1. Tỉnh/Thành phố
data class ProvinceDto(
    @SerializedName("ProvinceID") val provinceId: Int,
    @SerializedName("ProvinceName") val provinceName: String
    // Bỏ các trường không cần thiết khác
)

// 2. Quận/Huyện
data class DistrictDto(
    @SerializedName("DistrictID") val districtId: Int,
    @SerializedName("ProvinceID") val provinceId: Int,
    @SerializedName("DistrictName") val districtName: String
)

// 3. Phường/Xã
data class WardDto(
    @SerializedName("WardCode") val wardCode: String,
    @SerializedName("DistrictID") val districtId: Int,
    @SerializedName("WardName") val wardName: String
)

// DTO cho Query
data class GetDistrictsQueryDto(
    @SerializedName("provinceId") val provinceId: Int
)

data class GetWardsQueryDto(
    @SerializedName("districtId") val districtId: Int
)