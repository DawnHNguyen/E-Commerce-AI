package com.ptit.core.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.ptit.core.home.component.FilterBottomSheet
import com.ptit.core.home.component.ProductItem
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
    val filters by viewModel.filters.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }

    // Calculate active filter count
    val activeFilterCount = remember(filters) {
        var count = 0
        if (filters.categoryIds.isNotEmpty()) count++
        if (filters.minPrice != null || filters.maxPrice != null) count++
        if (filters.sortBy != "createdAt" || filters.orderBy != "desc") count++
        count
    }

    // Show filter bottom sheet
    if (showFilterSheet) {
        categoriesState.onSuccess { categories ->
            FilterBottomSheet(
                categories = categories,
                selectedCategoryIds = filters.categoryIds,
                minPrice = filters.minPrice,
                maxPrice = filters.maxPrice,
                selectedSortBy = filters.sortBy,
                selectedOrderBy = filters.orderBy,
                onDismiss = { showFilterSheet = false },
                onApply = { categoryIds, minPrice, maxPrice, sortBy, orderBy ->
                    viewModel.applyAllFilters(categoryIds, minPrice, maxPrice, sortBy, orderBy)
                }
            )
        }
    }

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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

                // Filter button with badge
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorResource(R.color.colorSystem_background_level_0).copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = colorResource(R.color.colorSystem_background_level_0)
                        )
                    }

                    // Active filter count badge
                    if (activeFilterCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(colorResource(R.color.colorSystem_tint_red))
                                .align(Alignment.TopEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeFilterCount.toString(),
                                style = CustomTypography.TextBold.copy(
                                    fontSize = 10.sp,
                                    color = colorResource(R.color.colorSystem_background_level_0)
                                )
                            )
                        }
                    }
                }
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
                // If search query is empty, show placeholder
                if (searchQuery.isEmpty()) {
                    item(
                        key = "search_placeholder",
                        span = { GridItemSpan(maxCurrentLineSpan) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nhập từ khóa để tìm kiếm sản phẩm",
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
                            text = "Kết quả tìm kiếm",
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
                                    text = "Đang tải thêm...",
                                    style = CustomTypography.TextRegular.merge(
                                        color = colorResource(R.color.colorSystem_normal_text),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }

                    // Show empty state
                    if (searchResults.itemCount == 0 && searchResults.loadState.refresh !is LoadState.Loading) {
                        item(
                            key = "empty_state",
                            span = { GridItemSpan(maxCurrentLineSpan) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Không tìm thấy sản phẩm",
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