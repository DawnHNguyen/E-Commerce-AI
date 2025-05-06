package com.ptit.core.product_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack // Đã sửa
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.ShoppingCart // Đã sửa
import androidx.compose.material.icons.outlined.StarRate // Đã sửa
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.ptit.common.presentation.MaxSizeBox
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.MyCrossFade
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.component.noRippleClickable
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.presentation.viewmodel.AddToCartState
import com.ptit.presentation.viewmodel.ProductDetailState
import com.ptit.presentation.viewmodel.ProductDetailViewModel
import com.ptit.presentation.viewmodel.SimilarProductsState // Import mới
import kotlinx.coroutines.launch

@Composable
fun ProductDetailScreen(
    productId: String,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    onBuyNowClick: () -> Unit,
    onProductItemClick: (String) -> Unit // Thêm callback này
) {
    LocalBottomNavigationVisibility.current.value = false

    val viewModel = hiltViewModel<ProductDetailViewModel>()
    val productState by viewModel.productDetailState.collectAsStateWithLifecycle()
    val addToCartState by viewModel.addToCartState.collectAsStateWithLifecycle()
    val similarProductsState by viewModel.similarProductsState.collectAsStateWithLifecycle() // Lấy state sản phẩm tương tự

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = productId) {
        viewModel.getProductDetail(productId)
    }

    // Handle add to cart state changes
    LaunchedEffect(key1 = addToCartState) {
        when (addToCartState) {
            is AddToCartState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Đã thêm sản phẩm vào giỏ hàng")
                }
            }

            is AddToCartState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Lỗi: ${(addToCartState as AddToCartState.Error).message}")
                }
            }
            // Thêm else để when expression là exhaustive
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MaxSizeBox(
            modifier = Modifier.background(color = colorResource(R.color.colorSystem_background_level_0))
        ) {
            when (val state = productState) {
                is ProductDetailState.Initial -> {}
                is ProductDetailState.Loading -> FullScreenProgressBar()
                is ProductDetailState.Success -> {
                    ProductDetailContent(
                        product = state.product,
                        similarProductsState = similarProductsState, // Truyền state vào
                        onBackClick = onBackClick,
                        onCartClick = onCartClick,
                        onAddToCartClick = { viewModel.addToCart(state.product.id) }, // Sử dụng onAddToCartClick từ ViewModel
                        onBuyNowClick = { /* TODO: Implement Buy Now logic */ }, // Sử dụng onBuyNowClick từ ViewModel
                        isAddingToCart = addToCartState is AddToCartState.Loading,
                        onProductItemClick = onProductItemClick // Truyền callback
                    )
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
                // Thêm else để when expression là exhaustive
                else -> {
                    MaxSizeColumn(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Trạng thái không mong muốn",
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
    similarProductsState: SimilarProductsState, // Thêm trạng thái sản phẩm tương tự
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    onBuyNowClick: () -> Unit,
    isAddingToCart: Boolean = false,
    onProductItemClick: (String) -> Unit // Thêm callback khi click vào sản phẩm gợi ý
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
                .background(Color(0xFFE8F5E9)) // Light green background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top row containing back button, empty space, and cart button
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back button
                    IconButton(
                        onClick = onBackClick,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }

                    // Cart button
                    IconButton(
                        onClick = onCartClick,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = "Cart",
                            tint = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }
                }

                // Image carousel - positioned below the top row buttons
                val pagerState =
                    rememberPagerState(pageCount = { product.images.size.coerceAtLeast(1) })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) { page ->
                    val imageUrl = if (product.images.isNotEmpty()) {
                        product.images.getOrElse(page) { product.image }
                    } else {
                        product.image
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        GlideImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .height(200.dp),
                            contentScale = ContentScale.FillBounds,
                            transition = MyCrossFade,
                        ) {
                            it.centerCrop()
                        }
                        // Image indicator (e.g., "1/5")
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1}/${product.images.size.coerceAtLeast(1)}",
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

        // Product details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Product name
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

            // Rating and sold count
            MaxWidthRow(
                verticalAlignment = Alignment.CenterVertically
            ) {
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

            // Price information
            MaxWidthRow(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${product.price}đ",
                    style = CustomTypography.TextSemiBold.merge(
                        color = colorResource(id = R.color.colorSystem_heading_button),
                        fontSize = 18.sp
                    )
                )

                if (product.hasDiscount.value) {
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                    Text(
                        text = "${product.priceBeforeDiscount}đ",
                        style = CustomTypography.TextRegular.merge(
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    )

                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                    Text(
                        text = "-${product.discountPercent.value}%",
                        style = CustomTypography.TextMedium.merge(
                            color = colorResource(id = R.color.colorSystem_heading_button),
                            fontSize = 12.sp
                        ),
                        modifier = Modifier
                            .background(
                                color = colorResource(R.color.colorSystem_stroke).copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(
                                vertical = 4.dp,
                                horizontal = 8.dp
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shop information
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
                        text = product.shop.shop.name, // Giả sử product.shop.shop.name không null
                        style = CustomTypography.TextMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "Xem shop",
                        style = CustomTypography.TextRegular.merge(
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        ),
                        modifier = Modifier.noRippleClickable { /* Navigate to shop */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Product description
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

            // Similar Products Section
            SimilarProductsSection(
                similarProductsState = similarProductsState,
                onProductItemClick = onProductItemClick
            )


            Spacer(modifier = Modifier.height(80.dp)) // Space for buttons
        }
    }

    // Action buttons at bottom
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
            FilledButton(
                modifier = Modifier.weight(1f),
                text = if (isAddingToCart) "Đang thêm..." else "Thêm vào giỏ hàng",
                onClick = onAddToCartClick,
                enabled = !isAddingToCart
            )

            FilledButton(
                modifier = Modifier.weight(1f),
                text = "Mua ngay",
                onClick = onBuyNowClick,
                enabled = !isAddingToCart,
                // Sử dụng ButtonDefaults để tùy chỉnh màu cho nút "Mua ngay"
                // colors = ButtonDefaults.buttonColors( // Lỗi: FilledButton không có tham số colors trực tiếp
                // containerColor = colorResource(id = R.color.colorSystem_tint_red)
                // )
            )
        }
    }
}


@Composable
fun ColumnScope.SimilarProductsSection(
    similarProductsState: SimilarProductsState,
    onProductItemClick: (String) -> Unit
) {
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = "Sản phẩm tương tự",
        style = CustomTypography.TextSemiBold.merge(
            color = colorResource(id = R.color.colorSystem_heading_button),
            fontSize = 16.sp
        )
    )
    Spacer(modifier = Modifier.height(12.dp))

    when (similarProductsState) {
        is SimilarProductsState.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 16.dp)
            )
        }
        is SimilarProductsState.Success -> {
            if (similarProductsState.products.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(similarProductsState.products, key = { it.id }) { product ->
                        SimilarProductItem(
                            product = product,
                            onClick = { onProductItemClick(product.id) }
                        )
                    }
                }
            } else {
                Text(
                    text = "Không tìm thấy sản phẩm tương tự.",
                    style = CustomTypography.TextRegular,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        is SimilarProductsState.Error -> {
            Text(
                text = "Lỗi: ${similarProductsState.message}",
                style = CustomTypography.TextRegular,
                color = Color.Red,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        SimilarProductsState.Initial -> {
            // Có thể hiển thị placeholder hoặc không làm gì cả
        }

         else -> {
             // Xử lý cho các trường hợp khác không được liệt kê ở trên
         }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SimilarProductItem(product: ProductDomainEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp) // Chiều rộng cố định cho item
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.colorSystem_background_level_2)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            GlideImage(
                model = product.image,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f), // Giữ tỷ lệ vuông cho ảnh
                contentScale = ContentScale.Crop,
                transition = MyCrossFade
            ) {
                it.centerCrop()
            }
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = product.name,
                    style = CustomTypography.TextMedium,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = colorResource(id = R.color.colorSystem_heading_button)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${product.price}đ",
                    style = CustomTypography.TextSemiBold,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.colorSystem_tint_red) // Màu giá nổi bật
                )
                if (product.hasDiscount.value) {
                    Text(
                        text = "${product.priceBeforeDiscount}đ",
                        style = CustomTypography.TextRegular.copy(
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough
                        ),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}