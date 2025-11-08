package com.ptit.data.repository


import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.ShippingRemoteDataSource
import com.ptit.domain.entity.shipping.DistrictEntity
import com.ptit.domain.entity.shipping.ProvinceEntity
import com.ptit.domain.entity.shipping.WardEntity
import com.ptit.domain.repository.ShippingRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import javax.inject.Inject

class ShippingRepositoryImpl @Inject constructor(
    private val remoteDataSource: ShippingRemoteDataSource // 🔴 SỬA: Inject DataSource
) : ShippingRepository {

    override suspend fun getProvinces(): Resource<List<ProvinceEntity>> {
        // 🔴 SỬA: Gọi qua DataSource
        return remoteDataSource.getProvinces().map { list -> list.map { it.toDomainEntity() } }
    }

    override suspend fun getDistricts(provinceId: Int): Resource<List<DistrictEntity>> {
        // 🔴 SỬA: Gọi qua DataSource
        return remoteDataSource.getDistricts(provinceId).map { list -> list.map { it.toDomainEntity() } }
    }

    override suspend fun getWards(districtId: Int): Resource<List<WardEntity>> {
        // 🔴 SỬA: Gọi qua DataSource
        return remoteDataSource.getWards(districtId).map { list -> list.map { it.toDomainEntity() } }
    }
}