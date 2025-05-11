@file:Suppress("DEPRECATION")

package com.ptit.core.category

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Star // Đổi sang filled để giống các màn hình khác
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color // Thêm import
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration // Thêm import
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.ptit.common.R
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.MyCrossFade
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.component.ProductEmptyState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.product.ProductDomainEntity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsByCategoryScreen(
    navController: NavController,
    navigateToProductDetail: (String) -> Unit,
) {
    LocalBottomNavigationVisibility.current.value = false

    val viewModel: ProductsByCategoryViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = uiState.categoryDisplayName.ifEmpty { uiState.categoryDisplayName },
                        style = CustomTypography.TextBold,
                        fontSize = 20.sp,
                        color = colorResource(id = R.color.colorSystem_greyscale_0_white),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
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
                .padding(paddingValues)
                .fillMaxSize()
                .background(colorResource(id = R.color.colorSystem_background_level_0))
        ) {
            when {
                uiState.isLoading -> {
                    FullScreenProgressBar()
                }

                uiState.error != null && uiState.products.isEmpty() -> { // Chỉ hiện empty state nếu không có sản phẩm nào VÀ có lỗi
                    ProductEmptyState(
                        message = uiState.error ?: "Không có sản phẩm nào trong danh mục này.",
                        buttonText = "Quay lại Danh mục",
                        onActionClick = { navController.popBackStack() }
                    )
                }

                uiState.products.isEmpty() -> {
                    ProductEmptyState(
                        message = "Không có sản phẩm nào trong danh mục '${uiState.categoryDisplayName}'.",
                        buttonText = "Quay lại Danh mục",
                        onActionClick = { navController.popBackStack() }
                    )
                }

                else -> {
                    ProductGrid(products = uiState.products, navigateToProductDetail = navigateToProductDetail)
                    if (uiState.error != null) {
                        Text(
                            text = "Lưu ý: ${uiState.error}",
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .background(Color.Yellow.copy(alpha = 0.5f))
                                .padding(8.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductGrid(
    products: List<ProductDomainEntity>,
    navigateToProductDetail: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(products, key = { it.id }) { product ->
            ProductItemCard( // Đã sửa đổi ProductItemCard bên dưới
                product = product,
                onClickProduct = { navigateToProductDetail(product.id) }
            )
        }
    }
}


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ProductItemCard(
    product: ProductDomainEntity,
    onClickProduct: () -> Unit,
) {
    val ratingAnnotatedString = remember(product.rating) {
        buildAnnotatedString {
            appendInlineContent(id = "ratingIcon", alternateText = "[star]")
            append(" ")
            append(String.format("%.1f", product.rating))
        }
    }
    val ratingInlineContentMap = mapOf(
        "ratingIcon" to InlineTextContent(
            Placeholder(14.sp, 14.sp, PlaceholderVerticalAlign.TextCenter)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Rating",
                tint = colorResource(R.color.colorSystem_tint_yellow)
            )
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // .aspectRatio(0.75f) // Bỏ aspectRatio cố định để card tự điều chỉnh chiều cao
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClickProduct),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.colorSystem_background_level_2)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth() // Cho phép Column mở rộng theo chiều rộng
        ) {
            // Box để chứa ảnh và badge giảm giá
            Box {
                GlideImage(
                    model = product.image.ifEmpty { Icons.Default.BrokenImage },
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth() // Ảnh chiếm toàn bộ chiều rộng của Card
                        .aspectRatio(1f), // Giữ ảnh vuông
                    contentScale = ContentScale.Crop,
                    transition = MyCrossFade,
                    loading = placeholder {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    },
                    failure = placeholder {
                        Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                            Text("Lỗi ảnh")
                        }
                    }
                )

                // Badge giảm giá, hiển thị ở góc trên cùng bên phải của ảnh
                if (product.hasDiscount.value) {
                    Text(
                        text = "-${product.discountPercent.value}%",
                        style = CustomTypography.TextMedium.merge(
                            color = Color.White,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier
                            .align(Alignment.TopEnd) // Căn chỉnh badge
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
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .defaultMinSize(minHeight = 100.dp), // Đặt chiều cao tối thiểu cho phần text
                verticalArrangement = Arrangement.SpaceBetween // Phân bố không gian
            ) {
                Text(
                    text = product.name,
                    style = CustomTypography.TextRegular,
                    fontSize = 14.sp,
                    color = colorResource(R.color.colorSystem_heading_button),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                // Spacer nhỏ nếu cần thêm không gian trước giá
                Spacer(modifier = Modifier.height(4.dp))

                // Giá sản phẩm
                Column { // Bọc giá và giá gốc trong một Column để chúng gần nhau hơn
                    Text(
                        text = "${product.price}đ",
                        style = CustomTypography.TextSemiBold,
                        fontSize = 15.sp,
                        color = colorResource(R.color.colorSystem_tint_red)
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

                Spacer(modifier = Modifier.height(6.dp)) // Khoảng cách giữa giá và dòng rating/sold

                MaxWidthRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = ratingAnnotatedString,
                        inlineContent = ratingInlineContentMap,
                        style = CustomTypography.TextRegular,
                        fontSize = 12.sp,
                        color = colorResource(R.color.colorSystem_normal_text)
                    )

                    Text(
                        text = "Đã bán: ${product.sold}",
                        style = CustomTypography.TextRegular,
                        fontSize = 12.sp,
                        color = colorResource(R.color.colorSystem_normal_text)
                    )
                }
            }
        }
    }
}