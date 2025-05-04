package com.ptit.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.ptit.data.api.FileUploadApi
import com.ptit.domain.repository.FileUploadRepository
import com.ptit.domain.utils.BadRequestException
import com.ptit.domain.utils.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class FileUploadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileUploadApi: FileUploadApi
) : FileUploadRepository {

    override suspend fun uploadSingleFile(fileUri: Uri): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(fileUri)
            val mimeType = getMimeType(fileUri)
            val requestFile = file.asRequestBody(mimeType?.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val response = fileUploadApi.uploadFile(body)
            if (response.isSuccessful && response.body() != null) {
                Resource.success(response.body()!!.data)
            } else {
                Resource.error(BadRequestException(error=null, requestUrl = ""))
            }
        } catch (e: Exception) {
            Resource.error(BadRequestException(error=null, requestUrl = ""))
        }
    }

    override suspend fun uploadMultipleFiles(fileUris: List<Uri>): Resource<List<String>> = withContext(Dispatchers.IO) {
        try {
            val parts = fileUris.map { uri ->
                val file = uriToFile(uri)
                val mimeType = getMimeType(uri)
                val requestFile = file.asRequestBody(mimeType?.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", file.name, requestFile)
            }

            val response = fileUploadApi.uploadMultipleFiles(parts)
            if (response.isSuccessful && response.body() != null) {
                Resource.success(response.body()!!.data)
            } else {
                Resource.error(BadRequestException(error=null, requestUrl = ""))
            }
        } catch (e: Exception) {
            Resource.error(BadRequestException(error=null, requestUrl = ""))
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val fileName = getFileName(uri)
        val file = File(context.cacheDir, fileName)

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex("_display_name")
                    if (columnIndex >= 0) {
                        result = it.getString(columnIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "file_${System.currentTimeMillis()}"
    }

    private fun getMimeType(uri: Uri): String? {
        return if (uri.scheme == "content") {
            context.contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
        }
    }
}