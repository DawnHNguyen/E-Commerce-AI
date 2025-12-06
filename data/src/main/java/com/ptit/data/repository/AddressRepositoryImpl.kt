package com.ptit.data.repository

import com.ptit.data.mapper.toDomainEntity
import com.ptit.data.remote.datasource.AddressRemoteDataSource
import com.ptit.domain.entity.address.AddressDomainEntity
import com.ptit.domain.utils.UnknownException
import com.ptit.domain.repository.AddressRepository
import com.ptit.domain.utils.Resource
import javax.inject.Inject

class AddressRepositoryImpl @Inject constructor(
    private val remoteDataSource: AddressRemoteDataSource
) : AddressRepository {

    override suspend fun getAddresses(): Resource<List<AddressDomainEntity>> {
        return try {
            val response = remoteDataSource.getAddresses()
            if (response.data != null) {
                Resource.success(response.data.map { it.toDomainEntity() })
            } else {
                Resource.error(
                    UnknownException(
                        error = null,
                        message = "Failed to get addresses",
                        requestUrl = "/profile/addresses"
                    )
                )
            }
        } catch (e: Exception) {
            Resource.error(
                UnknownException(
                    error = null,
                    message = e.message ?: "Unknown error",
                    requestUrl = "/profile/addresses"
                )
            )
        }
    }

    override suspend fun createAddress(
        name: String,
        recipient: String?,
        phoneNumber: String?,
        provinceId: Int,
        districtId: Int,
        wardCode: String,
        street: String,
        addressType: String,
        isDefault: Boolean
    ): Resource<AddressDomainEntity> {
        return try {
            val response = remoteDataSource.createAddress(
                name = name,
                recipient = recipient,
                phoneNumber = phoneNumber,
                provinceId = provinceId,
                districtId = districtId,
                wardCode = wardCode,
                street = street,
                addressType = addressType,
                isDefault = isDefault
            )
            if (response.data != null) {
                Resource.success(response.data.toDomainEntity())
            } else {
                Resource.error(
                    UnknownException(
                        error = null,
                        message = "Failed to create address",
                        requestUrl = "/profile/addresses"
                    )
                )
            }
        } catch (e: Exception) {
            Resource.error(
                UnknownException(
                    error = null,
                    message = e.message ?: "Unknown error",
                    requestUrl = "/profile/addresses"
                )
            )
        }
    }

    override suspend fun deleteAddress(addressId: String): Resource<Unit> {
        return try {
            remoteDataSource.deleteAddress(addressId)
            Resource.success(Unit)
        } catch (e: Exception) {
            Resource.error(
                UnknownException(
                    error = null,
                    message = e.message ?: "Unknown error",
                    requestUrl = "/profile/addresses/$addressId"
                )
            )
        }
    }
}

