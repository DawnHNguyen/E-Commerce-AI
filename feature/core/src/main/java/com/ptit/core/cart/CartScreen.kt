package com.ptit.core.cart

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.*
import com.ptit.common.presentation.component.*
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Sealed class để quản lý trạng thái dialog
sealed class DialogState {
    object Hidden : DialogState()
    data class DeleteSingleItem(val item: PurchaseDomainEntity) : DialogState()
    data class DeleteMultipleItems(val itemIds: Set<String>) : DialogState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCheckout: () -> Unit
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
                ErrorMessage(message = state.message)
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
                    onCheckout = onCheckout
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

@Composable
private fun HandleOperationStates(
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
                // snackbarHostState.showSnackbar(
                //     message = updatePurchaseState.message,
                //     duration = SnackbarDuration.Short
                // )
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

@Composable
private fun ErrorMessage(message: String) {
    MaxSizeBox(
        contentAlignment = Alignment.Center,
        modifier = Modifier.background(color = colorResource(R.color.colorSystem_background_level_0))
    ) {
        Text(
            text = message,
            style = CustomTypography.TextMedium,
            color = colorResource(R.color.colorSystem_normal_text)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartContent(
    purchases: List<PurchaseDomainEntity>,
    onRefresh: () -> Unit,
    isLoading: Boolean,
    onBack: () -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onDeleteSingleItem: (PurchaseDomainEntity) -> Unit,
    onDeleteMultipleItems: (Set<String>) -> Unit,
    onCheckout: () -> Unit
) {
    var selectedItemIds by remember { mutableStateOf(setOf<String>()) }
    var isAllSelected by remember(purchases) { mutableStateOf(false) }
    
    // Calculate total price based on selected items
    val totalPrice = purchases
        .filter { selectedItemIds.contains(it.id) }
        .sumOf { it.price * it.buyCount }
        
    // Custom checkbox colors
    val customCheckboxColors = CheckboxDefaults.colors(
        checkedColor = colorResource(R.color.colorSystem_heading_button),
        uncheckedColor = colorResource(R.color.colorSystem_greyscale_300),
        checkmarkColor = colorResource(R.color.colorSystem_greyscale_0_white)
    )

    CustomPullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isLoading,
        onRefresh = onRefresh
    ) {
        MaxSizeColumn(
            modifier = Modifier.background(colorResource(R.color.colorSystem_background_level_0))
        ) {
            // Top app bar
            CartTopAppBar(onBack = onBack)

            if (purchases.isEmpty()) {
                EmptyCartMessage()
            } else {
                // Cart items list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(purchases, key = { it.id }) { purchase ->
                        CustomSwipeToDeleteCartItem(
                            purchase = purchase,
                            isSelected = selectedItemIds.contains(purchase.id),
                            onSelectionChanged = { isSelected ->
                                selectedItemIds = if (isSelected) {
                                    selectedItemIds + purchase.id
                                } else {
                                    selectedItemIds - purchase.id
                                }
                            },
                            checkboxColors = customCheckboxColors,
                            onQuantityUpdate = onUpdateQuantity,
                            onDeleteRequest = { onDeleteSingleItem(purchase) }
                        )
                    }
                }
            }

            // Bottom payment bar
            CartBottomBar(
                totalPrice = totalPrice,
                isAllSelected = isAllSelected,
                onSelectAllChanged = { checked ->
                    isAllSelected = checked
                    selectedItemIds = if (checked) {
                        purchases.map { it.id }.toSet()
                    } else {
                        emptySet()
                    }
                },
                hasSelectedItems = selectedItemIds.isNotEmpty(),
                onDeleteSelected = { onDeleteMultipleItems(selectedItemIds) },
                onCheckout = onCheckout,
                checkboxColors = customCheckboxColors
            )
        }
    }
}

@Composable
private fun CartTopAppBar(onBack: () -> Unit) {
    MaxWidthRow(
        modifier = Modifier
            .background(color = colorResource(R.color.colorSystem_heading_button))
            .padding(vertical = 16.dp, horizontal = 20.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.noRippleClickable { onBack() },
            tint = colorResource(R.color.colorSystem_greyscale_0_white)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Giỏ hàng",
            style = CustomTypography.TextBold.copy(
                color = colorResource(R.color.colorSystem_greyscale_0_white),
                fontSize = 20.sp
            )
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun EmptyCartMessage() {
    MaxSizeBox(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
            .background(color = colorResource(R.color.colorSystem_background_level_0))
            .padding(16.dp)
    ) {
        Text(
            text = "Giỏ hàng của bạn đang trống",
            style = CustomTypography.TextMedium.merge(
                color = colorResource(R.color.colorSystem_normal_text)
            )
        )
    }
}

@Composable
private fun CartBottomBar(
    totalPrice: Int,
    isAllSelected: Boolean,
    onSelectAllChanged: (Boolean) -> Unit,
    hasSelectedItems: Boolean,
    onDeleteSelected: () -> Unit,
    onCheckout: () -> Unit,
    checkboxColors: CheckboxColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorResource(R.color.colorSystem_greyscale_0_white))
            .padding(16.dp)
    ) {
        MaxWidthRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Select All and Total
            Column {
                // Select All checkbox
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isAllSelected,
                        onCheckedChange = onSelectAllChanged,
                        colors = checkboxColors
                    )

                    Text(
                        text = "Chọn tất cả",
                        style = CustomTypography.TextMedium,
                        color = colorResource(R.color.colorSystem_normal_text)
                    )
                }

                // Total amount
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = "Tổng tiền: ",
                        style = CustomTypography.TextRegular,
                        color = colorResource(R.color.colorSystem_normal_text)
                    )

                    Text(
                        text = "$totalPrice đ",
                        style = CustomTypography.TextSemiBold.merge(
                            color = colorResource(R.color.colorSystem_heading_button)
                        ),
                        fontSize = 16.sp
                    )
                }
            }

