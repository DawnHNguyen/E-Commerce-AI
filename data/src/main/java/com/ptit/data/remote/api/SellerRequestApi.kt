package com.ptit.data.remote.api

import com.ptit.data.remote.dto.seller_request.*
import com.ptit.domain.utils.Resource
import retrofit2.http.*

interface SellerRequestApi {

    // User APIs
    @POST("seller-requests")
    suspend fun createSellerRequest(
        @Body request: CreateSellerRequestBody
    ): Resource<CreateSellerRequestResponse>

    @GET("seller-requests/my-request")
    suspend fun getMySellerRequest(): Resource<GetMySellerRequestResponse>

    // Admin APIs
    @GET("seller-requests")
    suspend fun getSellerRequests(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("status") status: String? = null
    ): Resource<GetSellerRequestsResponse>

    @PUT("seller-requests/{requestId}/approve")
    suspend fun approveSellerRequest(
        @Path("requestId") requestId: String
    ): Resource<ApproveSellerRequestResponse>

    @PUT("seller-requests/{requestId}/reject")
    suspend fun rejectSellerRequest(
        @Path("requestId") requestId: String,
        @Body body: RejectSellerRequestBody
    ): Resource<ApproveSellerRequestResponse>
}

