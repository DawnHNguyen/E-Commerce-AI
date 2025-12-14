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
        return remoteDataSource.createSellerRequest(request).map { it.data.toDomainEntity() }
    }

    override suspend fun getMySellerRequest(): Resource<SellerRequestDomainEntity?> {
        val remoteResult = try {
            remoteDataSource.getMySellerRequest()
        } catch (t: Throwable) {
            // If remote call itself throws for unexpected reasons, log and return safe null success
            Log.w("SellerRequestRepository", "Remote call getMySellerRequest threw: $t")
            return Resource.success(null)
        }

        return try {
            // Map safely: if the remote returned a malformed type (ClassCastException) or mapping fails,
            // don't crash the app — return success(null) so UI can show create-shop flow.
            remoteResult.map { dto -> dto?.toDomainEntity() }
        } catch (t: Throwable) {
            Log.w("SellerRequestRepository", "Failed to map getMySellerRequest response, returning null safe value: $t")
            Resource.success(null)
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
