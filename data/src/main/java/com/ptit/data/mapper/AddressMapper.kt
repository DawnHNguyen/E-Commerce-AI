package com.ptit.data.mapper

import com.ptit.data.remote.dto.address.AddressDto
import com.ptit.domain.entity.address.AddressDomainEntity

fun AddressDto.toDomainEntity() = AddressDomainEntity(
    id = id,
    recipient = recipient.orEmpty(),
    phoneNumber = phoneNumber.orEmpty(),
    province = province,
    district = district,
    ward = ward,
    street = street,
    isDefault = isDefault,
    createdAt = createdAt,
    updatedAt = updatedAt
)

