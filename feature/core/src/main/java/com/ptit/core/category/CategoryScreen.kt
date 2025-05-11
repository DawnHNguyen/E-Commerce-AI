@file:Suppress("DEPRECATION")

package com.ptit.core.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.ptit.common.R
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.component.ProductEmptyState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.navigation.destination.ProductsByCategoryRoute


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    navController: NavController,
) {
    LocalBottomNavigationVisibility.current.value = true

    val viewModel: CategoryViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Danh mục sản phẩm",
                        style = CustomTypography.TextBold,
                        fontSize = 20.sp,
                        color = colorResource(id = R.color.colorSystem_greyscale_0_white)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(id = R.color.colorSystem_heading_button)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(colorResource(id = R.color.colorSystem_background_level_0))
        ) {
            when {
                uiState.isLoading -> {
                    FullScreenProgressBar()
                }
                uiState.error != null -> {
                    ProductEmptyState(
                        message = "Lỗi tải danh mục: ${uiState.error}",
                        buttonText = "Thử lại",
                        onActionClick = { viewModel.fetchCategories() },
                    )
                }
                uiState.categories.isEmpty() && !uiState.isLoading -> {
                    ProductEmptyState(
                        message = "Không có danh mục nào.",
                        buttonText = "Tải lại",
                        onActionClick = { viewModel.fetchCategories() },
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp), // Áp dụng padding ở đây thay vì cho MaxSizeColumn
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize() // Grid chiếm toàn bộ không gian còn lại
                    ) {
                        items(uiState.categories, key = { it.id }) { category ->
                            CategoryCard(categoryItem = category) {
                                navController.navigate(
                                    ProductsByCategoryRoute(
                                        categoryId = category.id,
                                        categoryDisplayName = category.displayName
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


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CategoryCard(
    categoryItem: CategoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f) // Giữ tỷ lệ cho card
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), // Column fill toàn bộ Card
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Căn giữa các item con trong Column
        ) {
            GlideImage(
                model = categoryItem.imageUrl,
                contentDescription = categoryItem.displayName,
                modifier = Modifier
                    .fillMaxWidth() // Ảnh chiếm toàn bộ chiều rộng
                    .weight(1f) // Cho ảnh chiếm phần không gian còn lại sau khi Text đã chiếm
                    .padding(8.dp) // Padding cho ảnh
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                loading = placeholder {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }
                },
                failure = placeholder {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorResource(id = R.color.colorSystem_greyscale_200)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Lỗi ảnh",
                            color = colorResource(id = R.color.colorSystem_greyscale_600)
                        )
                    }
                }
            )

            Text(
                text = categoryItem.displayName,
                style = CustomTypography.TextSemiBold,
                fontSize = 16.sp,
                color = colorResource(id = R.color.colorSystem_heading_button),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 12.dp), // Padding cho text
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}