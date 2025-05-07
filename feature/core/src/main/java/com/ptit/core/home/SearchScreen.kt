package com.ptit.core.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.component.CustomPullToRefreshBox
import com.ptit.common.presentation.component.CustomSearchBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.core.home.component.CategoryItem
import com.ptit.core.home.component.FilterOption
import com.ptit.core.home.component.ProductItem
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onIdle
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navigateBack: () -> Unit,
    navigateToProductDetail: (String) -> Unit,
) {
    LocalBottomNavigationVisibility.current.value = false

    val viewModel = hiltViewModel<SearchViewModel>()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    val categoriesState = viewModel.categoriesState.collectAsStateWithLifecycle().value
    val trendingProductsState = viewModel.trendingProductsState.collectAsStateWithLifecycle().value

    CustomPullToRefreshBox(
        modifier = Modifier.background(color = colorResource(R.color.colorSystem_background_level_0)),
        isRefreshing = searchResults.loadState.refresh == LoadState.Loading,
        onRefresh = {
            searchResults.refresh()
        }
    ) {
        MaxSizeColumn {
            // Search header with back button and search bar
            MaxWidthRow(
                modifier = Modifier
                    .background(color = colorResource(R.color.colorSystem_heading_button))
                    .padding(
                        vertical = 8.dp,
                        horizontal = 16.dp
                    )
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = navigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colorResource(R.color.colorSystem_background_level_0)
                    )
                }
                
                CustomSearchBar(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    hint = "Tìm kiếm sản phẩm",
                    modifier = Modifier.weight(1f),
                    onClickSearch = { /* Search is automatically triggered */ }
                )
            }
            
            // Categories row
            categoriesState.let { state ->
                state.onIdle { /* Do nothing */ }
                state.onLoading {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loading categories...",
                            style = CustomTypography.TextRegular.merge(
                                color = colorResource(R.color.colorSystem_normal_text),
                                fontSize = 14.sp
                            )
                        )
                    }
                }
                state.onSuccess { categories ->
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            CategoryItem(category = category)
                        }
                    }
                }
                state.onError {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Failed to load categories",
                            style = CustomTypography.TextRegular.merge(
                                color = colorResource(R.color.colorSystem_tint_red),
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
            
            // Filter options row (for sorting and filtering)
            MaxWidthRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterOption(text = "Liên quan", isSelected = true)
                FilterOption(text = "Bán chạy", isSelected = false)
                FilterOption(text = "Giá", isSelected = false)
                FilterOption(text = "Đánh giá cao", isSelected = false)
                FilterOption(text = "Vị trí", isSelected = false)
            }
            
            // Grid of search results
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
                // If search query is empty, show trending products section
                if (searchQuery.isEmpty()) {
                    item(
                        key = "trending_title",
                        span = { GridItemSpan(maxCurrentLineSpan) }
                    ) {
                        Text(
                            text = "Xu hướng",
                            style = CustomTypography.TextSemiBold.merge(
                                color = colorResource(R.color.colorSystem_heading_button),
                                fontSize = 18.sp
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    // Show trending products
                    trendingProductsState.onIdle { /* Do nothing */ }
                    trendingProductsState.onLoading {
                        item(
                            key = "trending_loading",
                            span = { GridItemSpan(maxCurrentLineSpan) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Loading trending products...",
                                    style = CustomTypography.TextRegular.merge(
                                        color = colorResource(R.color.colorSystem_normal_text),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }
                    trendingProductsState.onSuccess { products ->
                        items(
                            count = products.size,
                            key = { products[it].id }
                        ) { index ->
                            ProductItem(
                                product = products[index],
                                onClickProduct = { navigateToProductDetail(products[index].id) }
                            )
                        }
                    }
                    trendingProductsState.onError {
                        item(
                            key = "trending_error",
                            span = { GridItemSpan(maxCurrentLineSpan) }
                        ) {
                            Text(
                                text = "Failed to load trending products",
                                style = CustomTypography.TextRegular.merge(
                                    color = colorResource(R.color.colorSystem_tint_red),
                                    fontSize = 14.sp
                                ),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    
                    // Show search history section
                    item(
                        key = "search_history_title",
                        span = { GridItemSpan(maxCurrentLineSpan) }
                    ) {
                        Text(
                            text = "Lịch sử tìm kiếm",
                            style = CustomTypography.TextSemiBold.merge(
                                color = colorResource(R.color.colorSystem_heading_button),
                                fontSize = 18.sp
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    // Add some example search history items
                    item(
                        key = "search_history_item_1",
                        span = { GridItemSpan(maxCurrentLineSpan) }
                    ) {
                        MaxWidthRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { viewModel.onSearchQueryChanged("áo khoác gió") },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "áo khoác gió",
                                style = CustomTypography.TextRegular.merge(
                                    color = colorResource(R.color.colorSystem_normal_text),
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                } else {
                    // Show search results count
                    item(
                        key = "search_results_count",
                        span = { GridItemSpan(maxCurrentLineSpan) }
                    ) {
                        Text(
                            text = "Kết quả tìm kiếm cho '$searchQuery'",
                            style = CustomTypography.TextSemiBold.merge(
                                color = colorResource(R.color.colorSystem_heading_button),
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    // Show search results
                    items(
                        count = searchResults.itemCount,
                        key = searchResults.itemKey { it.id }
                    ) { index ->
                        searchResults[index]?.let { product ->
                            ProductItem(
                                product = product,
                                onClickProduct = { navigateToProductDetail(product.id) }
                            )
                        }
                    }
                    
                    // Show loading indicator for pagination
                    if (searchResults.loadState.append == LoadState.Loading) {
                        item(
                            key = "loading_more",
                            span = { GridItemSpan(maxCurrentLineSpan) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Loading more...",
                                    style = CustomTypography.TextRegular.merge(
                                        color = colorResource(R.color.colorSystem_normal_text),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}