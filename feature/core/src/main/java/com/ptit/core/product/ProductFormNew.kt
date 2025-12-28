package com.ptit.core.product

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.domain.entity.product.*
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess
import kotlinx.coroutines.launch

data class VariantOption(
    val name: String,
    val options: MutableList<String> = mutableListOf()
)

data class SKUItem(
    val value: String,
    val price: String,
    val stock: String,
    val image: String = ""
)

data class SpecificationItem(
    val name: String,
    val value: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormNew(
    onNavigateBack: () -> Unit,
    productId: String? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel = hiltViewModel<ProductViewModel>()
    val isLoading = rememberState { false }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Form states
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var basePrice by remember { mutableStateOf("") }
    var virtualPrice by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("Chọn danh mục") }
    var brandId by remember { mutableStateOf("") }
    var selectedBrandName by remember { mutableStateOf("Chọn thương hiệu (tùy chọn)") }

    // Image handling
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var uploadedImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }

    // Variants and SKUs
    var variants by remember { mutableStateOf<List<VariantOption>>(emptyList()) }
    var skus by remember { mutableStateOf<List<SKUItem>>(emptyList()) }

    // Specifications
    var specifications by remember { mutableStateOf<List<SpecificationItem>>(emptyList()) }

    // Categories & Brands
    val categories by viewModel.categoryListState.collectAsStateWithLifecycle()
    var showCategoryDialog by remember { mutableStateOf(false) }

    val brandViewModel = hiltViewModel<com.ptit.core.brand.BrandViewModel>()
    val brands by brandViewModel.brandsState.collectAsStateWithLifecycle()
    var showBrandDialog by remember { mutableStateOf(false) }

    val isEditMode = productId != null

    // Load categories and brands
    LaunchedEffect(Unit) {
        viewModel.getCategories()
        brandViewModel.getBrands(page = 1, limit = 100) // Load all brands
    }

    // Load product details when in edit mode
    LaunchedEffect(productId) {
        if (productId != null) {
            viewModel.getProductDetails(productId)
        }
    }

    // Populate form fields when product details are loaded
    val productDetailsState by viewModel.productDetailsState.collectAsStateWithLifecycle()
    LaunchedEffect(productDetailsState) {
        if (productDetailsState is Resource.Success) {
            val product = (productDetailsState as Resource.Success).data
            product?.let {
                name = it.name
                description = it.description ?: ""
                basePrice = it.basePrice.toString()
                virtualPrice = it.virtualPrice.toString()
                categoryId = it.category?.id ?: ""
                selectedCategoryName = it.category?.name ?: "Chọn danh mục"
                brandId = it.brand?.id ?: ""
                selectedBrandName = it.brand?.name ?: "Chọn thương hiệu (tùy chọn)"
                uploadedImageUrls = it.images

                // Populate variants
                variants = it.variants.map { variant ->
                    VariantOption(
                        name = variant.name,
                        options = variant.options.toMutableList()
                    )
                }

                // Populate SKUs
                skus = it.skus.map { sku ->
                    SKUItem(
                        value = sku.value,
                        price = sku.price.toString(),
                        stock = sku.stock.toString(),
                        image = sku.image
                    )
                }

                // Populate specifications (if available)
                // Assuming specifications are stored in product entity
                // Add this if specifications exist in the ProductDomainEntity
            }
        }
    }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedImageUris = uris
    }

    // Handle upload images
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.uploadImagesState) {
            it.onLoading {
                isLoading.value = true
            }.onError { error ->
                isLoading.value = false
                scope.launch {
                    snackbarHostState.showSnackbar("Lỗi upload ảnh: ${error.message}")
                }
            }.onSuccess { urls ->
                isLoading.value = false
                uploadedImageUrls = urls
                scope.launch {
                    snackbarHostState.showSnackbar("Upload ảnh thành công!")
                }
            }
        }
    }

    // Handle save product
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.saveProductState) {
            it.onLoading {
                isLoading.value = true
            }.onError { error ->
                isLoading.value = false
                scope.launch {
                    snackbarHostState.showSnackbar("Lỗi: ${error.message}")
                }
            }.onSuccess { _ ->
                isLoading.value = false
                scope.launch {
                    snackbarHostState.showSnackbar("${if (isEditMode) "Cập nhật" else "Tạo"} sản phẩm thành công!")
                }
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Chỉnh sửa sản phẩm" else "Thêm sản phẩm mới",
                        style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                        color = colorResource(R.color.colorSystem_greyscale_0_white)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colorResource(R.color.colorSystem_greyscale_0_white)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.colorSystem_background_level_0))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Thông tin cơ bản
                SectionCard(title = "Thông tin cơ bản") {
                    FormTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Tên sản phẩm *",
                        placeholder = "Nhập tên sản phẩm"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FormTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Mô tả sản phẩm",
                        placeholder = "Nhập mô tả chi tiết về sản phẩm",
                        minLines = 4,
                        maxLines = 6
                    )
                }

                // Section: Hình ảnh
                SectionCard(title = "Hình ảnh sản phẩm") {
                    ImagePickerSection(
                        selectedImageUris = selectedImageUris,
                        uploadedImageUrls = uploadedImageUrls,
                        onPickImages = { imagePickerLauncher.launch("image/*") },
                        onRemoveImage = { uri ->
                            selectedImageUris = selectedImageUris - uri
                        },
                        onUploadImages = {
                            if (selectedImageUris.isNotEmpty()) {
                                viewModel.uploadProductImages(selectedImageUris)
                            }
                        }
                    )
                }

                // Section: Giá cả
                SectionCard(title = "Giá cả") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FormTextField(
                            value = basePrice,
                            onValueChange = { basePrice = it.filter { char -> char.isDigit() } },
                            label = "Giá gốc *",
                            placeholder = "0",
                            suffix = "đ",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )

                        FormTextField(
                            value = virtualPrice,
                            onValueChange = { virtualPrice = it.filter { char -> char.isDigit() } },
                            label = "Giá khuyến mãi",
                            placeholder = "0",
                            suffix = "đ",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section: Danh mục
                SectionCard(title = "Danh mục *") {
                    OutlinedCard(
                        onClick = { showCategoryDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = colorResource(R.color.colorSystem_background_level_2)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(
                                colorResource(R.color.colorSystem_stroke)
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCategoryName,
                                style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                                color = if (categoryId.isEmpty())
                                    colorResource(R.color.colorSystem_greyscale_400)
                                else
                                    colorResource(R.color.colorSystem_greyscale_900)
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = colorResource(R.color.colorSystem_text_button)
                            )
                        }
                    }
                }

                // Section: Thương hiệu (optional)
                SectionCard(title = "Thương hiệu (tùy chọn)") {
                    OutlinedCard(
                        onClick = { showBrandDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = colorResource(R.color.colorSystem_background_level_2)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(
                                colorResource(R.color.colorSystem_stroke)
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedBrandName,
                                style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                                color = if (brandId.isEmpty())
                                    colorResource(R.color.colorSystem_greyscale_400)
                                else
                                    colorResource(R.color.colorSystem_greyscale_900)
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = colorResource(R.color.colorSystem_text_button)
                            )
                        }
                    }

                    if (brandId.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                brandId = ""
                                selectedBrandName = "Chọn thương hiệu (tùy chọn)"
                            },
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Bỏ chọn thương hiệu",
                                style = CustomTypography.TextRegular.copy(fontSize = 12.sp)
                            )
                        }
                    }
                }

                // Section: Biến thể
                SectionCard(title = "Biến thể sản phẩm (tùy chọn)") {
                    VariantsSection(
                        variants = variants,
                        onAddVariant = {
                            variants = variants + VariantOption(name = "")
                        },
                        onRemoveVariant = { index ->
                            variants = variants.filterIndexed { i, _ -> i != index }
                        },
                        onUpdateVariant = { index, updated ->
                            variants = variants.mapIndexed { i, variant ->
                                if (i == index) updated else variant
                            }
                        }
                    )
                }

                // Section: SKUs
                if (variants.isNotEmpty() && variants.all { it.name.isNotEmpty() && it.options.isNotEmpty() }) {
                    SectionCard(title = "Quản lý SKU") {
                        SKUsSection(
                            skus = skus,
                            onUpdateSKU = { index, updated ->
                                skus = skus.mapIndexed { i, sku ->
                                    if (i == index) updated else sku
                                }
                            },
                            onGenerateSKUs = {
                                // Generate SKUs from variants
                                val generatedSKUs = generateSKUsFromVariants(variants, basePrice)
                                skus = generatedSKUs
                            }
                        )
                    }
                }

                // Section: Thông số kỹ thuật
                SectionCard(title = "Thông số kỹ thuật") {
                    SpecificationsSection(
                        specifications = specifications,
                        onAddSpecification = {
                            // Không làm gì vì đã có list cố định
                        },
                        onRemoveSpecification = { index ->
                            // Không làm gì vì không cho xóa spec
                        },
                        onUpdateSpecification = { index, updated ->
                            // Kiểm tra xem spec đã tồn tại chưa
                            val existingIndex = specifications.indexOfFirst { it.name == updated.name }
                            specifications = if (existingIndex >= 0) {
                                // Cập nhật spec đã có
                                specifications.mapIndexed { i, spec ->
                                    if (i == existingIndex) updated else spec
                                }
                            } else {
                                // Thêm spec mới
                                specifications + updated
                            }
                        }
                    )
                }

                // Save button
                Button(
                    onClick = {
                        // Validation
                        if (name.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng nhập tên sản phẩm")
                            }
                            return@Button
                        }

                        if (basePrice.isBlank() || basePrice.toIntOrNull() == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng nhập giá hợp lệ")
                            }
                            return@Button
                        }

                        if (categoryId.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng chọn danh mục")
                            }
                            return@Button
                        }

                        // Validate images uploaded
                        if (uploadedImageUrls.isEmpty() && selectedImageUris.isNotEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng upload ảnh trước khi lưu")
                            }
                            return@Button
                        }

                        // Validate SKUs if variants exist
                        if (variants.isNotEmpty() && skus.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng tạo SKU cho các biến thể")
                            }
                            return@Button
                        }

                        // Validate SKUs data
                        if (skus.any { it.price.toIntOrNull() == null || it.stock.toIntOrNull() == null }) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng nhập đầy đủ giá và số lượng cho SKU")
                            }
                            return@Button
                        }

                        // Create request
                        // Generate current datetime in ISO 8601 format
                        val currentDateTime = java.time.Instant.now().toString()

                        val request = CreateProductRequestDomainEntity(
                            name = name.trim(),
                            description = description.trim(),
                            publishedAt = currentDateTime, // Set to current time to publish immediately
                            basePrice = basePrice.toInt(),
                            virtualPrice = virtualPrice.toIntOrNull(),
                            brandId = brandId.ifBlank { null },
                            categoryId = categoryId,
                            images = uploadedImageUrls,
                            variants = variants
                                .filter { it.name.isNotBlank() && it.options.isNotEmpty() }
                                .map { VariantRequestDomainEntity(it.name.trim(), it.options.map { opt -> opt.trim() }) },
                            skus = skus.map {
                                SKURequestDomainEntity(
                                    value = it.value.trim(),
                                    price = it.price.toIntOrNull() ?: 0,
                                    stock = it.stock.toIntOrNull() ?: 0,
                                    image = it.image
                                )
                            },
                            specifications = specifications
                                .filter { it.name.isNotBlank() && it.value.isNotBlank() }
                                .map { SpecificationDomainEntity(it.name.trim(), it.value.trim()) }
                        )

                        if (isEditMode && productId != null) {
                            val updateRequest = UpdateProductRequestDomainEntity(
                                name = name.trim(),
                                description = description.trim(),
                                publishedAt = currentDateTime, // Set to current time
                                basePrice = basePrice.toInt(),
                                virtualPrice = virtualPrice.toIntOrNull(),
                                brandId = brandId.ifBlank { null },
                                categoryId = categoryId,
                                images = uploadedImageUrls,
                                variants = variants
                                    .filter { it.name.isNotBlank() && it.options.isNotEmpty() }
                                    .map { VariantRequestDomainEntity(it.name.trim(), it.options.map { opt -> opt.trim() }) },
                                skus = skus.map {
                                    SKURequestDomainEntity(
                                        value = it.value.trim(),
                                        price = it.price.toIntOrNull() ?: 0,
                                        stock = it.stock.toIntOrNull() ?: 0,
                                        image = it.image
                                    )
                                },
                                specifications = specifications
                                    .filter { it.name.isNotBlank() && it.value.isNotBlank() }
                                    .map { SpecificationDomainEntity(it.name.trim(), it.value.trim()) }
                            )
                            viewModel.updateProduct(productId, updateRequest)
                        } else {
                            viewModel.createProduct(request)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.colorSystem_heading_button)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        if (isEditMode) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) "Cập nhật sản phẩm" else "Tạo sản phẩm",
                        style = CustomTypography.TextBold.copy(fontSize = 16.sp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            if (isLoading.value) {
                FullScreenProgressBar()
            }
        }

        // Category selection dialog
        if (showCategoryDialog && categories is Resource.Success) {
            CategorySelectionDialog(
                categories = (categories as Resource.Success).data,
                onDismiss = { showCategoryDialog = false },
                onSelectCategory = { category ->
                    categoryId = category.id
                    selectedCategoryName = category.name
                    showCategoryDialog = false
                }
            )
        }

        // Brand selection dialog
        if (showBrandDialog && brands is Resource.Success) {
            BrandSelectionDialog(
                brands = (brands as Resource.Success<com.ptit.domain.entity.brand.BrandListDomainEntity>).data.data,
                onDismiss = { showBrandDialog = false },
                onSelectBrand = { brand ->
                    brandId = brand.id
                    selectedBrandName = brand.name
                    showBrandDialog = false
                }
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.colorSystem_background_level_2)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
            color = colorResource(R.color.colorSystem_greyscale_700)
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                    color = colorResource(R.color.colorSystem_greyscale_400)
                )
            },
            suffix = suffix?.let {
                {
                    Text(
                        text = it,
                        style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
                        color = colorResource(R.color.colorSystem_text_button)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = CustomTypography.TextRegular.copy(fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                unfocusedBorderColor = colorResource(R.color.colorSystem_stroke),
                focusedContainerColor = colorResource(R.color.colorSystem_background_level_2),
                unfocusedContainerColor = colorResource(R.color.colorSystem_background_level_2)
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            minLines = minLines,
            maxLines = maxLines
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ImagePickerSection(
    selectedImageUris: List<Uri>,
    uploadedImageUrls: List<String>,
    onPickImages: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onUploadImages: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (selectedImageUris.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 2.dp,
                        color = colorResource(R.color.colorSystem_stroke),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onPickImages() }
                    .background(colorResource(R.color.colorSystem_background_level_1)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = colorResource(R.color.colorSystem_text_button)
                    )
                    Text(
                        text = "Chọn ảnh sản phẩm",
                        style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
                        color = colorResource(R.color.colorSystem_text_button)
                    )
                }
            }
        } else {
            // Show selected images
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(selectedImageUris) { uri ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        // Image
                        GlideImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Remove button - nhỏ hơn, đẹp hơn
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable { onRemoveImage(uri) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Xóa",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 2.dp,
                                color = colorResource(R.color.colorSystem_stroke),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onPickImages() }
                            .background(colorResource(R.color.colorSystem_background_level_1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Thêm ảnh",
                            tint = colorResource(R.color.colorSystem_text_button)
                        )
                    }
                }
            }

            if (uploadedImageUrls.isEmpty()) {
                Button(
                    onClick = onUploadImages,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.colorSystem_text_button)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload ảnh", style = CustomTypography.TextSemiBold)
                }
            } else {
                Text(
                    text = "✓ Đã upload ${uploadedImageUrls.size} ảnh",
                    style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
                    color = colorResource(R.color.colorSystem_success)
                )
            }
        }
    }
}

