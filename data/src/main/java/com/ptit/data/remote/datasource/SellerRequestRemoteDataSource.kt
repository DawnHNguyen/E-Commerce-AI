package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.SellerRequestApi
import com.ptit.data.remote.dto.seller_request.*
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.UnknownException
import javax.inject.Inject

class SellerRequestRemoteDataSource @Inject constructor(
    private val remoteService: SellerRequestApi
) {
    suspend fun createSellerRequest(request: CreateSellerRequestBody): Resource<CreateSellerRequestResponse> =
        remoteService.createSellerRequest(request)

    suspend fun getMySellerRequest(): Resource<SellerRequestDto?> {
        return try {
            // 1. Gọi API (lấy về DTO)
            val response = remoteService.getMySellerRequest()

            // 2. Tự đóng gói thành Resource.Success
            // response.data lúc này là null, và Resource.success chấp nhận null
            Resource.success(response.data)
        } catch (e: Exception) {
            // 3. Xử lý lỗi nếu mạng hỏng
            Resource.error(
                UnknownException(
                    error = null,
                    message = e.message ?: "Lỗi kết nối",
                    requestUrl = "seller-requests/my-request"
                )
            )
        }
    }

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

