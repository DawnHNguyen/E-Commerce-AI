package com.ptit.core.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.navigation.destination.ProductsByCategoryRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    navController: NavController,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    LocalBottomNavigationVisibility.current.value = true

    val uiState by viewModel.uiState.collectAsState()

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
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Làm mới",
                            tint = colorResource(id = R.color.colorSystem_greyscale_0_white)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(id = R.color.colorSystem_heading_button)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is CategoryUiState.Initial, CategoryUiState.Loading -> {
                    LoadingContent()
                }
                is CategoryUiState.Success -> {
                    if (state.categories.isEmpty()) {
                        EmptyContent()
                    } else {
                        CategoryGridContent(
                            categories = state.categories,
                            navController = navController
                        )
                    }
                }
                is CategoryUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.refresh() }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryGridContent(
    categories: List<CategoryDomainEntity>,
    navController: NavController
) {
    // Filter out categories with empty or null id
    val validCategories = categories.filter { it.id.isNotBlank() }

    MaxSizeColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorSystem_background_level_0))
            .padding(16.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                items = validCategories,
                key = { category ->
                    // Ensure unique non-empty key
                    category.id.ifBlank { "category_${category.hashCode()}" }
                }
            ) { category ->
                CategoryCard(category = category) {
                    navController.navigate(
                        ProductsByCategoryRoute(
                            categoryId = category.id,
                            categoryDisplayName = category.name
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CategoryCard(
    category: CategoryDomainEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!category.logo.isNullOrEmpty()) {
                GlideImage(
                    model = category.logo,
                    contentDescription = category.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp)
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
                                text = category.name.take(2).uppercase(),
                                style = CustomTypography.TextBold,
                                fontSize = 32.sp,
                                color = colorResource(id = R.color.colorSystem_heading_button)
                            )
                        }
                    }
                )
            } else {
                // Default icon when no logo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorResource(id = R.color.colorSystem_greyscale_200)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.name.take(2).uppercase(),
                        style = CustomTypography.TextBold,
                        fontSize = 32.sp,
                        color = colorResource(id = R.color.colorSystem_heading_button)
                    )
                }
            }

            Text(
                text = category.name,
                style = CustomTypography.TextSemiBold,
                fontSize = 16.sp,
                color = colorResource(id = R.color.colorSystem_heading_button),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorSystem_background_level_0)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = colorResource(id = R.color.colorSystem_heading_button)
            )
            Text(
                text = "Đang tải danh mục...",
                style = CustomTypography.TextRegular,
                fontSize = 16.sp,
                color = colorResource(id = R.color.colorSystem_greyscale_600)
            )
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorSystem_background_level_0)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "📦",
                fontSize = 64.sp
            )
            Text(
                text = "Chưa có danh mục nào",
                style = CustomTypography.TextBold,
                fontSize = 20.sp,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )
            Text(
                text = "Các danh mục sản phẩm sẽ hiển thị tại đây",
                style = CustomTypography.TextRegular,
                fontSize = 14.sp,
                color = colorResource(id = R.color.colorSystem_greyscale_600),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorSystem_background_level_0)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "⚠️",
                fontSize = 64.sp
            )
            Text(
                text = "Đã xảy ra lỗi",
                style = CustomTypography.TextBold,
                fontSize = 20.sp,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )
            Text(
                text = message,
                style = CustomTypography.TextRegular,
                fontSize = 14.sp,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.colorSystem_heading_button)
                )
            ) {
                Text("Thử lại", style = CustomTypography.TextSemiBold)
            }
        }
    }
}
