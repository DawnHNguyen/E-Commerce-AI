package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.OrderRemoteDataSource
import com.ptit.domain.entity.order.CreateOrderResponseDomainEntity
import com.ptit.domain.entity.order.OrderDomainEntity
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val remoteDataSource: OrderRemoteDataSource
) : OrderRepository {

    override suspend fun createOrder(
        purchaseIds: List<String>,
        fullName: String,
        phone: String,
        address: String,
        note: String,
        totalAmount: Int,
        shippingFee: Int
    ): Resource<CreateOrderResponseDomainEntity> {
        return remoteDataSource.createOrder(
            purchaseIds = purchaseIds,
            fullName = fullName,
            phone = phone,
            address = address,
            shippingFee = shippingFee,
            totalAmount = totalAmount,
            note = note
        ).map { it.toDomainEntity() }
    }

    override suspend fun getOrders(): Resource<List<OrderDomainEntity>> {
        return remoteDataSource.getOrders().map { orderDtos ->
            orderDtos.map { it.toDomainEntity() }
        }
    }

    override suspend fun getOrderById(orderId: String): Resource<OrderDomainEntity> {
        return remoteDataSource.getOrderById(orderId).map { orderDetailResponse ->
            orderDetailResponse.order?.toDomainEntity() ?: OrderDomainEntity()
        }
    }
}