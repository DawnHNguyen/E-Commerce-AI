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

    // State cho Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // State cho dialog - sử dụng một biến duy nhất để kiểm soát tất cả các loại dialog
    var dialogState by remember { mutableStateOf<DialogState>(DialogState.Hidden) }

    // Xử lý update purchase state
    when (val state = updatePurchaseState) {
        is CartViewModel.UpdatePurchaseState.Success -> {
            LaunchedEffect(state) {
                snackbarHostState.showSnackbar(
                    message = "Cập nhật số lượng thành công",
                    duration = SnackbarDuration.Short
                )
            }
        }
        is CartViewModel.UpdatePurchaseState.Error -> {
            LaunchedEffect(state) {
//                snackbarHostState.showSnackbar(
//                    message = state.message,
//                    duration = SnackbarDuration.Short
//                )
            }
        }
        else -> { /* Handle other states if needed */ }
    }

    // Xử lý delete purchase state
    when (val state = deletePurchaseState) {
        is CartViewModel.DeletePurchaseState.Error -> {
            LaunchedEffect(state) {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
            }
        }
        is CartViewModel.DeletePurchaseState.Success -> {
            LaunchedEffect(state) {
                snackbarHostState.showSnackbar(
                    message = "Đã xóa ${state.deletedCount} sản phẩm",
                    duration = SnackbarDuration.Short
                )
            }
        }
        else -> { /* Handle other states if needed */ }
    }

    when (val state = cartState) {
        is CartViewModel.CartState.Loading -> FullScreenProgressBar()

        is CartViewModel.CartState.Error -> {
            MaxSizeBox(
                contentAlignment = Alignment.Center,
                modifier = Modifier.background(color = colorResource(R.color.colorSystem_background_level_0))
            ) {
                Text(
                    text = state.message,
                    style = CustomTypography.TextMedium,
                    color = colorResource(R.color.colorSystem_normal_text)
                )
            }
        }

        is CartViewModel.CartState.Success -> {
            // Sử dụng state trực tiếp từ ViewModel, không cần mutable state riêng
            val purchaseItems = state.purchases

            // Keep track of selected items
            var selectedItemIds by remember { mutableStateOf(setOf<String>()) }
            var isAllSelected by remember(purchaseItems) {
                mutableStateOf(false)
            }

            // Calculate total price based on selected items and their current quantities
            val totalPrice = purchaseItems
                .filter { selectedItemIds.contains(it.id) }
                .sumOf { it.price * it.buyCount }

            // Create custom colors for checkbox
            val customCheckboxColors = CheckboxDefaults.colors(
                checkedColor = colorResource(R.color.colorSystem_heading_button),
                uncheckedColor = colorResource(R.color.colorSystem_greyscale_300),
                checkmarkColor = colorResource(R.color.colorSystem_greyscale_0_white)
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Main content
                CustomPullToRefreshBox(
                    modifier = Modifier.fillMaxSize(),
                    isRefreshing = cartState is CartViewModel.CartState.Loading ||
                            deletePurchaseState is CartViewModel.DeletePurchaseState.Loading,
                    onRefresh = { viewModel.getPurchases() }
                ) {
                    MaxSizeColumn(
                        modifier = Modifier
                            .background(colorResource(R.color.colorSystem_background_level_0))
                    ) {
                        // Top app bar
                        MaxWidthRow(
                            modifier = Modifier
                                .background(color = colorResource(R.color.colorSystem_heading_button))
                                .padding(
                                    vertical = 16.dp,
                                    horizontal = 20.dp
                                )
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

                        if (purchaseItems.isEmpty()) {
                            MaxSizeBox(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Giỏ hàng của bạn đang trống",
                                    style = CustomTypography.TextMedium.merge(
                                        color = colorResource(R.color.colorSystem_normal_text)
                                    ),
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 16.dp
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(purchaseItems, key = { it.id }) { purchase ->
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
                                        onQuantityUpdate = { productId, newQuantity ->
                                            // Update API
                                            viewModel.updatePurchaseQuantity(productId, newQuantity)
                                        },
                                        onDeleteRequest = {
                                            // Chỉ cập nhật dialogState
                                            dialogState = DialogState.DeleteSingleItem(purchase)
                                        }
                                    )
                                }
                            }
                        }

                        // Bottom payment bar
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isAllSelected,
                                            onCheckedChange = { checked ->
                                                isAllSelected = checked
                                                selectedItemIds = if (checked) {
                                                    purchaseItems.map { it.id }.toSet()
                                                } else {
                                                    emptySet()
                                                }
                                            },
                                            colors = customCheckboxColors
                                        )

                                        Text(
                                            text = "Chọn tất cả",
                                            style = CustomTypography.TextMedium,
                                            color = colorResource(R.color.colorSystem_normal_text)
                                        )
                                    }

                                    // Total amount - in a single row
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
                                            text = "${totalPrice} đ",
                                            style = CustomTypography.TextSemiBold.merge(
                                                color = colorResource(R.color.colorSystem_heading_button)
                                            ),
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                // Right side - Buttons row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Delete button
                                    Button(
                                        onClick = {
                                            dialogState = DialogState.DeleteMultipleItems(selectedItemIds)
                                        },
                                        enabled = selectedItemIds.isNotEmpty(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedItemIds.isNotEmpty())
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
                                        enabled = selectedItemIds.isNotEmpty()
                                    )
                                }
                            }
                        }
                    }
                }

                // SnackbarHost positioned at the bottom
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp) // Position above the bottom bar
                )
            }
        }

        else -> {}
    }

    // Xử lý hiển thị dialog dựa trên dialogState
    when (val currentDialog = dialogState) {
        is DialogState.DeleteSingleItem -> {
            AlertDialog(
                onDismissRequest = { dialogState = DialogState.Hidden },
                title = {
                    Text(
                        text = "Xóa sản phẩm",
                        style = CustomTypography.TextBold,
                        fontSize = 18.sp,
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                },
                text = {
                    Text(
                        text = "Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?",
                        style = CustomTypography.TextRegular,
                        color = colorResource(R.color.colorSystem_normal_text),
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val itemId = currentDialog.item.id
                            dialogState = DialogState.Hidden
                            // Chờ một frame để đảm bảo dialog đã đóng hoàn toàn
                            scope.launch {
                                delay(150) // Chờ một lúc để đảm bảo dialog đã đóng
                                viewModel.deletePurchase(itemId)
                            }
                        },
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
                        onClick = { dialogState = DialogState.Hidden },
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
        is DialogState.DeleteMultipleItems -> {
            AlertDialog(
                onDismissRequest = { dialogState = DialogState.Hidden },
                title = {
                    Text(
                        text = "Xóa sản phẩm",
                        style = CustomTypography.TextBold,
                        fontSize = 18.sp,
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                },
                text = {
                    Text(
                        text = "Bạn có chắc chắn muốn xóa ${currentDialog.itemIds.size} sản phẩm đã chọn khỏi giỏ hàng?",
                        style = CustomTypography.TextRegular,
                        color = colorResource(R.color.colorSystem_normal_text),
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val itemIds = currentDialog.itemIds.toList()
                            dialogState = DialogState.Hidden
                            // Chờ một frame để đảm bảo dialog đã đóng hoàn toàn
                            scope.launch {
                                delay(150) // Chờ một lúc để đảm bảo dialog đã đóng
                                viewModel.deletePurchases(itemIds)
                            }
                        },
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
                        onClick = { dialogState = DialogState.Hidden },
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
        DialogState.Hidden -> { /* Không hiển thị dialog */ }
    }
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

    // Sử dụng remember với key để reset khi purchase thay đổi
    var offsetX by remember(purchase.id) { mutableStateOf(0f) }

    // Cleanup khi component bị hủy
    DisposableEffect(purchase.id) {
        onDispose {
            // Reset state when disposed
            offsetX = 0f
        }
    }

    // Threshold để kích hoạt hành động xóa (in dp)
    val deleteThreshold = 100.dp

    // Chuyển đổi threshold sang pixels
    val deleteThresholdPx = with(density) { deleteThreshold.toPx() }

    // Spring animation để trở về mượt mà
    val offsetXAnimated by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(),
        label = "Swipe Animation"
    )

    // State để theo dõi nếu hành động xóa hiển thị
    val isDeleteVisible = offsetXAnimated < -deleteThresholdPx / 2

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Nền xóa (hiển thị khi vuốt)
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

        // Nội dung item giỏ hàng chính
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetXAnimated.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        // Chỉ cho phép kéo sang trái (delta âm) hoặc phục hồi (delta dương khi đã âm)
                        if (delta < 0 || offsetX < 0) {
                            offsetX += delta

                            // Giới hạn khoảng cách kéo
                            if (offsetX < -deleteThresholdPx * 1.5f) {
                                offsetX = -deleteThresholdPx * 1.5f
                            }
                        }
                    },
                    onDragStopped = {
                        // Nếu kéo qua ngưỡng, giữ ở vị trí ngưỡng
                        if (offsetX < -deleteThresholdPx) {
                            offsetX = -deleteThresholdPx
                        } else {
                            // Reset vị trí
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

    // Sử dụng purchase.buyCount trực tiếp để đồng bộ với state cha
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
                onIncrease = {
                    // Cập nhật thông qua callback về cha
                    onQuantityUpdate(product.id, quantity + 1)
                },
                onDecrease = {
                    if (quantity > 1) {
                        // Cập nhật thông qua callback về cha
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
            modifier = Modifier
                .padding(horizontal = 16.dp)
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