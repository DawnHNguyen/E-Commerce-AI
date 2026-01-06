package com.ptit.data.repository

import android.util.Log
import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.SellerRequestRemoteDataSource
import com.ptit.data.remote.dto.seller_request.CreateSellerRequestBody
import com.ptit.data.remote.dto.seller_request.RejectSellerRequestBody
import com.ptit.domain.entity.seller_request.SellerRequestDomainEntity
import com.ptit.domain.entity.seller_request.SellerRequestListDomainEntity
import com.ptit.domain.repository.SellerRequestRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.UnknownException
import com.ptit.domain.utils.map
import javax.inject.Inject

class SellerRequestRepositoryImpl @Inject constructor(
    private val remoteDataSource: SellerRequestRemoteDataSource
) : SellerRequestRepository {

    override suspend fun createSellerRequest(
        shopName: String,
        shopDescription: String?,
        businessLicense: String?,
        taxCode: String?
    ): Resource<SellerRequestDomainEntity> {
        val request = CreateSellerRequestBody(
            shopName = shopName,
            shopDescription = shopDescription,
            businessLicense = businessLicense,
            taxCode = taxCode
        )
        return remoteDataSource.createSellerRequest(request).map { response ->
            response.data.toDomainEntity()
        }
    }

    override suspend fun getMySellerRequest(): Resource<SellerRequestDomainEntity?> {
        // Gọi datasource (đã trả về Resource an toàn ở bước 3)
        return remoteDataSource.getMySellerRequest().map { dto ->
            // dto có thể là null, hàm map này sẽ xử lý an toàn
            dto?.toDomainEntity()
        }
    }

    override suspend fun getSellerRequests(
        page: Int?,
        limit: Int?,
        status: String?
    ): Resource<SellerRequestListDomainEntity> {
        return remoteDataSource.getSellerRequests(page, limit, status).map { it.toDomainEntity() }
    }

    override suspend fun approveSellerRequest(requestId: String): Resource<SellerRequestDomainEntity> {
        return remoteDataSource.approveSellerRequest(requestId).map { it.data.toDomainEntity() }
    }

    override suspend fun rejectSellerRequest(
        requestId: String,
        rejectionReason: String
    ): Resource<SellerRequestDomainEntity> {
        val body = RejectSellerRequestBody(rejectionReason)
        return remoteDataSource.rejectSellerRequest(requestId, body).map { it.data.toDomainEntity() }
    }
}
