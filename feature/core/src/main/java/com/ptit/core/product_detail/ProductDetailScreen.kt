package com.ptit.core.product_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.ptit.common.presentation.component.noRippleClickable
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.presentation.viewmodel.ProductDetailState
import com.ptit.presentation.viewmodel.ProductDetailViewModel

@Composable
fun ProductDetailScreen(
    productId: String,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    onBuyNowClick: () -> Unit
) {
    val viewModel = hiltViewModel<ProductDetailViewModel>()
    val productState by viewModel.productDetailState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = productId) {
        viewModel.getProductDetail(productId)
    }

    MaxSizeBox(
        modifier = Modifier.background(color = colorResource(R.color.colorSystem_background_level_0))
    ) {
        when (val state = productState) {
            is ProductDetailState.Initial -> {
                // Initial state, might show placeholder
            }

            is ProductDetailState.Loading -> {
                FullScreenProgressBar()
            }

            is ProductDetailState.Success -> {
                ProductDetailContent(
                    product = state.product,
                    onBackClick = onBackClick,
                    onCartClick = onCartClick,
                    onAddToCartClick = onAddToCartClick,
                    onBuyNowClick = onBuyNowClick
                )
            }

            is ProductDetailState.Error -> {
                // Show error state
                MaxSizeColumn(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        style = CustomTypography.TextRegular.merge(
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    )
                }
            }

            else -> {
                // Handle other states if necessary
                MaxSizeColumn(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Unexpected state",
                        style = CustomTypography.TextRegular.merge(
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProductDetailContent(
    product: ProductDomainEntity,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    onBuyNowClick: () -> Unit
) {
    MaxSizeColumn(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .background(colorResource(id = R.color.colorSystem_background_level_0))
    ) {
        // Light green container for back button, image carousel, and cart button
        // Light green container for back button, image carousel, and cart button
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }

                    // Cart button
                    IconButton(
                        onClick = onCartClick,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }
                }

                // Image carousel - positioned below the top row buttons
                val pagerState =
                    rememberPagerState(pageCount = { product.images.size.coerceAtLeast(1) })

                Column {
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
                        }
                        // Image indicator (e.g., "1/5")
                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
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
                    imageVector = Icons.Default.Star,
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
                        text = "    ${product.shop.shop.name}",
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
                text = "Thêm vào giỏ hàng",
                onClick = onAddToCartClick
            )

            FilledButton(
                modifier = Modifier.weight(1f),
                text = "Mua ngay",
                onClick = onBuyNowClick
            )
        }
    }
}
