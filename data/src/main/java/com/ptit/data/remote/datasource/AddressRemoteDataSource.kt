package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.AddressService
import com.ptit.data.remote.dto.address.CreateAddressRequest
import javax.inject.Inject

class AddressRemoteDataSource @Inject constructor(
    private val addressService: AddressService
) {
    suspend fun getAddresses() = addressService.getAddresses()

    suspend fun createAddress(
        name: String,
        recipient: String?,
        phoneNumber: String?,
        provinceId: Int,
        districtId: Int,
        wardCode: String,
        street: String,
        addressType: String,
        isDefault: Boolean,
        province: String,
        district: String,
        ward: String
    ) = addressService.createAddress(
        CreateAddressRequest(
            name = name,
            recipient = recipient,
            phoneNumber = phoneNumber,
            provinceId = provinceId,
            districtId = districtId,
            wardCode = wardCode,
            street = street,
            addressType = addressType,
            isDefault = isDefault,
            province = province,
            district = district,
            ward = ward
        )
    )

    suspend fun deleteAddress(addressId: String) = addressService.deleteAddress(addressId)
}

