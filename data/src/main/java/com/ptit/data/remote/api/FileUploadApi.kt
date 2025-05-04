package com.ptit.data.api


import com.ptit.data.remote.dto.file.FileUploadResponse
import com.ptit.data.remote.dto.file.MultipleFileUploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileUploadApi {
    @Multipart
    @POST("/admin/products/upload-image")
    suspend fun uploadFile(@Part file: MultipartBody.Part): Response<FileUploadResponse>

    @Multipart
    @POST("/admin/products/upload-images")
    suspend fun uploadMultipleFiles(@Part files: List<MultipartBody.Part>): Response<MultipleFileUploadResponse>
}