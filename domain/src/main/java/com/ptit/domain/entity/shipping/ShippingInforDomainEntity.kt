package com.ptit.domain.entity.shipping

data class ProvinceEntity(
    val id: Int,
    val name: String
)

data class DistrictEntity(
    val id: Int,
    val provinceId: Int,
    val name: String
)

data class WardEntity(
    val code: String,
    val districtId: Int,
    val name: String
)
