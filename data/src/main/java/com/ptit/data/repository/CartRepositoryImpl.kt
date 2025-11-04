package com.ptit.data.repository

import com.ptit.data.mapper.cart.toDomainEntity
import com.ptit.data.remote.datasource.CartRemoteDataSource
import com.ptit.domain.entity.cart.CartItemDetailDomainEntity
import com.ptit.domain.entity.cart.DeleteCartRequestDomainEntity
import com.ptit.domain.entity.cart.DeleteCartResponseDomainEntity
import com.ptit.domain.entity.cart.GetCartDomainEntity
import com.ptit.domain.repository.CartRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val remoteDataSource: CartRemoteDataSource
) : CartRepository {

    override suspend fun getCart(page: Int?, limit: Int?): Resource<GetCartDomainEntity> {
        return remoteDataSource.getCart().map { it.toDomainEntity() }
    }

    override suspend fun addToCart(skuId: String, quantity: Int): Resource<Unit> {
        return remoteDataSource.addToCart(skuId, quantity).map { Unit }
    }

    override suspend fun updateCartItem(cartItemId: String, skuId: String, quantity: Int): Resource<Unit> {
        return remoteDataSource.updateCartItem(cartItemId, skuId, quantity).map { Unit }
    }

    override suspend fun deleteCartItems(
        request: DeleteCartRequestDomainEntity
    ): Resource<DeleteCartResponseDomainEntity> {
        return remoteDataSource
            .deleteCartItems(request.cartItemIds)
            .map { it.toDomainEntity() }
    }

    override suspend fun getCartItemById(cartItemId: String): Resource<CartItemDetailDomainEntity> {
        return remoteDataSource.getCartItemById(cartItemId).map { it.toDomainEntity() }
    }
}