@Composable
private fun VariantsSection(
    variants: List<VariantOption>,
    onAddVariant: () -> Unit,
    onRemoveVariant: (Int) -> Unit,
    onUpdateVariant: (Int, VariantOption) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        variants.forEachIndexed { index, variant ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.colorSystem_background_level_1)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Biến thể ${index + 1}",
                            style = CustomTypography.TextSemiBold.copy(fontSize = 14.sp),
                            color = colorResource(R.color.colorSystem_heading_button)
                        )
                        IconButton(
                            onClick = { onRemoveVariant(index) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Xóa",
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = variant.name,
                        onValueChange = { onUpdateVariant(index, variant.copy(name = it)) },
                        placeholder = { Text("Tên biến thể (VD: Màu sắc, Kích thước)") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    var optionText by remember { mutableStateOf("") }

                    // Function to add option
                    val addOption = {
                        if (optionText.isNotBlank()) {
                            val updatedOptions = variant.options.toMutableList()
                            updatedOptions.add(optionText.trim())
                            onUpdateVariant(index, variant.copy(options = updatedOptions))
                            optionText = ""
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = optionText,
                            onValueChange = { optionText = it },
                            placeholder = { Text("Thêm tùy chọn (Enter để thêm)") },
                            modifier = Modifier.weight(1f),
                            textStyle = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
                            ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onDone = { addOption() }
                            ),
                            trailingIcon = if (optionText.isNotBlank()) {
                                {
                                    IconButton(onClick = addOption) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Thêm",
                                            tint = colorResource(R.color.colorSystem_success),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            } else null
                        )

                        // Add button
                        IconButton(
                            onClick = addOption,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (optionText.isNotBlank())
                                        colorResource(R.color.colorSystem_heading_button)
                                    else
                                        colorResource(R.color.colorSystem_greyscale_300),
                                    RoundedCornerShape(8.dp)
                                ),
                            enabled = optionText.isNotBlank()
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Thêm",
                                tint = Color.White
                            )
                        }
                    }

                    // Show added options
                    if (variant.options.isNotEmpty()) {
                        val chipContainerColor = colorResource(R.color.colorSystem_background_level_2)
                        val chipLabelColor = colorResource(R.color.colorSystem_greyscale_900)
                        val chipBorderColor = colorResource(R.color.colorSystem_stroke)

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(variant.options) { _, option ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        val updatedOptions = variant.options.toMutableList()
                                        updatedOptions.remove(option)
                                        onUpdateVariant(index, variant.copy(options = updatedOptions))
                                    },
                                    label = {
                                        Text(
                                            text = option,
                                            style = CustomTypography.TextRegular.copy(fontSize = 13.sp)
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Xóa",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = chipContainerColor,
                                        labelColor = chipLabelColor
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = false,
                                        borderColor = chipBorderColor
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onAddVariant,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colorResource(R.color.colorSystem_heading_button)
            ),
            border = BorderStroke(
                width = 1.dp,
                color = colorResource(R.color.colorSystem_heading_button)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Thêm biến thể", style = CustomTypography.TextSemiBold)
        }
    }
}

@Composable
private fun SKUsSection(
    skus: List<SKUItem>,
    onUpdateSKU: (Int, SKUItem) -> Unit,
    onGenerateSKUs: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (skus.isEmpty()) {
            Button(
                onClick = onGenerateSKUs,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tạo SKU tự động", style = CustomTypography.TextSemiBold)
            }
        } else {
            skus.forEachIndexed { index, sku ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.colorSystem_background_level_1)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "SKU: ${sku.value}",
                            style = CustomTypography.TextSemiBold.copy(fontSize = 14.sp),
                            color = colorResource(R.color.colorSystem_heading_button)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = sku.price,
                                onValueChange = { onUpdateSKU(index, sku.copy(price = it.filter { char -> char.isDigit() })) },
                                label = { Text("Giá") },
                                suffix = { Text("đ") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = sku.stock,
                                onValueChange = { onUpdateSKU(index, sku.copy(stock = it.filter { char -> char.isDigit() })) },
                                label = { Text("Tồn kho") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onGenerateSKUs,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.colorSystem_text_button)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tạo lại SKU", style = CustomTypography.TextSemiBold)
            }
        }
    }
}

