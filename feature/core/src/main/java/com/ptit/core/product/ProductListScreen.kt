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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.ProductEmptyState
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.domain.entity.brand.BrandDomainEntity
import com.ptit.domain.entity.product.CategoryDomainEntity
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductForm: (String?) -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel = hiltViewModel<ProductViewModel>()
    val productList by viewModel.productList.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val brands by viewModel.brands.collectAsState()
    val isLoading = rememberState { false }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var dialogState by remember { mutableStateOf<ProductDialogState>(ProductDialogState.Hidden) }

    // Search and Filter states
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedBrandId by remember { mutableStateOf<String?>(null) }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }

    // Reload products when screen is displayed (including when returning from form)
    androidx.compose.runtime.DisposableEffect(Unit) {
        viewModel.fetchUserIdAndProducts()
        onDispose { }
    }

    LaunchedEffect(Unit) {
        viewModel.getCategories()
        viewModel.getBrands()

        lifecycleOwner.safeCollectFlow(viewModel.userProfileState) {
            it
                .onLoading {
                    isLoading.value = true
                }
                .onError { error ->
                    isLoading.value = false
                    Log.e("ProductListScreen", "Error fetching user profile: ${error.message}")
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Không thể tải thông tin người dùng: ${error.message}",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                .onSuccess { profile ->
                    isLoading.value = false
                    profile?.let { userId = it.id }
                }
        }
    }

    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.productListState) {
            it
                .onLoading {
                    isLoading.value = true
                }
                .onError { error ->
                    isLoading.value = false
                    Log.e("ProductListScreen", "Error fetching products: ${error.message}")
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
    }

    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.deleteProductState) { state ->
            state
                .onLoading {
                    isLoading.value = true
                }
                .onError { error ->
                    isLoading.value = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Không thể xóa sản phẩm: ${error.message}",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                .onSuccess { _ ->
                    isLoading.value = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Xóa sản phẩm thành công",
                            duration = SnackbarDuration.Short
                        )
                    }
                    // Refresh product list after successful delete
                    viewModel.fetchUserIdAndProducts()
                }
        }
    }

    Scaffold(
        topBar = {
            ProductsTopAppBar(
                onBack = onNavigateBack,
                searchQuery = searchQuery,
                onSearchQueryChange = {
                    searchQuery = it
                    if (userId.isNotEmpty()) {
                        viewModel.fetchProductList(
                            createdById = userId,
                            searchQuery = it.ifBlank { null },
                            minPrice = minPrice.toIntOrNull(),
                            maxPrice = maxPrice.toIntOrNull(),
                            categoryId = selectedCategoryId,
                            brandId = selectedBrandId
                        )
                    }
                },
                onFilterClick = { showFilterDialog = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToProductForm(null) },
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
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = colorResource(R.color.colorSystem_background_level_0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Content
            if (productList.isEmpty() && !isLoading.value) {
                ProductEmptyState(
                    message = "Chưa có sản phẩm nào",
                    buttonText = "Thêm sản phẩm",
                    onActionClick = { onNavigateToProductForm(null) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(productList, key = { it.id }) { product ->
                        ProductItem(
                            product = product,
                            onEditClick = { onNavigateToProductForm(product.id) },
                            onDeleteClick = { dialogState = ProductDialogState.DeleteSingleProduct(product) }
                        )
                    }
                }
            }

            // Show loading indicator when processing
            if (isLoading.value) {
                FullScreenProgressBar()
            }
        }

        // Handle delete confirmation dialog
        when (val currentDialog = dialogState) {
            is ProductDialogState.DeleteSingleProduct -> {
                DeleteProductConfirmationDialog(
                    title = "Xóa sản phẩm",
                    message = "Bạn có chắc chắn muốn xóa sản phẩm '${currentDialog.product.name}'?",
                    onConfirm = {
                        dialogState = ProductDialogState.Hidden
                        scope.launch {
                            delay(150)
                            viewModel.deleteProduct(currentDialog.product.id)
                        }
                    },
                    onDismiss = { dialogState = ProductDialogState.Hidden }
                )
            }
            ProductDialogState.Hidden -> { /* No dialog to show */ }
        }

        // Filter dialog
        if (showFilterDialog) {
            FilterDialog(
                showDialog = showFilterDialog,
                onDismiss = { showFilterDialog = false },
                categories = categories,
                brands = brands,
                selectedCategoryId = selectedCategoryId,
                selectedBrandId = selectedBrandId,
                minPrice = minPrice,
                maxPrice = maxPrice,
                onCategorySelected = { selectedCategoryId = it },
                onBrandSelected = { selectedBrandId = it },
                onMinPriceChange = { minPrice = it },
                onMaxPriceChange = { maxPrice = it },
                onApplyFilter = {
                    showFilterDialog = false
                    viewModel.fetchProductList(
                        createdById = userId,
                        searchQuery = searchQuery.ifBlank { null },
                        minPrice = minPrice.toIntOrNull(),
                        maxPrice = maxPrice.toIntOrNull(),
                        categoryId = selectedCategoryId,
                        brandId = selectedBrandId
                    )
                },
                onResetFilter = {
                    minPrice = ""
                    maxPrice = ""
                    selectedCategoryId = null
                    selectedBrandId = null
                    viewModel.fetchProductList(
                        createdById = userId,
                        searchQuery = searchQuery.ifBlank { null },
                        minPrice = null,
                        maxPrice = null,
                        categoryId = null,
                        brandId = null
                    )
                }
            )
        }
    }
}

@Composable
private fun ProductsTopAppBar(
    onBack: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = colorResource(R.color.colorSystem_heading_button),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Title row with back button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = colorResource(id = R.color.colorSystem_greyscale_0_white),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Quản lý sản phẩm",
                    style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                    color = colorResource(id = R.color.colorSystem_greyscale_0_white),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search and Filter row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search TextField
                androidx.compose.material3.OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Tìm kiếm sản phẩm...",
                            style = CustomTypography.TextRegular,
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.colorSystem_greyscale_0_white).copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colorResource(id = R.color.colorSystem_greyscale_0_white).copy(alpha = 0.7f)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = colorResource(id = R.color.colorSystem_greyscale_0_white).copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorResource(id = R.color.colorSystem_greyscale_0_white),
                        unfocusedTextColor = colorResource(id = R.color.colorSystem_greyscale_0_white),
                        focusedBorderColor = colorResource(id = R.color.colorSystem_greyscale_0_white),
                        unfocusedBorderColor = colorResource(id = R.color.colorSystem_greyscale_0_white).copy(alpha = 0.5f),
                        cursorColor = colorResource(id = R.color.colorSystem_greyscale_0_white),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    textStyle = CustomTypography.TextRegular.copy(fontSize = 14.sp)
                )

                // Filter Button
                androidx.compose.material3.Surface(
                    onClick = onFilterClick,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colorResource(id = R.color.colorSystem_greyscale_0_white).copy(alpha = 0.2f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = colorResource(id = R.color.colorSystem_greyscale_0_white),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
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

@Composable
private fun FilterDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    categories: List<com.ptit.domain.entity.product.CategoryDomainEntity>,
    brands: List<com.ptit.domain.entity.brand.BrandDomainEntity>,
    selectedCategoryId: String?,
    selectedBrandId: String?,
    minPrice: String,
    maxPrice: String,
    onCategorySelected: (String?) -> Unit,
    onBrandSelected: (String?) -> Unit,
    onMinPriceChange: (String) -> Unit,
    onMaxPriceChange: (String) -> Unit,
    onApplyFilter: () -> Unit,
    onResetFilter: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Lọc sản phẩm",
                    style = CustomTypography.TextBold,
                    fontSize = 20.sp,
                    color = colorResource(R.color.colorSystem_heading_button)
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Price Range
                    item {
                        Column {
                            Text(
                                text = "Khoảng giá",
                                style = CustomTypography.TextBold,
                                fontSize = 16.sp,
                                color = colorResource(R.color.colorSystem_heading_button)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                androidx.compose.material3.OutlinedTextField(
                                    value = minPrice,
                                    onValueChange = onMinPriceChange,
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Giá thấp nhất") },
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    )
                                )
                                androidx.compose.material3.OutlinedTextField(
                                    value = maxPrice,
                                    onValueChange = onMaxPriceChange,
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Giá cao nhất") },
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    )
                                )
                            }
                        }
                    }

                    // Category Filter
                    item {
                        Column {
                            Text(
                                text = "Danh mục",
                                style = CustomTypography.TextBold,
                                fontSize = 16.sp,
                                color = colorResource(R.color.colorSystem_heading_button)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            var expandedCategory by remember { mutableStateOf(false) }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                androidx.compose.material3.OutlinedTextField(
                                    value = categories.find { it.id == selectedCategoryId }?.name ?: "Tất cả danh mục",
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { expandedCategory = !expandedCategory }) {
                                            Icon(
                                                imageVector = if (expandedCategory)
                                                    androidx.compose.material.icons.Icons.Default.ArrowDropUp
                                                else
                                                    androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                                                contentDescription = "Dropdown"
                                            )
                                        }
                                    }
                                )

                                androidx.compose.material3.DropdownMenu(
                                    expanded = expandedCategory,
                                    onDismissRequest = { expandedCategory = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("Tất cả danh mục") },
                                        onClick = {
                                            onCategorySelected(null)
                                            expandedCategory = false
                                        }
                                    )
                                    categories.forEach { category ->
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(category.name) },
                                            onClick = {
                                                onCategorySelected(category.id)
                                                expandedCategory = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Brand Filter
                    item {
                        Column {
                            Text(
                                text = "Thương hiệu",
                                style = CustomTypography.TextBold,
                                fontSize = 16.sp,
                                color = colorResource(R.color.colorSystem_heading_button)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            var expandedBrand by remember { mutableStateOf(false) }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                androidx.compose.material3.OutlinedTextField(
                                    value = brands.find { it.id == selectedBrandId }?.name ?: "Tất cả thương hiệu",
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { expandedBrand = !expandedBrand }) {
                                            Icon(
                                                imageVector = if (expandedBrand)
                                                    androidx.compose.material.icons.Icons.Default.ArrowDropUp
                                                else
                                                    androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                                                contentDescription = "Dropdown"
                                            )
                                        }
                                    }
                                )

                                androidx.compose.material3.DropdownMenu(
                                    expanded = expandedBrand,
                                    onDismissRequest = { expandedBrand = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("Tất cả thương hiệu") },
                                        onClick = {
                                            onBrandSelected(null)
                                            expandedBrand = false
                                        }
                                    )
                                    brands.forEach { brand ->
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(brand.name) },
                                            onClick = {
                                                onBrandSelected(brand.id)
                                                expandedBrand = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onApplyFilter,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.colorSystem_heading_button)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Áp dụng",
                        style = CustomTypography.TextSemiBold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                Row {
                    Button(
                        onClick = onResetFilter,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Đặt lại",
                            style = CustomTypography.TextSemiBold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Hủy",
                            style = CustomTypography.TextSemiBold,
                            color = Color.White
                        )
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProductItem(
    product: ProductDomainEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
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
                    model = product.images.firstOrNull(),
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                ) {
                    it.centerCrop()
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Product Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = CustomTypography.TextSemiBold,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.colorSystem_heading_button),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${product.basePrice} đ",
                    style = CustomTypography.TextBold,
                    fontSize = 15.sp,
                    color = colorResource(id = R.color.colorSystem_heading_button)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Kho: ${product.skus.sumOf { it.stock }}",
                    style = CustomTypography.TextRegular,
                    fontSize = 13.sp,
                    color = colorResource(id = R.color.colorSystem_normal_text)
                )
            }

            // Three dots menu button
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = colorResource(id = R.color.colorSystem_normal_text)
                    )
                }

                androidx.compose.material3.DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Sửa",
                                    tint = colorResource(id = R.color.colorSystem_heading_button),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Sửa",
                                    style = CustomTypography.TextSemiBold,
                                    fontSize = 14.sp,
                                    color = colorResource(id = R.color.colorSystem_heading_button)
                                )
                            }
                        },
                        onClick = {
                            showMenu = false
                            onEditClick()
                        }
                    )

                    androidx.compose.material3.DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Xóa",
                                    tint = Color.Red,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Xóa",
                                    style = CustomTypography.TextSemiBold,
                                    fontSize = 14.sp,
                                    color = Color.Red
                                )
                            }
                        },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}