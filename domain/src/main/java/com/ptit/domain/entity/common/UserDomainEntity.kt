package com.ptit.domain.entity.common

data class UserDomainEntity(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val avatar: String = "",
    val status: String = "",
    val roleId: String = "",
    val createdById: String? = null,
    val updatedById: String? = null,
    val deletedById: String? = null,
    val deletedAt: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val role: Role? = null,
) {
    data class Role(
        val id: String = "",
        val name: String = "",
        val permissions: List<Permission> = emptyList(),
    ) {
        data class Permission(
            val id: String = "",
            val name: String = "",
            val module: String = "",
            val path: String = "",
            val method: String = "",
        )
    }
}