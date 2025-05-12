package com.ptit.domain.usecase

import android.net.Uri
import com.ptit.domain.repository.FileUploadRepository
import com.ptit.domain.repository.UserRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val fileUploadRepository: FileUploadRepository
) {
    suspend operator fun invoke(
        name: String?,
        phone: String?,
        avatar: Uri?,
        address: String?
    ): Resource<Unit> {
        val avatarUrl = if (avatar != null) {
            when (val result = fileUploadRepository.uploadSingleFile(avatar)) {
                is Resource.Success -> result.data
                is Resource.Error -> return result.map {  }
                else -> null
            }
        } else null

        return userRepository.updateUserProfile(
            name = name,
            phone = phone,
            avatar = avatarUrl,
            address = address
        ).map {  }
    }
}