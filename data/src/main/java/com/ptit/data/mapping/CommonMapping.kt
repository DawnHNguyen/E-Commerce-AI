package com.ptit.data.mapping

import com.ptit.data.remote.dto.common.UserDto
import com.ptit.domain.entity.common.UserDomainEntity

fun UserDto.toDomainEntity(): UserDomainEntity {
    return UserDomainEntity(
        id = id ?: "",
        email = email ?: "",
        name = name ?: "",
        phoneNumber = phoneNumber ?: "",
        avatar = avatar ?: "",
        status = status ?: "",
        roleId = roleId ?: "",
        createdById = createdById,
        updatedById = updatedById,
        deletedById = deletedById,
        deletedAt = deletedAt,
        createdAt = createdAt ?: "",
        updatedAt = updatedAt ?: "",
        role = role?.toDomainEntity(),
    )
}

fun UserDto.RoleDto.toDomainEntity(): UserDomainEntity.Role {
    return UserDomainEntity.Role(
        id = id ?: "",
        name = name ?: "",
        permissions = permissions?.map { it.toDomainEntity() } ?: emptyList(),
    )
}

fun UserDto.RoleDto.PermissionDto.toDomainEntity(): UserDomainEntity.Role.Permission {
    return UserDomainEntity.Role.Permission(
        id = id ?: "",
        name = name ?: "",
        module = module ?: "",
        path = path ?: "",
        method = method ?: "",
    )
}