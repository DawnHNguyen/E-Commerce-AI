package com.ptit.domain.repository

import com.ptit.domain.entity.seller_request.SellerRequestDomainEntity
import com.ptit.domain.entity.seller_request.SellerRequestListDomainEntity
import com.ptit.domain.utils.Resource

interface SellerRequestRepository {

    // User APIs
    suspend fun createSellerRequest(
        shopName: String,
        shopDescription: String?,
        businessLicense: String?,
        taxCode: String?
    ): Resource<SellerRequestDomainEntity>

    suspend fun getMySellerRequest(): Resource<SellerRequestDomainEntity?>

    // Admin APIs
    suspend fun getSellerRequests(
        page: Int?,
        limit: Int?,
        status: String?
    ): Resource<SellerRequestListDomainEntity>

    suspend fun approveSellerRequest(requestId: String): Resource<SellerRequestDomainEntity>

    suspend fun rejectSellerRequest(
        requestId: String,
        rejectionReason: String
    ): Resource<SellerRequestDomainEntity>
}

