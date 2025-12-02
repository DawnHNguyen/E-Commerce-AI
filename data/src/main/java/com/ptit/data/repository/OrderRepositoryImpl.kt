package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.mapping.toDto
import com.ptit.data.remote.datasource.OrderRemoteDataSource
import com.ptit.domain.entity.order.CancelOrderResponseDomainEntity
import com.ptit.domain.entity.order.CreateOrderRequestDomainEntity
import com.ptit.domain.entity.order.CreateOrderResponseDomainEntity
import com.ptit.domain.entity.order.GetOrderListDomainEntity
import com.ptit.domain.entity.order.OrderDomainEntity
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val remoteDataSource: OrderRemoteDataSource
) : OrderRepository {

    override suspend fun getOrders(
        page: Int?, 
        limit: Int?, 
        status: String?
    ): Resource<GetOrderListDomainEntity> {
        return remoteDataSource.getOrders(page, limit, status).map { it.toDomainEntity() }
    }
    
    override suspend fun getMyOrders(): Resource<List<OrderDomainEntity>> {
        // Get all orders without pagination
        return remoteDataSource.getOrders(page = 1, limit = 1000, status = null).map { response ->
            response.data?.map { it.toDomainEntity() } ?: emptyList()
        }
    }

    override suspend fun getOrderById(orderId: String): Resource<OrderDomainEntity> {
        return remoteDataSource.getOrderById(orderId).map { it.toDomainEntity() }
    }

    override suspend fun createOrder(
        request: CreateOrderRequestDomainEntity
    ): Resource<CreateOrderResponseDomainEntity> {
        val dtoList = request.toDto()
        return remoteDataSource.createOrder(dtoList).map { it.toDomainEntity() }
    }

    override suspend fun cancelOrder(orderId: String): Resource<CancelOrderResponseDomainEntity> {
        return remoteDataSource.cancelOrder(orderId).map { it.toDomainEntity() }
    }
}