@Composable
private fun SpecificationsSection(
    specifications: List<SpecificationItem>,
    onAddSpecification: () -> Unit,
    onRemoveSpecification: (Int) -> Unit,
    onUpdateSpecification: (Int, SpecificationItem) -> Unit
) {
    // Danh sách các thông số cố định
    val predefinedSpecs = listOf(
        "Xuất xứ",
        "Chất liệu",
        "Kho hàng",
        "Vị trí kho",
        "Gửi từ",
        "Bảo hành"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Hiển thị các thông số với label cố định
        predefinedSpecs.forEachIndexed { index, specName ->
            // Tìm giá trị hiện tại của thông số này
            val currentSpec = specifications.find { it.name == specName }
            val currentValue = currentSpec?.value ?: ""

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Label cố định
                Text(
                    text = specName,
                    style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
                    color = colorResource(R.color.colorSystem_heading_button),
                    modifier = Modifier.width(100.dp)
                )

                // TextField để nhập giá trị
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = { newValue ->
                        // Tìm index của spec trong list
                        val specIndex = specifications.indexOfFirst { it.name == specName }
                        if (specIndex >= 0) {
                            // Cập nhật spec đã có
                            onUpdateSpecification(specIndex, SpecificationItem(specName, newValue))
                        } else {
                            // Thêm spec mới
                            onAddSpecification()
                            onUpdateSpecification(
                                specifications.size,
                                SpecificationItem(specName, newValue)
                            )
                        }
                    },
                    placeholder = {
                        Text(
                            when (specName) {
                                "Xuất xứ" -> "VD: Việt Nam"
                                "Chất liệu" -> "VD: Nhựa, Kim loại"
                                "Kho hàng" -> "VD: 50"
                                "Vị trí kho" -> "VD: Hà Nội"
                                "Gửi từ" -> "VD: Hồ Chí Minh"
                                "Bảo hành" -> "VD: 6 tháng"
                                else -> "Nhập giá trị"
                            },
                            style = CustomTypography.TextRegular.copy(fontSize = 13.sp),
                            color = colorResource(R.color.colorSystem_greyscale_400)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                        unfocusedBorderColor = colorResource(R.color.colorSystem_stroke)
                    )
                )
            }

            // Divider giữa các thông số
            if (index < predefinedSpecs.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = colorResource(R.color.colorSystem_stroke).copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun CategorySelectionDialog(
    categories: List<CategoryDomainEntity>,
    onDismiss: () -> Unit,
    onSelectCategory: (CategoryDomainEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Chọn danh mục",
                style = CustomTypography.TextBold.copy(fontSize = 18.sp)
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                categories.forEach { category ->
                    TextButton(
                        onClick = { onSelectCategory(category) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = category.name,
                            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                            color = colorResource(R.color.colorSystem_greyscale_900),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Đóng",
                    style = CustomTypography.TextSemiBold,
                    color = colorResource(R.color.colorSystem_heading_button)
                )
            }
        },
        containerColor = colorResource(R.color.colorSystem_background_level_2),
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun BrandSelectionDialog(
    brands: List<com.ptit.domain.entity.brand.BrandDomainEntity>,
    onDismiss: () -> Unit,
    onSelectBrand: (com.ptit.domain.entity.brand.BrandDomainEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Chọn thương hiệu",
                style = CustomTypography.TextBold.copy(fontSize = 18.sp)
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                brands.forEach { brand ->
                    TextButton(
                        onClick = { onSelectBrand(brand) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Brand logo with background
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colorResource(R.color.colorSystem_background_level_1)),
                                contentAlignment = Alignment.Center
                            ) {
                                GlideImage(
                                    model = brand.logo,
                                    contentDescription = brand.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                ) {
                                    // Glide request builder
                                    it.centerCrop()
                                }
                            }

                            Text(
                                text = brand.name,
                                style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                                color = colorResource(R.color.colorSystem_greyscale_900),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Đóng",
                    style = CustomTypography.TextSemiBold,
                    color = colorResource(R.color.colorSystem_heading_button)
                )
            }
        },
        containerColor = colorResource(R.color.colorSystem_background_level_2),
        shape = RoundedCornerShape(16.dp)
    )
}

private fun generateSKUsFromVariants(variants: List<VariantOption>, basePrice: String): List<SKUItem> {
    if (variants.isEmpty() || variants.any { it.options.isEmpty() }) {
        return emptyList()
    }

    val combinations = mutableListOf<List<String>>()

    fun generateCombinations(current: List<String>, depth: Int) {
        if (depth == variants.size) {
            combinations.add(current)
            return
        }

        variants[depth].options.forEach { option ->
            generateCombinations(current + option, depth + 1)
        }
    }

    generateCombinations(emptyList(), 0)

    return combinations.map { combination ->
        SKUItem(
            value = combination.joinToString("-"),
            price = basePrice.ifBlank { "0" },
            stock = "0",
            image = ""
        )
    }
}

