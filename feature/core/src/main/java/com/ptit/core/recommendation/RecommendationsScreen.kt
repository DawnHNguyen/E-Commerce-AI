package com.ptit.core.recommendation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.toPriceFormat
import com.ptit.core.home.HomeViewModel
import com.ptit.domain.entity.recommendation.RecommendedProductDomainEntity
import com.ptit.domain.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(
    navigateBack: () -> Unit,
    navigateToProductDetail: (String) -> Unit
) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val recommendationsState by viewModel.recommendationsState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Đề xuất cho bạn",
                        style = CustomTypography.TextBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = colorResource(R.color.colorSystem_background_level_0))
        ) {
            when (recommendationsState) {
                is Resource.Success -> {
                    val recommendations = (recommendationsState as Resource.Success).data
                    if (recommendations.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                count = recommendations.size,
                                key = { index -> recommendations[index].id }
                            ) { index ->
                                RecommendationProductItem(
                                    product = recommendations[index],
                                    onClick = { navigateToProductDetail(recommendations[index].id) }
                                )
                            }
                        }
                    } else {
                        // Empty state
                        EmptyRecommendationsState()
                    }
                }
                is Resource.Loading -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(R.color.colorSystem_heading_button)
                        )
                    }
                }
                is Resource.Error -> {
                    // Error state
                    ErrorRecommendationsState(
                        onRetry = { viewModel.fetchRecommendations() }
                    )
                }
                is Resource.Idle -> {
                    // Idle state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Đang tải đề xuất...",
                            style = CustomTypography.TextRegular,
                            color = colorResource(R.color.colorSystem_greyscale_500)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun RecommendationProductItem(
    product: RecommendedProductDomainEntity,
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
            // Product Image
            GlideImage(
                model = product.images.firstOrNull() ?: "",
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            ) {
                it.centerCrop()
            }

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 100.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Product Name
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
                Column {
                    Text(
                        text = product.basePrice.toPriceFormat(),
                        style = CustomTypography.TextSemiBold,
                        fontSize = 15.sp,
                        color = colorResource(id = R.color.colorSystem_tint_red)
                    )
                    if (product.virtualPrice > product.basePrice) {
                        Text(
                            text = product.virtualPrice.toPriceFormat(),
                            style = CustomTypography.TextRegular.copy(
                                textDecoration = TextDecoration.LineThrough,
                                fontSize = 12.sp,
                                color = colorResource(id = R.color.colorSystem_greyscale_500)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Category
                product.categoryName?.let { categoryName ->
                    Text(
                        text = categoryName,
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
fun EmptyRecommendationsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Chưa có đề xuất phù hợp",
                style = CustomTypography.TextMedium,
                fontSize = 16.sp,
                color = colorResource(R.color.colorSystem_normal_text),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hãy khám phá thêm sản phẩm để nhận được đề xuất phù hợp nhất",
                style = CustomTypography.TextRegular,
                fontSize = 14.sp,
                color = colorResource(R.color.colorSystem_greyscale_500),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorRecommendationsState(
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Không thể tải đề xuất",
                style = CustomTypography.TextMedium,
                fontSize = 16.sp,
                color = colorResource(R.color.colorSystem_normal_text),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button)
                )
            ) {
                Text(
                    text = "Thử lại",
                    style = CustomTypography.TextMedium,
                    color = Color.White
                )
            }
        }
    }
}

