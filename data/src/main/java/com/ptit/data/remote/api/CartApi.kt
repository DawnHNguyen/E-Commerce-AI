package com.ptit.data.remote.api

import com.ptit.data.remote.dto.cart.AddToCartRequestDto
import com.ptit.data.remote.dto.cart.CartItemDetailDto
import com.ptit.data.remote.dto.cart.CartItemDto
import com.ptit.data.remote.dto.cart.DeleteCartRequestDto
import com.ptit.data.remote.dto.cart.DeleteCartResponseDto
import com.ptit.data.remote.dto.cart.GetCartResponseDto
import com.ptit.data.remote.dto.cart.UpdateCartItemRequestDto
import com.ptit.domain.utils.Resource
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CartApi {
    @GET("cart")
    suspend fun getCart(): Resource<GetCartResponseDto>

    @POST("cart")
    suspend fun addToCart(@Body request: AddToCartRequestDto): Resource<CartItemDto>

    @PUT("cart/{cartItemId}")
    suspend fun updateCartItem(
        @Path ("cartItemId") cartItemId: String,
        @Body request: UpdateCartItemRequestDto
    ): Resource<CartItemDto>

    @POST("cart/delete") // Sử dụng POST /cart/delete
    suspend fun deleteCartItems(@Body request: DeleteCartRequestDto): Resource<DeleteCartResponseDto>

    @GET("cart/{cartItemId}")
    suspend fun getCartItemById(@Path("cartItemId") cartItemId: String): Resource<CartItemDetailDto>
}