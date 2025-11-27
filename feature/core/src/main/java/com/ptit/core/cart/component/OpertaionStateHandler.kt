package com.ptit.core.cart.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ptit.core.cart.CartViewModel

@Composable
fun HandleOperationStates(
    updateCartState: CartViewModel.UpdateCartState,
    deleteCartState: CartViewModel.DeleteCartState,
    snackbarHostState: SnackbarHostState
) {
    // ✅ Theo dõi trạng thái cập nhật số lượng
    LaunchedEffect(updateCartState) {
        when (updateCartState) {
            is CartViewModel.UpdateCartState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Cập nhật số lượng thành công",
                    duration = SnackbarDuration.Short
                )
            }
            is CartViewModel.UpdateCartState.Error -> {
                snackbarHostState.showSnackbar(
                    message = updateCartState.message,
                    duration = SnackbarDuration.Short
                )
            }
            else -> {}
        }
    }

    // ✅ Theo dõi trạng thái xóa
    LaunchedEffect(deleteCartState) {
        when (deleteCartState) {
            is CartViewModel.DeleteCartState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Đã xóa ${deleteCartState.deletedCount} sản phẩm khỏi giỏ hàng",
                    duration = SnackbarDuration.Short
                )
            }
            is CartViewModel.DeleteCartState.Error -> {
                snackbarHostState.showSnackbar(
                    message = deleteCartState.message,
                    duration = SnackbarDuration.Short
                )
            }
            else -> {}
        }
    }
}
