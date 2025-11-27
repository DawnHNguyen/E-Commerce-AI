package com.ptit.data.mapping

import com.ptit.data.remote.dto.shipping.DistrictDto
import com.ptit.data.remote.dto.shipping.ProvinceDto
import com.ptit.data.remote.dto.shipping.WardDto
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