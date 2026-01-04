package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
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
            // 1. Gọi API lấy Root Response
            val rootResponse = remoteDataSource.getAddresses()

            // 2. Đi sâu vào trong: Root -> Data Wrapper -> Address List
            val addressList = rootResponse.data?.addresses

            if (addressList != null) {
                // 3. Map sang Domain Entity và trả về Success
                val domainList = addressList.map { it.toDomainEntity() }
                Resource.Success(domainList)
            } else {
                // Trường hợp null (có thể do lỗi backend hoặc list rỗng nhưng trả về null)
                // Bạn có thể trả về Success với list rỗng hoặc Error tùy logic
                Resource.Success(emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 4. Trả về Error đúng chuẩn Resource.Error(CustomException)
            Resource.Error(
                UnknownException(
                    error = null,
                    message = e.message ?: "Lỗi kết nối hoặc parse data",
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
        isDefault: Boolean,
        provinceName: String,
        districtName: String,
        wardName: String
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
                isDefault = isDefault,
                province = provinceName,
                district = districtName,
                ward = wardName
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

