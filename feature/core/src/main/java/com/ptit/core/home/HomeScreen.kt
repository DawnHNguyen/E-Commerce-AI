package com.ptit.core.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.MyCrossFade
import com.ptit.common.presentation.component.*
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.toPriceFormat
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.entity.recommendation.RecommendedProductDomainEntity
import com.ptit.domain.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToSearch: () -> Unit,
    navigateToProductDetail: (String) -> Unit,
    navigateToAllProducts: () -> Unit = {},
) {
    LocalBottomNavigationVisibility.current.value = true

    val viewModel = hiltViewModel<HomeViewModel>()
    val products = viewModel.products.collectAsLazyPagingItems()
    val recommendationsState by viewModel.recommendationsState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // 🔍 Search Bar (cố định, không cuộn)
        MaxWidthRow(
            modifier = Modifier
                .background(color = colorResource(R.color.colorSystem_heading_button))
                .padding(vertical = 8.dp, horizontal = 20.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomSearchBar(
                value = "",
                onValueChange = {},
                hint = "Tìm kiếm sản phẩm",
                modifier = Modifier
                    .weight(1f)
                    .noRippleClickable(onClick = navigateToSearch),
                enabled = false
            )
        }

        // Scrollable content
        CustomPullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(R.color.colorSystem_background_level_0)),
            isRefreshing = products.loadState.refresh == LoadState.Loading,
            onRefresh = {
                products.refresh()
                viewModel.fetchRecommendations()
            }
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 🌟 Recommendations Section
                item(span = { GridItemSpan(2) }) {
                    RecommendationsSection(
                        recommendationsState = recommendationsState,
                        onProductClick = navigateToProductDetail,
                        onViewAll = navigateToAllProducts
                    )
                }

                // Header for Products
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "Chờ bạn khám phá",
                        style = CustomTypography.TextBold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        color = colorResource(id = R.color.colorSystem_heading_button)
                    )
                }

                // Loading state (first load)
                if (products.loadState.refresh is LoadState.Loading) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = colorResource(id = R.color.colorSystem_heading_button)
                            )
                        }
                    }
                }

                // Error state
                if (products.loadState.refresh is LoadState.Error) {
                    item(span = { GridItemSpan(2) }) {
                        val error = (products.loadState.refresh as LoadState.Error).error
                        ProductEmptyState(
                            message = "Lỗi tải sản phẩm: ${error.message}",
                            buttonText = "Thử lại",
                            onActionClick = { products.retry() }
                        )
                    }
                }

                // Empty state
                if (products.loadState.refresh is LoadState.NotLoading && products.itemCount == 0) {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "Hiện chưa có sản phẩm nào.",
                            style = CustomTypography.TextRegular,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = colorResource(id = R.color.colorSystem_normal_text)
                        )
                    }
                }

                // Product items
                items(
                    count = products.itemCount,
                    key = products.itemKey { it.id }
                ) { index ->
                    products[index]?.let { product ->
                        ProductItem(
                            product = product,
                            onClick = { navigateToProductDetail(product.id) }
                        )
                    }
                }

                // Loading more indicator
                if (products.loadState.append is LoadState.Loading) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = colorResource(id = R.color.colorSystem_heading_button)
                            )
                        }
                    }
                }

                // Load more error
                if (products.loadState.append is LoadState.Error) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(onClick = { products.retry() }) {
                                Text(
                                    text = "Lỗi tải thêm. Nhấn để thử lại",
                                    color = colorResource(id = R.color.colorSystem_tint_red)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProductItem(
    product: ProductDomainEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box {
                GlideImage(
                    model = product.images.firstOrNull() ?: "",
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop,
                    transition = MyCrossFade
                ) {
                    it.centerCrop()
                }
                if (product.hasDiscount) {
                    Text(
                        text = "-${product.discountPercent}%",
                        style = CustomTypography.TextMedium.merge(
                            color = Color.White,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(
                                color = colorResource(R.color.colorSystem_tint_red),
                                shape = RoundedCornerShape(topEnd = 12.dp, bottomStart = 6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 100.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.name,
                    style = CustomTypography.TextRegular,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    color = colorResource(id = R.color.colorSystem_heading_button)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Column {
                    Text(
                        text = product.basePrice.toPriceFormat(),
                        style = CustomTypography.TextSemiBold,
                        fontSize = 15.sp,
                        color = colorResource(id = R.color.colorSystem_tint_red)
                    )
                    if (product.hasDiscount && product.virtualPrice != null) {
                        Text(
                            text = product.virtualPrice!!.toPriceFormat(),
                            style = CustomTypography.TextRegular.copy(
                                textDecoration = TextDecoration.LineThrough,
                                fontSize = 12.sp,
                                color = colorResource(id = R.color.colorSystem_greyscale_500)
                            )
                        )
                     }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = colorResource(R.color.colorSystem_tint_yellow),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", product.rating),
                            style = CustomTypography.TextRegular,
                            fontSize = 12.sp,
                            color = colorResource(id = R.color.colorSystem_normal_text)
                        )
                    }
                    Text(
                        text = "Đã bán ${product.sold}",
                        style = CustomTypography.TextRegular,
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.colorSystem_normal_text)
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendationsSection(
    recommendationsState: Resource<List<RecommendedProductDomainEntity>>,
    onProductClick: (String) -> Unit,
    onViewAll: () -> Unit
) {
    android.util.Log.d("HomeScreen", "=== RecommendationsSection recompose ===")
    android.util.Log.d("HomeScreen", "State: $recommendationsState")
    android.util.Log.d("HomeScreen", "State class: ${recommendationsState::class.java.simpleName}")

    // Always show title section
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp)
    ) {
        // Title Section with "View All" button (always visible)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Đề xuất cho bạn",
                style = CustomTypography.TextBold,
                fontSize = 20.sp,
                color = colorResource(R.color.colorSystem_heading_button)
            )

            TextButton(
                onClick = onViewAll,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Xem tất cả",
                    style = CustomTypography.TextMedium,
                    fontSize = 14.sp,
                    color = colorResource(R.color.colorSystem_heading_button)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on state
        when (recommendationsState) {
            is Resource.Success -> {
                val recommendations = recommendationsState.data
                android.util.Log.d("HomeScreen", "✅ SUCCESS - size: ${recommendations.size}")
                recommendations.forEachIndexed { index, product ->
                    android.util.Log.d("HomeScreen", "  Product $index: ${product.name}")
                }

                if (recommendations.isNotEmpty()) {
                    // Horizontal scrolling list
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(modifier = Modifier.width(4.dp))
                        recommendations.forEach { product ->
                            RecommendationProductCard(
                                product = product,
                                onClick = { onProductClick(product.id) }
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                } else {
                    android.util.Log.d("HomeScreen", "❌ Empty recommendations list")
                    Text(
                        text = "Chưa có đề xuất phù hợp",
                        style = CustomTypography.TextRegular,
                        fontSize = 14.sp,
                        color = colorResource(R.color.colorSystem_greyscale_500),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            is Resource.Loading -> {
                android.util.Log.d("HomeScreen", "⏳ LOADING")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                }
            }
            is Resource.Error -> {
                android.util.Log.e("HomeScreen", "❌ ERROR: ${recommendationsState.error}")
                Text(
                    text = "Không thể tải đề xuất",
                    style = CustomTypography.TextRegular,
                    fontSize = 14.sp,
                    color = colorResource(R.color.colorSystem_greyscale_500),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            is Resource.Idle -> {
                android.util.Log.d("HomeScreen", "💤 IDLE")
                // Show nothing for idle state
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun RecommendationProductCard(
    product: RecommendedProductDomainEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.colorSystem_background_level_1)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Product Image
            GlideImage(
                model = product.images.firstOrNull() ?: "",
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            // Product Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Product Name
                Text(
                    text = product.name,
                    style = CustomTypography.TextRegular,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = colorResource(R.color.colorSystem_normal_text)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Brand
                if (product.brandName.isNotEmpty()) {
                    Text(
                        text = product.brandName,
                        style = CustomTypography.TextRegular,
                        fontSize = 12.sp,
                        color = colorResource(R.color.colorSystem_greyscale_500)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Prices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = product.basePrice.toPriceFormat(),
                            style = CustomTypography.TextBold,
                            fontSize = 16.sp,
                            color = colorResource(R.color.colorSystem_tint_red)
                        )
                        if (product.virtualPrice > product.basePrice) {
                            Text(
                                text = product.virtualPrice.toPriceFormat(),
                                style = CustomTypography.TextRegular,
                                fontSize = 12.sp,
                                color = colorResource(R.color.colorSystem_greyscale_500),
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                }
            }
        }
    }
}
