package com.ptit.domain.repository

import com.ptit.domain.entity.cart.CartItemDetailDomainEntity
import com.ptit.domain.entity.cart.DeleteCartRequestDomainEntity
import com.ptit.domain.entity.cart.DeleteCartResponseDomainEntity
import com.ptit.domain.entity.cart.GetCartDomainEntity
import com.ptit.domain.utils.Resource

interface CartRepository {
    suspend fun getCart(page: Int?, limit: Int?): Resource<GetCartDomainEntity>
    suspend fun addToCart(skuId: String, quantity: Int): Resource<Unit>
    suspend fun updateCartItem(cartItemId: String, skuId: String, quantity: Int): Resource<Unit>
    suspend fun deleteCartItems(request: DeleteCartRequestDomainEntity): Resource<DeleteCartResponseDomainEntity>
    suspend fun getCartItemById(cartItemId: String): Resource<CartItemDetailDomainEntity>
}
