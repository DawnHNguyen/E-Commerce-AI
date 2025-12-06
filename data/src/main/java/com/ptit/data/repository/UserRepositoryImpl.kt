package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.UserRemoteDataSource
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.repository.UserRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.UnknownException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource,
) : UserRepository {
    override suspend fun getUserProfile(): Resource<UserDomainEntity> {
        return try {
            val response = remoteDataSource.getUserProfile()
            if (response.data != null) {
                Resource.success(response.data.toDomainEntity())
            } else {
                Resource.error(
                    UnknownException(
                        error = null,
                        message = "Failed to get user profile",
                        requestUrl = "/profile"
                    )
                )
            }
        } catch (e: Exception) {
            Resource.error(
                UnknownException(
                    error = null,
                    message = e.message ?: "Unknown error",
                    requestUrl = "/profile"
                )
            )
        }
    }

    override suspend fun updateUserProfile(
        name: String,
        phoneNumber: String,
        avatar: String,
    ): Resource<Unit> {
        return try {
            val response = remoteDataSource.updateUserProfile(
                name = name,
                phoneNumber = phoneNumber,
                avatar = avatar
            )

            // ✅ Backend returns { data: {...}, statusCode: 200 } on success
            // Check both data and statusCode
            if (response.data != null && response.statusCode == 200) {
                Resource.success(Unit)
            } else {
                Resource.error(
                    UnknownException(
                        error = null,
                        message = "Update failed: statusCode=${response.statusCode}, data=${response.data}",
                        requestUrl = "/profile"
                    )
                )
            }
        } catch (e: Exception) {
            // ✅ Handle all exceptions (HTTP errors, network, parsing, etc.)
            Resource.error(
                UnknownException(
                    error = null,
                    message = e.message ?: "Unknown error",
                    requestUrl = "/profile"
                )
            )
        }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): Resource<Unit> {
        return try {
            val response = remoteDataSource.changePassword(
                currentPassword = currentPassword,
                newPassword = newPassword,
                confirmNewPassword = confirmNewPassword
            )

            // ✅ Check statusCode like updateUserProfile does
            if (response.statusCode == 200) {
                Resource.success(Unit)
            } else {
                Resource.error(
                    UnknownException(
                        error = null,
                        message = response.message ?: "Failed to change password",
                        requestUrl = "/profile/change-password"
                    )
                )
            }
        } catch (e: Exception) {
            Resource.error(
                UnknownException(
                    error = null,
                    message = e.message ?: "Unknown error",
                    requestUrl = "/profile/change-password"
                )
            )
        }
    }
}
