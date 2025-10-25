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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.*
import com.ptit.common.presentation.component.*
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.entity.product.SKUDomainEntity
import com.ptit.domain.entity.product.VariantDomainEntity
import com.ptit.presentation.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun ProductDetailScreen(
    productId: String,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    onProductItemClick: (String) -> Unit
) {
    LocalBottomNavigationVisibility.current.value = false

    val viewModel = hiltViewModel<ProductDetailViewModel>()
    val productState by viewModel.productDetailState.collectAsStateWithLifecycle()
    val addToCartState by viewModel.addToCartState.collectAsStateWithLifecycle()
    val similarProductsState by viewModel.similarProductsState.collectAsStateWithLifecycle()
    val selectedVariants by viewModel.selectedVariants.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddToCartBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = productId) {
        viewModel.getProductDetail(productId)
    }

    LaunchedEffect(key1 = addToCartState) {
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
                        similarProductsState = similarProductsState,
                        onBackClick = onBackClick,
                        onCartClick = onCartClick,
                        onAddToCartClick = {
                            showAddToCartBottomSheet = true
                        },
                        isAddingToCart = addToCartState is AddToCartState.Loading,
                        onProductItemClick = onProductItemClick
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
                            onAddToCart = { skuValue, quantity ->
                                viewModel.addToCart(skuValue, quantity)
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
    selectedSKU: SKUDomainEntity?,
    similarProductsState: SimilarProductsState,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    isAddingToCart: Boolean = false,
    onProductItemClick: (String) -> Unit
) {
    MaxSizeColumn(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .background(colorResource(id = R.color.colorSystem_background_level_0))
    ) {
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

                // Hiển thị ảnh từ SKU đã chọn hoặc ảnh mặc định
                val displayImages = if (selectedSKU != null && selectedSKU.image.isNotEmpty()) {
                    listOf(selectedSKU.image)
                } else {
                    product.images.ifEmpty {
                        // Fallback: nếu không có images, tìm ảnh từ SKU đầu tiên hoặc dùng ảnh mặc định
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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

            // Hiển thị giá từ SKU đã chọn hoặc giá base
            val displayPrice = selectedSKU?.price ?: product.basePrice
            val virtualPrice = product.virtualPrice ?: 0
            val hasDiscount = virtualPrice > displayPrice && virtualPrice > 0

            MaxWidthRow(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${displayPrice}đ",
                    style = CustomTypography.TextSemiBold.merge(
                        color = colorResource(id = R.color.colorSystem_heading_button),
                        fontSize = 18.sp
                    )
                )

                if (hasDiscount) {
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                    Text(
                        text = "${virtualPrice}đ",
                        style = CustomTypography.TextRegular.merge(
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    )

                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))

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

            // Hiển thị variants
            product.variants.forEach { variant ->
                VariantSelector(
                    variant = variant,
                    selectedOption = selectedVariants[variant.name],
                    onOptionSelected = { option ->
                        onVariantSelected(variant.name, option)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Hiển thị stock khi đã chọn đủ variants
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
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(id = R.color.colorSystem_background_level_2)
                )
            ) {
                MaxWidthRow(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Shop",
                        tint = colorResource(id = R.color.colorSystem_heading_button),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = product.shop?.name ?: "Unknown Shop",
                        style = CustomTypography.TextMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mô tả sản phẩm",
                style = CustomTypography.TextSemiBold.merge(
                    color = colorResource(id = R.color.colorSystem_heading_button),
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.description,
                style = CustomTypography.TextRegular.merge(
                    color = colorResource(id = R.color.colorSystem_normal_text),
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

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
                    selectedSKU != null &&
                    selectedSKU.stock > 0

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

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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