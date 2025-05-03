package com.ptit.core.cart

import DialogState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.core.cart.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCheckout: (List<String>) -> Unit, // Modified to accept IDs instead of PurchaseDomainEntity objects
    onProductClick: (String) -> Unit = {}
) {
    val cartState by viewModel.cartState.collectAsState()
    val updatePurchaseState by viewModel.updatePurchaseState.collectAsState()
    val deletePurchaseState by viewModel.deletePurchaseState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var dialogState by remember { mutableStateOf<DialogState>(DialogState.Hidden) }

    // Handle operation states and show appropriate snackbars
    HandleOperationStates(
        updatePurchaseState = updatePurchaseState,
        deletePurchaseState = deletePurchaseState,
        snackbarHostState = snackbarHostState
    )

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = cartState) {
            is CartViewModel.CartState.Loading -> FullScreenProgressBar()

            is CartViewModel.CartState.Error -> {
                //ErrorMessage(message = state.message)
            }

            is CartViewModel.CartState.Success -> {
                CartContent(
                    purchases = state.purchases,
                    onRefresh = { viewModel.getPurchases() },
                    isLoading = cartState is CartViewModel.CartState.Loading ||
                            deletePurchaseState is CartViewModel.DeletePurchaseState.Loading,
                    onBack = onBack,
                    onUpdateQuantity = { productId, newQuantity ->
                        viewModel.updatePurchaseQuantity(productId, newQuantity)
                    },
                    onDeleteSingleItem = { purchase ->
                        dialogState = DialogState.DeleteSingleItem(purchase)
                    },
                    onDeleteMultipleItems = { selectedIds ->
                        dialogState = DialogState.DeleteMultipleItems(selectedIds)
                    },
                    onCheckout = { selectedIds ->
                        onCheckout(selectedIds.toList()) // Convert Set<String> to List<String>
                    },
                    onProductClick = onProductClick
                )
            }

            else -> {}
        }

        // SnackbarHost positioned at the bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp) // Position above the bottom bar
        )
    }

    // Handle dialog displays based on dialogState
    HandleDialogs(
        dialogState = dialogState,
        onDismiss = { dialogState = DialogState.Hidden },
        onConfirmDelete = { itemIds ->
            dialogState = DialogState.Hidden
            scope.launch {
                delay(150) // Wait briefly to ensure dialog has closed
                if (itemIds.size == 1) {
                    viewModel.deletePurchase(itemIds.first())
                } else {
                    viewModel.deletePurchases(itemIds.toList())
                }
            }
        }
    )
}