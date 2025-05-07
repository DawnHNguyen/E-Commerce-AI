package com.ptit.core.product

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess
import kotlinx.coroutines.launch

@Composable
fun ProductForm(
    onNavigateBack: () -> Unit,
    productId: String? = null
) {
    Log.d("ProductForm", "ProductForm started with productId: $productId")
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel = hiltViewModel<ProductViewModel>()
    val isLoading = rememberState { false }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Form states
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var priceBeforeDiscount by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("") }

    // Image handling
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var uploadedImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }

    // Determine if we're in edit mode
    val isEditMode = productId != null

    // Load product details if in edit mode
    LaunchedEffect(productId) {
        if (productId != null) {
            viewModel.getProductDetails(productId)
        }
    }

    // Observe product details state for edit mode
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.productDetailsState) {
            it.onLoading {
                isLoading.value = true
            }.onError { error ->
                isLoading.value = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Lỗi tải thông tin sản phẩm: ${error.message}",
                        duration = SnackbarDuration.Short
                    )
                }
            }.onSuccess { product ->
                isLoading.value = false
                product?.let {
                    // Populate form with existing product data
                    name = it.name
                    description = it.description
                    price = it.price.toString()
                    priceBeforeDiscount = it.priceBeforeDiscount.toString()
                    quantity = it.quantity.toString()
                    categoryId = it.category?.id ?: ""
                    uploadedImageUrls = it.images
                }
            }
        }
    }

    // Image picker
    val multipleImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (selectedImageUris.size < 5) {
                selectedImageUris = selectedImageUris + it
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Tối đa 5 hình ảnh")
                }
            }
        }
    }

    // Load categories when form is displayed
    LaunchedEffect(Unit) {
        viewModel.getCategories()
    }

    // Observe image upload state
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.uploadImagesState) {
            it.onLoading {
                isLoading.value = true
            }.onError { error ->
                isLoading.value = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Lỗi tải ảnh: ${error.message}",
                        duration = SnackbarDuration.Short
                    )
                }
            }.onSuccess { urls ->
                isLoading.value = false
                uploadedImageUrls = urls
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Đã tải lên ${urls.size} hình ảnh",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // Observe save product state
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.saveProductState) {
            it.onLoading {
                isLoading.value = true
            }.onError { error ->
                isLoading.value = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Lỗi: ${error.message}",
                        duration = SnackbarDuration.Short
                    )
                }
            }.onSuccess { product ->
                isLoading.value = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Đã tạo sản phẩm thành công",
                        duration = SnackbarDuration.Short
                    )
                }
                onNavigateBack()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.colorSystem_background_level_0))
    ) {
        MaxSizeColumn(
            modifier = Modifier
                .statusBarsPadding()
                .padding(bottom = 80.dp)
        ) {
            // Top App Bar
            MaxWidthRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colorResource(R.color.colorSystem_heading_button))
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onNavigateBack,
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
                    text = if(isEditMode) "Cập nhật sản phẩm" else "Tạo sản phẩm mới",
                    style = CustomTypography.TextBold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = colorResource(id = R.color.colorSystem_greyscale_0_white)
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            // Form content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (!isEditMode) {
                    // Product Images Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Hình ảnh sản phẩm",
                                style = CustomTypography.TextSemiBold,
                                fontSize = 18.sp,
                                color = colorResource(id = R.color.colorSystem_heading_button)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Main product image - shows first image selected
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .border(
                                        width = 1.dp,
                                        color = colorResource(id = R.color.colorSystem_stroke),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colorResource(id = R.color.colorSystem_background_level_1))
                                    .clickable { multipleImagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedImageUris.isNotEmpty()) {
                                    MainProductImage(imageUri = selectedImageUris.first())
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Outlined.Photo,
                                            contentDescription = "Add Image",
                                            tint = colorResource(id = R.color.colorSystem_normal_text),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "Thêm hình ảnh",
                                            style = CustomTypography.TextRegular,
                                            color = colorResource(id = R.color.colorSystem_normal_text)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Additional images
                            Text(
                                text = "Thêm hình ảnh sản phẩm (tối đa 5 hình)",
                                style = CustomTypography.TextMedium,
                                color = colorResource(id = R.color.colorSystem_normal_text)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                // Selected images
                                items(selectedImageUris) { imageUri ->
                                    ProductImageItem(
                                        imageUri = imageUri,
                                        onRemove = {
                                            selectedImageUris =
                                                selectedImageUris.filter { it != imageUri }
                                        }
                                    )
                                }

                                // Add image button (if less than 5 images)
                                if (selectedImageUris.size < 5) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .border(
                                                    width = 1.dp,
                                                    color = colorResource(id = R.color.colorSystem_stroke),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colorResource(id = R.color.colorSystem_background_level_1))
                                                .clickable { multipleImagePicker.launch("image/*") },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add Image",
                                                tint = colorResource(id = R.color.colorSystem_normal_text)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Product Details Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Thông tin sản phẩm",
                            style = CustomTypography.TextSemiBold,
                            fontSize = 18.sp,
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Name field
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Tên sản phẩm") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(id = R.color.colorSystem_heading_button),
                                unfocusedBorderColor = colorResource(id = R.color.colorSystem_stroke)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category dropdown field
                        CategorySelectField(
                            selectedCategoryId = categoryId,
                            onCategorySelected = { categoryId = it },
                            viewModel = viewModel
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Description field
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Mô tả sản phẩm") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            singleLine = false,
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(id = R.color.colorSystem_heading_button),
                                unfocusedBorderColor = colorResource(id = R.color.colorSystem_stroke)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Price fields
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Regular price
                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it.filter { char -> char.isDigit() } },
                                label = { Text("Giá (VNĐ)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorResource(id = R.color.colorSystem_heading_button),
                                    unfocusedBorderColor = colorResource(id = R.color.colorSystem_stroke)
                                )
                            )

                            // Original price (before discount)
                            OutlinedTextField(
                                value = priceBeforeDiscount,
                                onValueChange = { priceBeforeDiscount = it.filter { char -> char.isDigit() } },
                                label = { Text("Giá gốc (VNĐ)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorResource(id = R.color.colorSystem_heading_button),
                                    unfocusedBorderColor = colorResource(id = R.color.colorSystem_stroke)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quantity field
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it.filter { char -> char.isDigit() } },
                            label = { Text("Số lượng") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(id = R.color.colorSystem_heading_button),
                                unfocusedBorderColor = colorResource(id = R.color.colorSystem_stroke)
                            )
                        )
                    }
                }
            }
        }

        // Save button (fixed at bottom)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    // Validate form
                    when {
                        name.isBlank() -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng nhập tên sản phẩm")
                            }
                        }
                        categoryId.isBlank() -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng nhập mã danh mục")
                            }
                        }
                        price.isBlank() || price.toIntOrNull() == null -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng nhập giá hợp lệ")
                            }
                        }
                        quantity.isBlank() || quantity.toIntOrNull() == null -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng nhập số lượng hợp lệ")
                            }
                        }
                        selectedImageUris.isEmpty() && uploadedImageUrls.isEmpty() -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng thêm ít nhất một hình ảnh")
                            }
                        }
                        else -> {
                            // First upload images if not already uploaded
                            if (selectedImageUris.isNotEmpty() && uploadedImageUrls.isEmpty()) {
                                // Upload images first
                                viewModel.uploadProductImages(selectedImageUris)
                            } else if (uploadedImageUrls.isNotEmpty()) {
                                if (isEditMode && productId != null) {
                                    viewModel.updateProduct(
                                        productId = productId,
                                        name = name,
                                        description = description,
                                        price = price.toIntOrNull() ?: 0,
                                        priceBeforeDiscount = priceBeforeDiscount.toIntOrNull() ?: 0,
                                        quantity = quantity.toIntOrNull() ?: 0,
                                        imageFiles = uploadedImageUrls,
                                        category = categoryId
                                    )
                                } else {
                                    viewModel.createProduct(
                                        name = name,
                                        description = description,
                                        price = price.toIntOrNull() ?: 0,
                                        priceBeforeDiscount = priceBeforeDiscount.toIntOrNull() ?: 0,
                                        quantity = quantity.toIntOrNull() ?: 0,
                                        imageFiles = uploadedImageUrls,
                                        category = categoryId
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.colorSystem_heading_button)
                )
            ) {
                Text(
                    text = when {
                        selectedImageUris.isNotEmpty() && uploadedImageUrls.isEmpty() -> "Tải ảnh lên"
                        isEditMode -> "Cập nhập sản phẩm"
                        else -> "Tạo sản phẩm"
                    },
                    style = CustomTypography.TextSemiBold,
                    fontSize = 16.sp
                )
            }
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
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun MainProductImage(imageUri: Uri) {
    GlideImage(
        model = imageUri,
        contentDescription = "Product Image",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    ) {
        it.centerCrop()
    }
}

@Composable
fun CategorySelectField(
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit,
    viewModel: ProductViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val categoryState by viewModel.categoryListState.collectAsStateWithLifecycle()
    var categoriesList by remember { mutableStateOf<List<CategoryDomainEntity>>(emptyList()) }
    var selectedCategoryName by remember { mutableStateOf("") }

    // Update categories list when state changes
    LaunchedEffect(categoryState) {
        categoryState.onSuccess { data ->
            categoriesList = data
            // If we have a selectedCategoryId, find its name
            if (selectedCategoryId.isNotEmpty()) {
                val category = data.find { it.id == selectedCategoryId }
                if (category != null) {
                    selectedCategoryName = category.name
                }
            }
        }
    }

    // Also update the name when the ID changes
    LaunchedEffect(selectedCategoryId) {
        if (selectedCategoryId.isNotEmpty() && categoriesList.isNotEmpty()) {
            val category = categoriesList.find { it.id == selectedCategoryId }
            if (category != null) {
                selectedCategoryName = category.name
            }
        } else {
            selectedCategoryName = ""
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value =  if (selectedCategoryName.isNotEmpty()) selectedCategoryName else "Chọn danh mục sản phẩm",
            onValueChange = { },
            readOnly = true,
            label = { Text("Danh mục") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.colorSystem_heading_button),
                unfocusedBorderColor = colorResource(id = R.color.colorSystem_stroke)
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            categoriesList.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
}



@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ProductImageItem(
    imageUri: Uri,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.colorSystem_stroke),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        GlideImage(
            model = imageUri,
            contentDescription = "Product Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        ) {
            it.centerCrop()
        }

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
                .background(Color.White.copy(alpha = 0.7f), shape = CircleShape)
                .padding(2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Remove Image",
                tint = Color.Red,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}