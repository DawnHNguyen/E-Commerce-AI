package com.ptit.core.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.component.CustomPullToRefreshBox
import com.ptit.common.presentation.component.CustomSearchBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.component.noRippleClickable
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.core.home.component.ProductItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToSearch: () -> Unit,
    navigateToProductDetail: (String) -> Unit,
) {
    LocalBottomNavigationVisibility.current.value = true

    val viewModel = hiltViewModel<HomeViewModel>()
    val paginatedRecommendedProduct = viewModel.paginatedRecommendedProduct.collectAsLazyPagingItems()

    CustomPullToRefreshBox(
        modifier = Modifier.background(color = colorResource(R.color.colorSystem_background_level_0)),
        isRefreshing =
            paginatedRecommendedProduct.loadState.refresh == LoadState.Loading,
        onRefresh = {
            paginatedRecommendedProduct.refresh()
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
                    value = "",
                    onValueChange = {},
                    hint = "Tìm kiếm sản phẩm",
                    modifier = Modifier
                        .weight(1f)
                        .noRippleClickable(onClick = navigateToSearch),
                    enabled = false,
                )
            }

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
                        ProductItem(
                            product = product,
                            onClickProduct = { navigateToProductDetail(product.id) }
                        )
                    }
                }
            }
        }
    }
}