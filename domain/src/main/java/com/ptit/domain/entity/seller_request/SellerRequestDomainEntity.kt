package com.ptit.domain.entity.seller_request

data class SellerRequestDomainEntity(
    val id: String = "",
    val userId: String = "",
    val shopName: String = "",
    val shopDescription: String? = null,
    val businessLicense: String? = null,
    val taxCode: String? = null,
    val status: SellerRequestStatus = SellerRequestStatus.PENDING,
    val rejectionReason: String? = null,
    val approvedById: String? = null,
    val approvedAt: String? = null,
    val rejectedById: String? = null,
    val rejectedAt: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val userName: String? = null,
    val userEmail: String? = null,
    val userPhone: String? = null
)

enum class SellerRequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class SellerRequestListDomainEntity(
    val requests: List<SellerRequestDomainEntity> = emptyList(),
    val totalItems: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val totalPages: Int = 0,
    val hasNext: Boolean = false,
    val hasPrev: Boolean = false
)

