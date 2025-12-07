package com.ptit.data.mapping

import com.ptit.data.remote.dto.seller_request.SellerRequestDto
import com.ptit.domain.entity.seller_request.SellerRequestDomainEntity
import com.ptit.domain.entity.seller_request.SellerRequestListDomainEntity
import com.ptit.domain.entity.seller_request.SellerRequestStatus
import com.ptit.data.remote.dto.seller_request.GetSellerRequestsResponse

fun SellerRequestDto.toDomainEntity(): SellerRequestDomainEntity {
    return SellerRequestDomainEntity(
        id = id,
        userId = userId,
        shopName = shopName,
        shopDescription = shopDescription,
        businessLicense = businessLicense,
        taxCode = taxCode,
        status = when (status) {
            "APPROVED" -> SellerRequestStatus.APPROVED
            "REJECTED" -> SellerRequestStatus.REJECTED
            else -> SellerRequestStatus.PENDING
        },
        rejectionReason = rejectionReason,
        approvedById = approvedById,
        approvedAt = approvedAt,
        rejectedById = rejectedById,
        rejectedAt = rejectedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        userName = user?.name,
        userEmail = user?.email,
        userPhone = user?.phoneNumber
    )
}

fun GetSellerRequestsResponse.toDomainEntity(): SellerRequestListDomainEntity {
    return SellerRequestListDomainEntity(
        requests = data.map { it.toDomainEntity() },
        totalItems = metadata.totalItems,
        page = metadata.page,
        limit = metadata.limit,
        totalPages = metadata.totalPages,
        hasNext = metadata.hasNext,
        hasPrev = metadata.hasPrev
    )
}

