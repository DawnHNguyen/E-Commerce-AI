package com.ptit.domain.entity.common

data class UserDomainEntity(
    val createdAt: String = "",
    val email: String = "",
    val id: String = "",
    val shop: Shop = Shop(),
    val updatedAt: String = "",
    val watchList: List<String> = emptyList(),
    val name: String = "",
    val phone: String = "",
    val avatar: String = "",
) {
    data class Shop(
        val address: String = "",
        val avatar: String = "",
        val description: String = "",
        val name: String = "",
        val phone: String = "",
    )
}
