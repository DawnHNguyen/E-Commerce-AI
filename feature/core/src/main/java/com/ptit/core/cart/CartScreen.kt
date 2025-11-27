package com.ptit.core.cart

import DialogState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.core.cart.components.*
import com.ptit.domain.entity.cart.CartItemDetailDomainEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: (List<String>, List<CartItemDetailDomainEntity>) -> Unit,
    onProductClick: (String) -> Unit = {}
) {
    // ✅ Hiển thị bottom navigation
    LocalBottomNavigationVisibility.current.value = true

    // ✅ ViewModel
    val viewModel: CartViewModel = hiltViewModel()
    val cartState by viewModel.cartState.collectAsState()
    val updateState by viewModel.updateCartState.collectAsState()
    val deleteState by viewModel.deleteCartState.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var dialogState by remember { mutableStateOf<DialogState>(DialogState.Hidden) }

    // ✅ Hiển thị snackbar khi có update/delete
    HandleOperationStates(
        updateCartState = updateState,
        deleteCartState = deleteState,
        snackbarHostState = snackbarHostState
    )

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = cartState) {
            is CartViewModel.CartState.Loading -> {
                FullScreenProgressBar()
            }

            is CartViewModel.CartState.Error -> {
                // TODO: hiển thị UI lỗi nếu cần
            }

            is CartViewModel.CartState.Success -> {
                val flatItems = remember(state.groupedItems) {
                    viewModel.getFlatCartItems(state.groupedItems)
                }
                CartContent(
                    // ✅ Danh sách sản phẩm trong giỏ
                    purchases = flatItems, // state.items là List<CartItemDomainEntity>

                    // ✅ Làm mới danh sách
                    onRefresh = { viewModel.getCart() },

                    // ✅ Hiển thị loading khi đang xóa hoặc tải
                    isLoading = cartState is CartViewModel.CartState.Loading ||
                            deleteState is CartViewModel.DeleteCartState.Loading,

                    // ✅ Quay lại màn hình trước
                    onBack = onBack,

                    // ✅ Cập nhật số lượng sản phẩm
                    onUpdateQuantity = { cartItemId, skuId, newQty ->
                        viewModel.updateCartItemQuantity(cartItemId, skuId, newQty)
                    },

                    // ✅ Xóa 1 sản phẩm
                    onDeleteSingleItem = { cartItem ->
                        dialogState = DialogState.DeleteSingleItem(item = cartItem)
                    },

                    // ✅ Xóa nhiều sản phẩm
                    onDeleteMultipleItems = { ids ->
                        dialogState = DialogState.DeleteMultipleItems(itemIds = ids)
                    },

                    // ✅ Thanh toán danh sách sản phẩm đã chọn
                    onCheckout = { selectedIds ->
                        onCheckout(selectedIds.toList(), state.groupedItems)
                    },

                    // ✅ Xem chi tiết sản phẩm
                    onProductClick = onProductClick
                )
            }
        }

        // ✅ Snackbar hiển thị dưới cùng màn hình
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }

    // ✅ Xử lý hiển thị dialog xác nhận xóa
    HandleDialogs(
        dialogState = dialogState,
        onDismiss = { dialogState = DialogState.Hidden },
        onConfirmDelete = { ids ->
            dialogState = DialogState.Hidden
            scope.launch {
                delay(150) // tránh xung đột animation
                viewModel.deleteCartItems(ids.toList())
            }
        }
    )
}
