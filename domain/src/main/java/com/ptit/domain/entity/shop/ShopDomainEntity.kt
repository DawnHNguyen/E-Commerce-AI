package com.ptit.domain.entity.shop

data class ShopDomainEntity(
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val phone: String = "",
    val avatar: String = "",
    val totalProduct: Int? = 0,
    val totalOrder: Int? = 0,
)
