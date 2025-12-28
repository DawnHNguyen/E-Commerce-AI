package com.ptit.domain.repository

import com.ptit.domain.entity.address.AddressDomainEntity
import com.ptit.domain.utils.Resource

interface AddressRepository {
    suspend fun getAddresses(): Resource<List<AddressDomainEntity>>
    
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
        provinceName: String,
        districtName: String,
        wardName: String
    ): Resource<AddressDomainEntity>
    
    suspend fun deleteAddress(addressId: String): Resource<Unit>
}

