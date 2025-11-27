package com.ptit.data.remote.dto.common

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("roleId")
    val roleId: String?,
    @SerializedName("createdById")
    val createdById: String?,
    @SerializedName("updatedById")
    val updatedById: String?,
    @SerializedName("deletedById")
    val deletedById: String?,
    @SerializedName("deletedAt")
    val deletedAt: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("role")
    val role: RoleDto?,
) {
    data class RoleDto(
        @SerializedName("id")
        val id: String?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("permissions")
        val permissions: List<PermissionDto>?,
    ) {
        data class PermissionDto(
            @SerializedName("id")
            val id: String?,
            @SerializedName("name")
            val name: String?,
            @SerializedName("module")
            val module: String?,
            @SerializedName("path")
            val path: String?,
            @SerializedName("method")
            val method: String?,
        )
    }
}