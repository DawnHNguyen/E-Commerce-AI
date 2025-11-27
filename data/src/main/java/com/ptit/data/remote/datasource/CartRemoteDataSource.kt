package com.ptit.data.remote.datasource

import com.ptit.data.remote.api.CartApi
import com.ptit.data.remote.dto.cart.AddToCartRequestDto
import com.ptit.data.remote.dto.cart.DeleteCartRequestDto
import com.ptit.data.remote.dto.cart.UpdateCartItemRequestDto
import javax.inject.Inject

class CartRemoteDataSource @Inject constructor(private val api: CartApi) {

    suspend fun getCart() = api.getCart()

    suspend fun addToCart(skuId: String, quantity: Int) =
        api.addToCart(AddToCartRequestDto(skuId, quantity))

    suspend fun updateCartItem(cartItemId: String, skuId: String, quantity: Int) =
        api.updateCartItem(cartItemId,UpdateCartItemRequestDto(skuId, quantity))

    suspend fun deleteCartItems(cartItemIds: List<String>) =
        api.deleteCartItems(DeleteCartRequestDto(cartItemIds))

    suspend fun getCartItemById(cartItemId: String) = api.getCartItemById(cartItemId)
}