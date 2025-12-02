package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.SellerRequestApi
import com.ptit.data.remote.dto.seller_request.*
import com.ptit.domain.utils.Resource
import javax.inject.Inject

class SellerRequestRemoteDataSource @Inject constructor(
    private val remoteService: SellerRequestApi
) {
    suspend fun createSellerRequest(request: CreateSellerRequestBody): Resource<CreateSellerRequestResponse> =
        remoteService.createSellerRequest(request)

    suspend fun getMySellerRequest(): Resource<GetMySellerRequestResponse> =
        remoteService.getMySellerRequest()

    suspend fun getSellerRequests(
        page: Int?,
        limit: Int?,
        status: String?
    ): Resource<GetSellerRequestsResponse> =
        remoteService.getSellerRequests(page, limit, status)

    suspend fun approveSellerRequest(requestId: String): Resource<ApproveSellerRequestResponse> =
        remoteService.approveSellerRequest(requestId)

    suspend fun rejectSellerRequest(
        requestId: String,
        body: RejectSellerRequestBody
    ): Resource<ApproveSellerRequestResponse> =
        remoteService.rejectSellerRequest(requestId, body)
}

