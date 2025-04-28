package com.ptit.core.cart.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ptit.core.cart.CartViewModel

@Composable
fun HandleOperationStates(
    updatePurchaseState: CartViewModel.UpdatePurchaseState,
    deletePurchaseState: CartViewModel.DeletePurchaseState,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(updatePurchaseState) {
        when (updatePurchaseState) {
            is CartViewModel.UpdatePurchaseState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Cập nhật số lượng thành công",
                    duration = SnackbarDuration.Short
                )
            }
            is CartViewModel.UpdatePurchaseState.Error -> {
                // Uncomment if needed
                 snackbarHostState.showSnackbar(
                     message = updatePurchaseState.message,
                     duration = SnackbarDuration.Short
                 )
            }
            else -> {}
        }
    }

    LaunchedEffect(deletePurchaseState) {
        when (deletePurchaseState) {
            is CartViewModel.DeletePurchaseState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Đã xóa ${deletePurchaseState.deletedCount} sản phẩm",
                    duration = SnackbarDuration.Short
                )
            }
            is CartViewModel.DeletePurchaseState.Error -> {
                snackbarHostState.showSnackbar(
                    message = deletePurchaseState.message,
                    duration = SnackbarDuration.Short
                )
            }
            else -> {}
        }
    }
}