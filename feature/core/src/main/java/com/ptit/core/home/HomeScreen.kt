package com.ptit.core.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeBox
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.MyCrossFade
import com.ptit.common.presentation.component.CustomPullToRefreshBox
import com.ptit.common.presentation.component.CustomSearchBar
import com.ptit.common.presentation.component.noRippleClickable
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.product.ProductDomainEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToSearch: () -> Unit,
    navigateToCart: () -> Unit,
    navigateToProductDetail: (String) -> Unit,
) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val paginatedRecommendedProduct = viewModel.paginatedRecommendedProduct.collectAsLazyPagingItems()

    val searchQuery = viewModel.searchQuery.collectAsState().value
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()

    // Track whether search is active
    val isSearchActive = remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    CustomPullToRefreshBox(
        modifier = Modifier.background(color = colorResource(R.color.colorSystem_background_level_0)),
        isRefreshing = if (isSearchActive.value)
            searchResults.loadState.refresh == LoadState.Loading
        else
            paginatedRecommendedProduct.loadState.refresh == LoadState.Loading,
        onRefresh = {
            if (isSearchActive.value) {
                searchResults.refresh()
            } else {
                paginatedRecommendedProduct.refresh()
            }
        }
    ) {
        MaxSizeColumn {
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
                    value = searchQuery,
                    onValueChange = {
                        viewModel.updateSearchQuery(it)
                        if (it.isNotBlank()) {
                            isSearchActive.value = true
                        } else {
                            isSearchActive.value = false
                        }
                    },
                    hint = "Tìm kiếm sản phẩm",
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    enabled = true,
                    onSearch = {
                        focusManager.clearFocus()
                    },
                )
            }

            // Content changes based on search state
            if (isSearchActive.value) {
                // Search results
                LazyVerticalGrid(
                    modifier = Modifier.weight(1f),
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        vertical = 8.dp,
                        horizontal = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(
                        key = "search_title",
                        span = { GridItemSpan(maxCurrentLineSpan) }
                    ) {
                        MaxWidthRow(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kết quả tìm kiếm",
                                style = CustomTypography.TextSemiBold.merge(
                                    color = colorResource(R.color.colorSystem_heading_button),
                                    fontSize = 20.sp
                                ),
                            )

                            TextButton(
                                onClick = {
                                    viewModel.updateSearchQuery("")
                                    isSearchActive.value = false
                                    focusManager.clearFocus()
                                }
                            ) {
                                Text("Hủy")
                            }
                        }
                    }

                    if (searchResults.loadState.refresh is LoadState.Loading) {
                        item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                            MaxSizeBox(
                                modifier = Modifier.padding(top = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (searchResults.itemCount == 0) {
                        item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                            MaxSizeBox(
                                modifier = Modifier.padding(top = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Không tìm thấy sản phẩm",
                                    style = CustomTypography.TextMedium.merge(
                                        color = colorResource(R.color.colorSystem_normal_text)
                                    )
                                )
                            }
                        }
                    } else {
                        items(
                            count = searchResults.itemCount,
                            key = searchResults.itemKey { it.id }
                        ) { index ->
                            searchResults[index]?.let { product ->
                                RecommendProductItem(
                                    product = product,
                                    onClickProduct = { navigateToProductDetail(product.id) }
                                )
                            }
                        }
                    }
                }
            } else {
                // Normal home content
                LazyVerticalGrid(
                    modifier = Modifier.weight(1f),
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        vertical = 8.dp,
                        horizontal = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(
                        key = "recommend_title",
                        span = { GridItemSpan(maxCurrentLineSpan) }
                    ) {
                        Text(
                            text = "Gợi ý cho bạn",
                            style = CustomTypography.TextSemiBold.merge(
                                color = colorResource(R.color.colorSystem_heading_button),
                                fontSize = 20.sp
                            ),
                        )
                    }

                    items(
                        count = paginatedRecommendedProduct.itemCount,
                        key = paginatedRecommendedProduct.itemKey { it.id }
                    ) { index ->
                        paginatedRecommendedProduct[index]?.let { product ->
                            RecommendProductItem(
                                product = product,
                                onClickProduct = { navigateToProductDetail(product.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun RecommendProductItem(
    product: ProductDomainEntity,
    onClickProduct: () -> Unit,
) {
    val ratingAnnotatedString = remember(product.rating) {
        buildAnnotatedString {
            appendInlineContent(id = "ratingIcon")
            append(product.rating.toString())
        }
    }
    val ratingInlineContentMap = mapOf(
        "ratingIcon" to InlineTextContent(
            Placeholder(12.sp, 12.sp, PlaceholderVerticalAlign.TextCenter)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "",
                tint = colorResource(R.color.colorSystem_tint_yellow)
            )
        }
    )

    MaxSizeBox(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color = colorResource(R.color.colorSystem_background_level_2))
            .clickable(onClick = onClickProduct)
    ) {
        MaxSizeColumn {
            GlideImage(
                model = product.image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.FillBounds,
                transition = MyCrossFade,
            ) {
                it.centerCrop()
            }

            MaxSizeColumn(
                modifier = Modifier
                    .padding(
                        vertical = 8.dp,
                        horizontal = 12.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = product.name,
                    style = CustomTypography.TextRegular.merge(
                        color = colorResource(R.color.colorSystem_normal_text),
                        fontSize = 12.sp
                    ),
                    minLines = 2,
                    maxLines = 2
                )

                Text(
                    text = "${product.price}đ",
                    style = CustomTypography.TextSemiBold.merge(
                        color = colorResource(R.color.colorSystem_heading_button),
                        fontSize = 14.sp
                    )
                )

                Spacer(Modifier.height(8.dp))

                MaxWidthRow(
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = ratingAnnotatedString,
                        inlineContent = ratingInlineContentMap,
                        style = CustomTypography.TextRegular.merge(
                            color = colorResource(R.color.colorSystem_normal_text),
                            fontSize = 12.sp
                        )
                    )

                    Text(
                        text = "${product.sold} đã bán",
                        style = CustomTypography.TextRegular.merge(
                            color = colorResource(R.color.colorSystem_normal_text),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        if (product.hasDiscount.value)
            Text(
                text = "-${product.discountPercent.value}%",
                style = CustomTypography.TextMedium.merge(
                    color = colorResource(R.color.colorSystem_heading_button),
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .padding(4.dp)
                    .background(
                        color = colorResource(R.color.colorSystem_stroke).copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(
                        vertical = 8.dp,
                        horizontal = 4.dp
                    )
                    .align(Alignment.TopEnd)
            )
    }
}
