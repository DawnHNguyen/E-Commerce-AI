package com.ptit.data.mapping

import com.ptit.data.remote.dto.address.AddressDto
import com.ptit.domain.entity.address.AddressDomainEntity

fun AddressDto.toDomainEntity(): AddressDomainEntity {
    // 1. Xử lý ghép địa chỉ hiển thị
    val fullAddressStr = listOfNotNull(
        street,
        ward,
        district,
        province
    ).filter { !it.isNullOrBlank() }.joinToString(", ")

    // 2. Xử lý tên người nhận: Nếu recipient null thì lấy name, nếu name null thì để trống
    val displayRecipient = recipient ?: name ?: "Không tên"

    return AddressDomainEntity(
        id = id,
        label = name ?: "Địa chỉ",
        recipientName = displayRecipient,
        phoneNumber = phoneNumber ?: "",
        fullAddress = fullAddressStr,
        isDefault = isDefault ?: false,
        provinceId = provinceId ?: 0,
        districtId = districtId ?: 0,
        wardCode = wardCode ?: ""
    )
}