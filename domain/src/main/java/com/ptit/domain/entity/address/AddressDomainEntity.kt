package com.ptit.domain.entity.address

data class AddressDomainEntity(
    val id: String = "",
    val recipient: String = "",
    val phoneNumber: String = "",
    val province: String = "",
    val district: String = "",
    val ward: String = "",
    val street: String = "",
    val isDefault: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    val fullAddress: String
        get() = listOf(street, ward, district, province)
            .filter { it.isNotBlank() }
            .joinToString(", ")
}

