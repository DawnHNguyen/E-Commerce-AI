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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder // Thêm import này
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography

// Data class for Category Item
data class CategoryItem(
    val id: String,
    val name: String,
    val imageUrl: String,
    // val route: String // Add if navigation from category item is needed
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(navController: NavController) {
    LocalBottomNavigationVisibility.current.value = true
    val context = LocalContext.current

    val categories = listOf(
        CategoryItem("1", "Thời trang", "https://cdn-icons-png.flaticon.com/512/3205/3205438.png"),
        CategoryItem("2", "Đồ gia dụng", "https://cdn-icons-png.flaticon.com/512/7540/7540904.png"),
        CategoryItem("3", "Nội thất", "https://cdn-icons-png.flaticon.com/512/1434/1434247.png"),
        CategoryItem("4", "Mỹ phẩm", "https://cdn-icons-png.flaticon.com/512/3501/3501241.png")
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
                columns = GridCells.Fixed(2), // 2 columns in the grid
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(categories, key = { it.id }) { category ->
                    CategoryCard(categoryItem = category) {
                        // Handle category item click, e.g., navigate to a product list for that category
                        // navController.navigate("products_by_category/${category.id}")
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
                contentDescription = categoryItem.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                loading = placeholder { // Hiển thị khi đang tải
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }
                },
                failure = placeholder { // Hiển thị khi tải lỗi
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorResource(id = R.color.colorSystem_greyscale_200)), // Màu nền placeholder
                        contentAlignment = Alignment.Center
                    ) {
                        // Bạn có thể thêm Icon lỗi ở đây nếu muốn
                        // Icon(imageVector = Icons.Filled.BrokenImage, contentDescription = "Lỗi tải ảnh")
                        Text("Lỗi ảnh", color = colorResource(id = R.color.colorSystem_greyscale_600))
                    }
                }
            )

            Text(
                text = categoryItem.name,
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