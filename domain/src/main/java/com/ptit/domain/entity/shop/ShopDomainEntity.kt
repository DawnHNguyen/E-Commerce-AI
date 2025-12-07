package com.ptit.domain.entity.shop

data class ShopDomainEntity(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val avatar: String? = null,
    val userId: String = "",
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)
