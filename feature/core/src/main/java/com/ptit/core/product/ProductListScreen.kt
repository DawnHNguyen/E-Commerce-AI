package com.ptit.core.product

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeBox
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.ProductEmptyState
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class ProductDialogState {
    object Hidden : ProductDialogState()
    data class DeleteSingleProduct(val product: ProductDomainEntity) : ProductDialogState()
}

@Composable
fun ProductListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductForm: (String?) -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel = hiltViewModel<ProductViewModel>()
    val productList by viewModel.productList.collectAsState()
    val isLoading = rememberState { false }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var dialogState by remember { mutableStateOf<ProductDialogState>(ProductDialogState.Hidden) }

    LaunchedEffect(Unit) {
        viewModel.fetchProductList()

        lifecycleOwner.safeCollectFlow(viewModel.productListState) {
            it
                .onLoading {
                    isLoading.value = true
                }
                .onError { error ->
                    isLoading.value = false
                    Log.e("ProductListScreen", "Error: ${error.message}")
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Không thể tải sản phẩm: ${error.message}",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                .onSuccess { _ ->
                    isLoading.value = false
                }
        }

//        lifecycleOwner.safeCollectFlow(viewModel.deleteProductState) {
//            when (it) {
//                is ProductViewModel.DeleteProductState.Loading -> {
//                    isLoading.value = true
//                }
//                is ProductViewModel.DeleteProductState.Error -> {
//                    isLoading.value = false
//                    scope.launch {
//                        snackbarHostState.showSnackbar(
//                            message = "Không thể xóa sản phẩm: ${it.message}",
//                            duration = SnackbarDuration.Short
//                        )
//                    }
//                }
//                is ProductViewModel.DeleteProductState.Success -> {
//                    isLoading.value = false
//                    scope.launch {
//                        snackbarHostState.showSnackbar(
//                            message = "Đã xóa sản phẩm thành công",
//                            duration = SnackbarDuration.Short
//                        )
//                    }
//                }
//                else -> {}
//            }
//        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.colorSystem_background_level_0))
    ) {
        MaxSizeColumn(
            modifier = Modifier.statusBarsPadding()
        ) {
            // Top App Bar
            ProductsTopAppBar(onBack = onNavigateBack)

            // Content
            if (productList.isEmpty() && !isLoading.value) {
                ProductEmptyState(
                    message = "Chưa có sản phẩm nào",
                    buttonText = "Thêm sản phẩm",
                    onActionClick = { onNavigateToProductForm(null) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    items(productList, key = { it.id }) { product ->
                        ProductItem(
                            product = product,
                            onEditClick = { onNavigateToProductForm(product.id) },
                            onDeleteClick = { dialogState = ProductDialogState.DeleteSingleProduct(product) }
                        )
                    }

                    item {
                        // Extra space at bottom for FAB
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // FAB for adding new product
        FloatingActionButton(
            onClick = { onNavigateToProductForm(null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = colorResource(id = R.color.colorSystem_heading_button),
            contentColor = colorResource(id = R.color.colorSystem_greyscale_0_white),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Thêm sản phẩm",
                modifier = Modifier.size(24.dp)
            )
        }

        // Show loading indicator when processing
        if (isLoading.value) {
            FullScreenProgressBar()
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )

        // Handle delete confirmation dialog
        when (val currentDialog = dialogState) {
            is ProductDialogState.DeleteSingleProduct -> {
                DeleteProductConfirmationDialog(
                    title = "Xóa sản phẩm",
                    message = "Bạn có chắc chắn muốn xóa sản phẩm '${currentDialog.product.name}'?",
                    onConfirm = {
                        dialogState = ProductDialogState.Hidden
                        scope.launch {
                            delay(150) // Wait briefly to ensure dialog has closed
//                            viewModel.deleteProduct(currentDialog.product.id)
                        }
                    },
                    onDismiss = { dialogState = ProductDialogState.Hidden }
                )
            }
            ProductDialogState.Hidden -> { /* No dialog to show */ }
        }
    }
}

@Composable
private fun ProductsTopAppBar(onBack: () -> Unit) {
    MaxWidthRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorResource(R.color.colorSystem_heading_button))
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Quay lại",
                tint = colorResource(id = R.color.colorSystem_greyscale_0_white)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Quản lý sản phẩm",
            style = CustomTypography.TextBold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = colorResource(id = R.color.colorSystem_greyscale_0_white)
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DeleteProductConfirmationDialog(
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
                    text = "Xác nhận",
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
                    text = "Hủy bỏ",
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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProductItem(
    product: ProductDomainEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = colorResource(id = R.color.colorSystem_background_level_1),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                GlideImage(
                    model = product.image,
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                ) {
                    it.centerCrop()
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Product name row
                Text(
                    text = product.name,
                    style = CustomTypography.TextSemiBold,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.colorSystem_heading_button),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Product details row
                Row {
                    // Price
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Giá",
                            style = CustomTypography.TextRegular,
                            fontSize = 12.sp,
                            color = colorResource(id = R.color.colorSystem_normal_text)
                        )
                        Text(
                            text = "${product.price} đ",
                            style = CustomTypography.TextBold,
                            fontSize = 15.sp,
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }

                    // Quantity
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Số lượng",
                            style = CustomTypography.TextRegular,
                            fontSize = 12.sp,
                            color = colorResource(id = R.color.colorSystem_normal_text)
                        )
                        Text(
                            text = "${product.quantity}",
                            style = CustomTypography.TextBold,
                            fontSize = 15.sp,
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Edit button
                    Button(
                        onClick = onEditClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.colorSystem_heading_button)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Sửa",
                            tint = colorResource(id = R.color.colorSystem_greyscale_0_white),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sửa",
                            style = CustomTypography.TextSemiBold,
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.colorSystem_greyscale_0_white)
                        )
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    // Delete button
                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Xóa",
                            style = CustomTypography.TextSemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}