            // Right side - Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Delete button
                Button(
                    onClick = onDeleteSelected,
                    enabled = hasSelectedItems,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasSelectedItems)
                            Color(0xFFE53935) // Enabled - deeper red
                        else
                            Color(0xFFFFCDD2), // Disabled - light red
                        disabledContainerColor = Color(0xFFFFCDD2)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Xóa",
                        style = CustomTypography.TextSemiBold,
                        color = Color.White
                    )
                }

                // Checkout button
                FilledButton(
                    text = "Mua hàng",
                    onClick = onCheckout,
                    enabled = hasSelectedItems
                )
            }
        }
    }
}

@Composable
private fun HandleDialogs(
    dialogState: DialogState,
    onDismiss: () -> Unit,
    onConfirmDelete: (List<String>) -> Unit
) {
    when (val currentDialog = dialogState) {
        is DialogState.DeleteSingleItem -> {
            DeleteConfirmationDialog(
                title = "Xóa sản phẩm",
                message = "Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?",
                onConfirm = { onConfirmDelete(listOf(currentDialog.item.id)) },
                onDismiss = onDismiss
            )
        }
        is DialogState.DeleteMultipleItems -> {
            DeleteConfirmationDialog(
                title = "Xóa sản phẩm",
                message = "Bạn có chắc chắn muốn xóa ${currentDialog.itemIds.size} sản phẩm đã chọn khỏi giỏ hàng?",
                onConfirm = { onConfirmDelete(currentDialog.itemIds.toList()) },
                onDismiss = onDismiss
            )
        }
        DialogState.Hidden -> { /* No dialog to show */ }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = CustomTypography.TextBold,
                fontSize = 18.sp,
                color = colorResource(R.color.colorSystem_heading_button)
            )
        },
        text = {
            Text(
                text = message,
                style = CustomTypography.TextRegular,
                color = colorResource(R.color.colorSystem_normal_text),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Yes",
                    style = CustomTypography.TextSemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "No",
                    style = CustomTypography.TextSemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CustomSwipeToDeleteCartItem(
    purchase: PurchaseDomainEntity,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    checkboxColors: CheckboxColors,
    onQuantityUpdate: (String, Int) -> Unit,
    onDeleteRequest: () -> Unit
) {
    val density = LocalDensity.current
    var offsetX by remember(purchase.id) { mutableStateOf(0f) }

    // Clean up state when component is disposed
    DisposableEffect(purchase.id) {
        onDispose { offsetX = 0f }
    }

    // Threshold to trigger delete action (in dp)
    val deleteThreshold = 100.dp
    val deleteThresholdPx = with(density) { deleteThreshold.toPx() }

    // Animate the offset for smooth movement
    val offsetXAnimated by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(),
        label = "Swipe Animation"
    )

    // Track if delete action is visible
    val isDeleteVisible = offsetXAnimated < -deleteThresholdPx / 2

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Delete background (visible when swiped)
        if (isDeleteVisible) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorResource(R.color.colorSystem_background_level_1)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Xóa",
                    color = Color.Red,
                    modifier = Modifier
                        .padding(end = 20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFE8E8))
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                        .noRippleClickable { onDeleteRequest() },
                    style = CustomTypography.TextSemiBold
                )
            }
        }

        // Main cart item content
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetXAnimated.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        // Only allow left drag or recovery
                        if (delta < 0 || offsetX < 0) {
                            offsetX += delta
                            // Limit drag distance
                            if (offsetX < -deleteThresholdPx * 1.5f) {
                                offsetX = -deleteThresholdPx * 1.5f
                            }
                        }
                    },
                    onDragStopped = {
                        // If dragged past threshold, keep at threshold position
                        if (offsetX < -deleteThresholdPx) {
                            offsetX = -deleteThresholdPx
                        } else {
                            // Reset position
                            offsetX = 0f
                        }
                    }
                )
        ) {
            CartItemRow(
                purchase = purchase,
                isSelected = isSelected,
                onSelectionChanged = onSelectionChanged,
                checkboxColors = checkboxColors,
                onQuantityUpdate = onQuantityUpdate
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun CartItemRow(
    purchase: PurchaseDomainEntity,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    checkboxColors: CheckboxColors,
    onQuantityUpdate: (String, Int) -> Unit
) {
    val product = purchase.product
    val quantity = purchase.buyCount

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectionChanged(it) },
                colors = checkboxColors
            )

            Spacer(modifier = Modifier.width(8.dp))

            GlideImage(
                model = product.image,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = CustomTypography.TextRegular.merge(
                        color = colorResource(R.color.colorSystem_normal_text)
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${product.price} đ",
                    style = CustomTypography.TextRegular.copy(
                        color = colorResource(R.color.colorSystem_greyscale_300)
                    ),
                    textDecoration = TextDecoration.LineThrough
                )

                Text(
                    text = "${purchase.price}đ",
                    style = CustomTypography.TextSemiBold.merge(
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                )
            }

            QuantityControl(
                quantity = quantity,
                onIncrease = { onQuantityUpdate(product.id, quantity + 1) },
                onDecrease = { 
                    if (quantity > 1) {
                        onQuantityUpdate(product.id, quantity - 1)
                    }
                }
            )
        }
    }
}

@Composable
private fun QuantityControl(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(id = R.color.colorSystem_background_level_0))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        IconButton(
            onClick = onDecrease,
            enabled = quantity > 1,
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = colorResource(R.color.colorSystem_background_level_1),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Text(
                text = "-",
                style = CustomTypography.TextSemiBold,
                color = colorResource(R.color.colorSystem_heading_button)
            )
        }

        Text(
            text = quantity.toString(),
            style = CustomTypography.TextSemiBold,
            color = colorResource(R.color.colorSystem_heading_button),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        IconButton(
            onClick = onIncrease,
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = colorResource(R.color.colorSystem_background_level_1),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Text(
                text = "+",
                style = CustomTypography.TextSemiBold,
                color = colorResource(R.color.colorSystem_heading_button)
            )
        }
    }
}
