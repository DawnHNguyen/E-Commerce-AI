package com.ptit.core.product_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.ptit.common.R
import com.ptit.common.presentation.*
import com.ptit.common.presentation.component.*
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.toPriceFormat
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.entity.product.VariantDomainEntity
import com.ptit.domain.entity.recommendation.RecommendedProductDomainEntity
import com.ptit.domain.utils.Resource
import com.ptit.presentation.viewmodel.*

import kotlinx.coroutines.launch

@Composable
fun ProductDetailScreen(
    productId: String,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    onProductItemClick: (String) -> Unit,
    navigateToAllReviews: (String) -> Unit = {}
) {
    LocalBottomNavigationVisibility.current.value = false

    val viewModel = hiltViewModel<ProductDetailViewModel>()
    val productState by viewModel.productDetailState.collectAsStateWithLifecycle()
    val similarProductsState by viewModel.similarProductsState.collectAsStateWithLifecycle()
    val addToCartState by viewModel.addToCartState.collectAsStateWithLifecycle()
    val selectedVariants by viewModel.selectedVariants.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddToCartBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        viewModel.getProductDetail(productId)
    }

    LaunchedEffect(addToCartState) {
        when (addToCartState) {
            is AddToCartState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Đã thêm sản phẩm vào giỏ hàng")
                    viewModel.resetAddToCartState()
                }
            }
            is AddToCartState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Lỗi: ${(addToCartState as AddToCartState.Error).message}")
                    viewModel.resetAddToCartState()
                }
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MaxSizeBox(
            modifier = Modifier.background(color = colorResource(R.color.colorSystem_background_level_0))
        ) {
            when (val state = productState) {
                is ProductDetailState.Initial -> {}
                is ProductDetailState.Loading -> FullScreenProgressBar()
                is ProductDetailState.Success -> {
                    ProductDetailContent(
                        product = state.product,
                        selectedVariants = selectedVariants,
                        onVariantSelected = { variantName, optionValue ->
                            viewModel.updateSelectedVariant(variantName, optionValue)
                        },
                        selectedSKU = viewModel.getSelectedSKU(state.product),
                        onBackClick = onBackClick,
                        onCartClick = onCartClick,
                        onAddToCartClick = { showAddToCartBottomSheet = true },
                        isAddingToCart = addToCartState is AddToCartState.Loading,
                        similarProductsState = similarProductsState,
                        onProductItemClick = onProductItemClick,
                        navigateToAllReviews = navigateToAllReviews
                    )

                    if (showAddToCartBottomSheet) {
                        AddToCartBottomSheet(
                            product = state.product,
                            selectedVariants = selectedVariants,
                            selectedSKU = viewModel.getSelectedSKU(state.product),
                            onVariantSelected = { variantName, optionValue ->
                                viewModel.updateSelectedVariant(variantName, optionValue)
                            },
                            isShowBottomSheet = showAddToCartBottomSheet,
                            onDismiss = { showAddToCartBottomSheet = false },
                            onAddToCart = { _, quantity ->
                                val selectedSKU = viewModel.getSelectedSKU(state.product)
                                selectedSKU?.let {
                                    viewModel.addToCart(it.id, quantity)  // ✅ gửi sku.id
                                }
                            }
                        )
                    }
                }
                is ProductDetailState.Error -> {
                    MaxSizeColumn(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Lỗi: ${state.message}",
                            style = CustomTypography.TextRegular.merge(
                                color = colorResource(id = R.color.colorSystem_heading_button)
                            )
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) { data ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = colorResource(id = R.color.colorSystem_background_level_2),
                contentColor = colorResource(id = R.color.colorSystem_heading_button),
            ) {
                Text(text = data.visuals.message)
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProductDetailContent(
    product: ProductDomainEntity,
    selectedVariants: Map<String, String>,
    onVariantSelected: (String, String) -> Unit,
    selectedSKU: com.ptit.domain.entity.product.SKUDomainEntity?,

    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    isAddingToCart: Boolean = false,
    similarProductsState: Resource<List<RecommendedProductDomainEntity>>,
    onProductItemClick: (String) -> Unit,
    navigateToAllReviews: (String) -> Unit = {}
) {
    MaxSizeColumn(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .background(colorResource(id = R.color.colorSystem_background_level_0))
    ) {
        // 🖼️ Ảnh sản phẩm
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFFE8F5E9))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }

                    IconButton(onClick = onCartClick) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = "Cart",
                            tint = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }
                }

                val displayImages = if (selectedSKU != null && selectedSKU.image.isNotEmpty()) {
                    listOf(selectedSKU.image)
                } else {
                    product.images.ifEmpty {
                        product.skus.firstOrNull()?.image?.let { listOf(it) } ?: emptyList()
                    }
                }

                val pagerState = rememberPagerState(pageCount = { displayImages.size.coerceAtLeast(1) })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) { page ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        GlideImage(
                            model = displayImages.getOrNull(page) ?: "",
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .height(200.dp),
                            contentScale = ContentScale.FillBounds,
                            transition = MyCrossFade,
                        ) {
                            it.centerCrop()
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1}/${displayImages.size}",
                                style = CustomTypography.TextRegular.merge(
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // 🧾 Thông tin sản phẩm
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = product.name,
                style = CustomTypography.TextSemiBold.merge(
                    color = colorResource(id = R.color.colorSystem_heading_button),
                    fontSize = 20.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            MaxWidthRow(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.StarRate,
                    contentDescription = "Rating",
                    tint = colorResource(id = R.color.colorSystem_tint_yellow),
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = product.rating.toString(),
                    style = CustomTypography.TextRegular.merge(
                        color = colorResource(id = R.color.colorSystem_normal_text),
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.weight(0.1f))
                Text(
                    text = "Đã bán: ${product.sold}",
                    style = CustomTypography.TextRegular.merge(
                        color = colorResource(id = R.color.colorSystem_normal_text),
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val displayPrice = selectedSKU?.price ?: product.basePrice
            val virtualPrice = product.virtualPrice ?: 0
            val hasDiscount = virtualPrice > displayPrice && virtualPrice > 0

            MaxWidthRow(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayPrice.toPriceFormat(),
                    style = CustomTypography.TextSemiBold.merge(
                        color = colorResource(id = R.color.colorSystem_heading_button),
                        fontSize = 18.sp
                    )
                )

                if (hasDiscount) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = virtualPrice.toPriceFormat(),
                        style = CustomTypography.TextRegular.merge(
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val discountPercent = ((virtualPrice - displayPrice) * 100 / virtualPrice)
                    Text(
                        text = "-${discountPercent}%",
                        style = CustomTypography.TextMedium.merge(
                            color = colorResource(id = R.color.colorSystem_heading_button),
                            fontSize = 12.sp
                        ),
                        modifier = Modifier
                            .background(
                                color = colorResource(R.color.colorSystem_stroke).copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🧩 Variants
            product.variants.forEach { variant ->
                VariantSelector(
                    variant = variant,
                    selectedOption = selectedVariants[variant.name],
                    onOptionSelected = { option -> onVariantSelected(variant.name, option) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (selectedVariants.size == product.variants.size) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (selectedSKU != null && selectedSKU.stock > 0)
                        "Kho: ${selectedSKU.stock}"
                    else "Hết hàng",
                    style = CustomTypography.TextRegular.merge(
                        color = if (selectedSKU != null && selectedSKU.stock > 0)
                            colorResource(id = R.color.colorSystem_normal_text)
                        else Color.Red,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // TODO: Navigate to Shop Detail
                    },
                // 1. Dùng elevation thay cho shadow để tránh lỗi và chuẩn Material 3
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val shopName = product.shopInfo?.name ?: "Shop"
                    val shopInitials = remember(shopName) {
                        // Tách chuỗi theo khoảng trắng
                        val words = shopName.trim().split("\\s+".toRegex())
                        // Lấy chữ cái đầu của từ thứ 1 và thứ 2 (nếu có)
                        val first = words.getOrNull(0)?.take(1) ?: ""
                        val second = words.getOrNull(1)?.take(1) ?: ""
                        (first + second).uppercase()
                    }

                    // 2. Định nghĩa giao diện Avatar chữ cái (để tái sử dụng)
                    val AvatarPlaceholder = @Composable {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF26C6DA)), // Màu nền xanh ngọc (Cyan) giống Shopee/Lazada
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = shopInitials,
                                style = CustomTypography.TextBold.merge(
                                    color = Color.White,
                                    fontSize = 18.sp // Chỉnh cỡ chữ cho vừa vòng tròn 48dp
                                )
                            )
                        }
                    }
                    // 2. Avatar Shop
                    if (product.shopInfo?.avatar.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        ) {
                            AvatarPlaceholder()
                        }
                    } else {
                        // Nếu có link -> Dùng GlideImage
                        GlideImage(
                            model = product.shopInfo!!.avatar,
                            contentDescription = "Shop Avatar",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            // SỬA Ở ĐÂY: Dùng tham số failure/loading thay vì builder
                            failure = placeholder { AvatarPlaceholder() },
                            loading = placeholder { AvatarPlaceholder() }
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))


                    // 3. Thông tin Shop
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = product.shopInfo?.name ?: "Admin Shop",
                            style = CustomTypography.TextSemiBold.merge(
                                color = colorResource(id = R.color.colorSystem_heading_button),
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${product.shopInfo?.productsCount ?: 0} Sản Phẩm",
                            style = CustomTypography.TextRegular.merge(
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            // ✅ Phần mới — Chi tiết sản phẩm
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chi tiết sản phẩm",
                style = CustomTypography.TextSemiBold.merge(
                    color = colorResource(id = R.color.colorSystem_heading_button),
                    fontSize = 16.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colorResource(id = R.color.colorSystem_background_level_2),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                DetailRow(label = "Danh mục", value = product.category?.name ?: "Chưa có")
                DetailRow(label = "Thương hiệu", value = product.brand?.name ?: "Chưa có")
                DetailRow(label = "Dòng sản phẩm", value = product.name)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ⭐ Đánh giá sản phẩm
            ProductReviewsSection(
                productId = product.id,
                onViewAllClick = { navigateToAllReviews(product.id) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 👇 THÊM SECTION SẢN PHẨM TƯƠNG TỰ Ở ĐÂY
            SimilarProductsSection(
                state = similarProductsState,
                onProductClick = onProductItemClick
            )

            // Spacer bottom để tránh nút Add to Cart che mất nội dung cuối
            Spacer(modifier = Modifier.height(80.dp))

        }
    }


    // 🛒 Nút thêm giỏ hàng
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val canAddToCart = selectedVariants.size == product.variants.size &&
                    selectedSKU != null && selectedSKU.stock > 0

            FilledButton(
                modifier = Modifier.weight(1f),
                text = if (isAddingToCart) "Đang thêm..." else "Thêm vào giỏ hàng",
                onClick = onAddToCartClick,
                enabled = !isAddingToCart && canAddToCart
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = CustomTypography.TextMedium.merge(
                color = colorResource(id = R.color.colorSystem_normal_text),
                fontSize = 14.sp
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = CustomTypography.TextRegular.merge(
                color = colorResource(id = R.color.colorSystem_heading_button),
                fontSize = 14.sp
            ),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun VariantSelector(
    variant: VariantDomainEntity,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit
) {
    Column {
        Text(
            text = variant.name,
            style = CustomTypography.TextMedium.merge(
                color = colorResource(id = R.color.colorSystem_heading_button),
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(variant.options) { option ->
                VariantOption(
                    option = option,
                    isSelected = option == selectedOption,
                    onClick = { onOptionSelected(option) }
                )
            }
        }
    }
}
// 👇 COMPOSABLE HIỂN THỊ DANH SÁCH (Tương tự RecommendationsSection ở Home)
@Composable
fun SimilarProductsSection(
    state: Resource<List<RecommendedProductDomainEntity>>,
    onProductClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            text = "Sản phẩm liên quan",
            style = CustomTypography.TextSemiBold.merge(
                color = colorResource(id = R.color.colorSystem_heading_button),
                fontSize = 16.sp
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        when (state) {
            is Resource.Success -> {
                val products = state.data
                if (products.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(products) { product ->
                            // Tái sử dụng RecommendationProductCard nếu bạn đã tách ra file chung
                            // Hoặc định nghĩa lại UI card nhỏ ở đây (giống Home)
                            SimilarProductCardItem(product = product, onClick = { onProductClick(product.id) })
                        }
                    }
                } else {
                    Text("Không có sản phẩm tương tự nào.", style = CustomTypography.TextRegular, fontSize = 14.sp)
                }
            }
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(R.color.colorSystem_heading_button))
                }
            }
            is Resource.Error -> {
                // Có thể ẩn đi hoặc hiện text lỗi nhẹ nhàng
            }
            else -> {}
        }
    }
}

// 👇 UI Card cho sản phẩm tương tự (Copy logic từ RecommendationProductCard ở Home)
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SimilarProductCardItem(
    product: RecommendedProductDomainEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp) // Card nhỏ hơn một chút cho list ngang
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            GlideImage(
                model = product.images.firstOrNull() ?: "",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = product.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = CustomTypography.TextRegular,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.basePrice.toPriceFormat(),
                    style = CustomTypography.TextBold,
                    color = colorResource(R.color.colorSystem_tint_red),
                    fontSize = 14.sp
                )
            }
        }
    }
}
@Composable
fun VariantOption(
    option: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected)
                    colorResource(id = R.color.colorSystem_heading_button)
                else
                    colorResource(id = R.color.colorSystem_stroke),
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                if (isSelected)
                    colorResource(id = R.color.colorSystem_heading_button).copy(alpha = 0.1f)
                else
                    Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = option,
            style = CustomTypography.TextMedium.merge(
                color = if (isSelected)
                    colorResource(id = R.color.colorSystem_heading_button)
                else
                    colorResource(id = R.color.colorSystem_normal_text),
                fontSize = 14.sp
            )
        )
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProductReviewsSection(
    productId: String,
    onViewAllClick: () -> Unit = {}
) {
    val reviewViewModel: com.ptit.core.review.ReviewViewModel = hiltViewModel()
    val reviewsState by reviewViewModel.reviewsState.collectAsStateWithLifecycle()

    LaunchedEffect(productId) {
        reviewViewModel.getReviews(productId, page = 1, limit = 5)
    }

    Column {
        // Header with title and "Xem tất cả" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Đánh giá sản phẩm",
                style = CustomTypography.TextSemiBold.merge(
                    color = colorResource(id = R.color.colorSystem_heading_button),
                    fontSize = 16.sp
                )
            )

            // "Xem tất cả" button - only show if there are reviews
            if (reviewsState is com.ptit.domain.utils.Resource.Success) {
                val reviewsData = (reviewsState as com.ptit.domain.utils.Resource.Success).data
                if (reviewsData.totalItems > 0) {
                    TextButton(onClick = onViewAllClick) {
                        Text(
                            text = "Xem tất cả",
                            style = CustomTypography.TextMedium.merge(
                                color = colorResource(id = R.color.colorSystem_heading_button),
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        when (reviewsState) {
            is com.ptit.domain.utils.Resource.Loading -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.colorSystem_background_level_2)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }
                }
            }
            is com.ptit.domain.utils.Resource.Success -> {
                val reviewsData = (reviewsState as com.ptit.domain.utils.Resource.Success).data
                val reviews = reviewsData.data

                if (reviews.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.colorSystem_background_level_2)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "0.0 trên 5",
                                style = CustomTypography.TextSemiBold.merge(
                                    color = colorResource(id = R.color.colorSystem_heading_button),
                                    fontSize = 18.sp
                                )
                            )
                            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                                repeat(5) {
                                    Icon(
                                        imageVector = Icons.Outlined.StarRate,
                                        contentDescription = null,
                                        tint = colorResource(id = R.color.colorSystem_stroke)
                                    )
                                }
                            }
                            Text(
                                text = "Chưa có đánh giá nào",
                                style = CustomTypography.TextRegular.merge(
                                    color = colorResource(id = R.color.colorSystem_normal_text),
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                } else {
                    // Calculate average rating
                    val avgRating = reviews.map { it.rating }.average()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.colorSystem_background_level_2)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "%.1f trên 5".format(avgRating),
                                style = CustomTypography.TextSemiBold.merge(
                                    color = colorResource(id = R.color.colorSystem_heading_button),
                                    fontSize = 18.sp
                                )
                            )
                            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                                repeat(5) { index ->
                                    Icon(
                                        imageVector = if (index < avgRating.toInt())
                                            Icons.Filled.Star
                                        else
                                            Icons.Outlined.StarRate,
                                        contentDescription = null,
                                        tint = if (index < avgRating.toInt())
                                            Color(0xFFFFB800)
                                        else
                                            colorResource(id = R.color.colorSystem_stroke)
                                    )
                                }
                            }
                            Text(
                                text = "${reviewsData.totalItems} đánh giá",
                                style = CustomTypography.TextRegular.merge(
                                    color = colorResource(id = R.color.colorSystem_normal_text),
                                    fontSize = 14.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = colorResource(id = R.color.colorSystem_stroke))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Show first few reviews
                            reviews.take(3).forEach { review ->
                                ReviewItem(review = review)
                                if (review != reviews.last()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }

                            if (reviewsData.totalItems > 3) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Xem thêm ${reviewsData.totalItems - 3} đánh giá khác",
                                    style = CustomTypography.TextMedium.merge(
                                        color = colorResource(id = R.color.colorSystem_heading_button),
                                        fontSize = 14.sp
                                    ),
                                    modifier = Modifier.clickable {
                                        // TODO: Navigate to full reviews screen
                                    }
                                )
                            }
                        }
                    }
                }
            }
            is com.ptit.domain.utils.Resource.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.colorSystem_background_level_2)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Không thể tải đánh giá",
                            style = CustomTypography.TextRegular.merge(
                                color = colorResource(id = R.color.colorSystem_normal_text),
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
            else -> {}
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ReviewItem(review: com.ptit.domain.entity.review.ReviewDomainEntity) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // User avatar
            GlideImage(
                model = review.user?.avatar ?: "",
                contentDescription = review.user?.name,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorResource(id = R.color.colorSystem_stroke)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.user?.name ?: "Anonymous",
                    style = CustomTypography.TextSemiBold.merge(
                        color = colorResource(id = R.color.colorSystem_heading_button),
                        fontSize = 14.sp
                    )
                )
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < review.rating)
                                Icons.Filled.Star
                            else
                                Icons.Outlined.StarRate,
                            contentDescription = null,
                            tint = if (index < review.rating)
                                Color(0xFFFFB800)
                            else
                                colorResource(id = R.color.colorSystem_stroke),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = review.content,
            style = CustomTypography.TextRegular.merge(
                color = colorResource(id = R.color.colorSystem_normal_text),
                fontSize = 14.sp
            )
        )

        // Show review images if available
        review.medias?.filter { it.type == "IMAGE" }?.let { images ->
            if (images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(images.take(3)) { media ->
                        GlideImage(
                            model = media.url,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = review.createdAt,
            style = CustomTypography.TextRegular.merge(
                color = colorResource(id = R.color.colorSystem_greyscale_400),
                fontSize = 12.sp
            )
        )
    }
}

