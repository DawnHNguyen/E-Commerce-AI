package com.ptit.domain.repository

import android.net.Uri
import com.ptit.domain.utils.Resource

interface FileUploadRepository {
    suspend fun uploadSingleFile(fileUri: Uri): Resource<String>
    suspend fun uploadMultipleFiles(fileUris: List<Uri>): Resource<List<String>>
}