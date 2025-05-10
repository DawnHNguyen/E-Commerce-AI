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
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.navigation.destination.ProductsByCategoryRoute // Import route mới

// Data class for Category Item
data class CategoryItem(
    val id: String,
    val name: String, // Đây sẽ là tên dùng để lọc
    val displayName: String, // Tên hiển thị, có thể có dấu
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(navController: NavController) {
    LocalBottomNavigationVisibility.current.value = true

    // Cập nhật danh sách categories
    val categories = listOf(
        CategoryItem("67f1ebef40ab575580040f42", "Thời trang", "Thời trang", "https://cdn-icons-png.flaticon.com/512/3205/3205438.png"),
        CategoryItem("67fe76b4a2e13b000d81e942", "Đồ gia dụng", "Đồ gia dụng", "https://cdn-icons-png.flaticon.com/512/7540/7540904.png"),
        CategoryItem("67f1ebe740ab575580040f41", "Nội thất", "Nội thất", "https://cdn-icons-png.flaticon.com/512/1434/1434247.png"),
        CategoryItem("67fe764aa2e13b000d81e941", "Mỹ phẩm", "Mỹ phẩm", "https://cdn-icons-png.flaticon.com/512/3501/3501241.png"),
        CategoryItem("67f1ebde40ab575580040f40", "Điện tử", "Điện tử", "https://cdn-icons-png.flaticon.com/512/3696/3696504.png"),
        CategoryItem("67fe7b38a2e13b000d81e947", "Thực phẩm", "Thực phẩm", "https://cdn-icons-png.flaticon.com/512/7910/7910878.png"),
    )

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
        MaxSizeColumn(
            modifier = Modifier
                .padding(paddingValues)
                .background(colorResource(id = R.color.colorSystem_background_level_0))
                .padding(16.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(categories, key = { it.id }) { category ->
                    CategoryCard(categoryItem = category) {
                        // Điều hướng đến màn hình danh sách sản phẩm theo category
                        navController.navigate(
                            ProductsByCategoryRoute(
                                categoryId = category.id, // Truyền tên category để lọc
                                categoryDisplayName = category.displayName // Truyền tên hiển thị cho AppBar
                            )
                        )
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
            GlideImage(
                model = categoryItem.imageUrl,
                contentDescription = categoryItem.displayName,
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
                        Text("Lỗi ảnh", color = colorResource(id = R.color.colorSystem_greyscale_600))
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
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}