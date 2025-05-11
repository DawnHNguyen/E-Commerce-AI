package com.ptit.core.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.MyCrossFade
import com.ptit.common.presentation.component.CustomPullToRefreshBox
import com.ptit.common.presentation.component.CustomSearchBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.component.ProductEmptyState
import com.ptit.common.presentation.component.noRippleClickable
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.product.ProductDomainEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToSearch: () -> Unit,
    navigateToProductDetail: (String) -> Unit,
) {
    LocalBottomNavigationVisibility.current.value = true

    val viewModel = hiltViewModel<HomeViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    CustomPullToRefreshBox(
        modifier = Modifier.background(color = colorResource(R.color.colorSystem_background_level_0)),
        isRefreshing = uiState.isLoadingTrending || uiState.isLoadingRecommended,
        onRefresh = {
            viewModel.refreshData()
        }
    ) {
        MaxSizeColumn {
            // Search Bar
            MaxWidthRow(
                modifier = Modifier
                    .background(color = colorResource(R.color.colorSystem_heading_button))
                    .padding(
                        vertical = 8.dp,
                        horizontal = 20.dp
                    )
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CustomSearchBar(
                    value = "",
                    onValueChange = {},
                    hint = "Tìm kiếm sản phẩm",
                    modifier = Modifier
                        .weight(1f)
                        .noRippleClickable(onClick = navigateToSearch),
                    enabled = false,
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    TrendingProductsSection(
                        isLoading = uiState.isLoadingTrending,
                        products = uiState.trendingProducts,
                        error = uiState.errorTrending,
                        onProductClick = navigateToProductDetail,
                        onRetry = { viewModel.fetchTrendingProducts() }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    RecommendedProductsSection(
                        isLoading = uiState.isLoadingRecommended,
                        products = uiState.recommendedProducts,
                        error = uiState.errorRecommended,
                        onProductClick = navigateToProductDetail,
                        onRetry = { viewModel.fetchHomeRecommendations() }
                    )
                }
            }
        }
    }
}

@Composable
fun TrendingProductsSection(
    isLoading: Boolean,
    products: List<ProductDomainEntity>,
    error: String?,
    onProductClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        MaxWidthRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "Sản phẩm bán chạy",
                style = CustomTypography.TextBold,
                fontSize = 18.sp,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp), // Chiều cao tương đối cho item ngang
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                ProductEmptyState(
                    message = "Lỗi tải sản phẩm bán chạy: $error",
                    buttonText = "Thử lại",
                    onActionClick = onRetry
                )
            }
            products.isEmpty() -> {
                Text(
                    text = "Hiện chưa có sản phẩm bán chạy nào.",
                    style = CustomTypography.TextRegular,
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = colorResource(id = R.color.colorSystem_normal_text)
                )
            }
            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        TrendingProductItem(
                            product = product,
                            onClick = { onProductClick(product.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun TrendingProductItem(
    product: ProductDomainEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp) // Điều chỉnh chiều rộng cho item ngang
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.colorSystem_text_field).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlideImage(
                model = product.image,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop,
                transition = MyCrossFade
            ) {
                it.centerCrop()
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                style = CustomTypography.TextMedium,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${product.price}đ",
                style = CustomTypography.TextSemiBold,
                fontSize = 15.sp,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )
            if (product.priceBeforeDiscount > 0 && product.priceBeforeDiscount > product.price) {
                Text(
                    text = "${product.priceBeforeDiscount}đ",
                    style = CustomTypography.TextRegular.copy(
                        textDecoration = TextDecoration.LineThrough,
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.colorSystem_greyscale_500)
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Autorenew,
                    contentDescription = "Refresh icon",
                    modifier = Modifier.size(16.dp),
                    tint = colorResource(id = R.color.colorSystem_normal_text)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = product.sold.toString(),
                    style = CustomTypography.TextRegular,
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.colorSystem_normal_text)
                )
            }
        }
    }
}


@Composable
fun RecommendedProductsSection(
    isLoading: Boolean,
    products: List<ProductDomainEntity>,
    error: String?,
    onProductClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Đề xuất cho bạn",
            style = CustomTypography.TextBold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp),
            color = colorResource(id = R.color.colorSystem_heading_button)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 50.dp), // Thêm padding để dễ nhìn hơn
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                ProductEmptyState(
                    message = "Lỗi tải sản phẩm đề xuất: $error",
                    buttonText = "Thử lại",
                    onActionClick = onRetry
                )
            }
            products.isEmpty() -> {
                Text(
                    text = "Hiện chưa có sản phẩm nào được đề xuất.",
                    style = CustomTypography.TextRegular,
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = colorResource(id = R.color.colorSystem_normal_text)
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                    userScrollEnabled = true,
                    modifier = Modifier.heightIn(max = (products.size / 2 * 280).dp)
                ) {
                    items(products,  key = { it.id }) { product ->
                        RecommendedProductItem(
                            product = product,
                            onClick = { onProductClick(product.id) }
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun RecommendedProductItem(
    product: ProductDomainEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth() // Item sẽ chiếm toàn bộ chiều rộng của cột trong Grid
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box {
                GlideImage(
                    model = product.image,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f), // Giữ ảnh vuông
                    contentScale = ContentScale.Crop,
                    transition = MyCrossFade
                ) {
                    it.centerCrop()
                }
                if (product.hasDiscount.value) {
                    Text(
                        text = "-${product.discountPercent.value}%",
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
                        text = "${product.price}đ",
                        style = CustomTypography.TextSemiBold,
                        fontSize = 15.sp,
                        color = colorResource(id = R.color.colorSystem_tint_red)
                    )
                    if (product.priceBeforeDiscount > 0 && product.priceBeforeDiscount > product.price) {
                        Text(
                            text = "${product.priceBeforeDiscount}đ",
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