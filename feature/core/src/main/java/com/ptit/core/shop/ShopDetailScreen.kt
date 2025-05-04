package com.ptit.core.shop

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.MyCrossFade
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.domain.entity.shop.ShopDomainEntity
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess

@Composable
fun ShopDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditShop: () -> Unit,
    onNavigateToProductList: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val viewModel = hiltViewModel<ShopViewModel>()
    val uiModel = viewModel.uiModel.collectAsStateWithLifecycle()
    val isLoading = rememberState { false }


    LaunchedEffect(Unit) {
        viewModel.fetchMyShopDetails()
        lifecycleOwner.safeCollectFlow(viewModel.shopDetailsState) {
            it
                .onLoading {
                isLoading.value = true
            }
                .onError { error ->
                    isLoading.value = false
                    Toast.makeText(
                        context,
                        "Không thể tải thông tin cửa hàng: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onSuccess { shop ->
                    isLoading.value = false
                }
        }
    }

    MaxSizeColumn(
        modifier = Modifier
            .background(colorResource(R.color.colorSystem_background_level_0))
            .statusBarsPadding()
    ) {
        // Top bar
        MaxWidthRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = colorResource(id = R.color.colorSystem_heading_button)
                )
            }

            Text(
                text = "Chi tiết cửa hàng",
                style = CustomTypography.TextBold,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 40.dp),
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )
        }

        // Content
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Shop Profile
            ShopProfileSection(
                shop = ShopDomainEntity(
                    name = uiModel.value.shop.name,
                    description = uiModel.value.shop.description,
                    address = uiModel.value.shop.address,
                    avatar = uiModel.value.shop.avatar
                ),
                onEditClick = { onNavigateToEditShop() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Shop Stats and Information
            ShopInfoSection(
                shopName = uiModel.value.shop.name,
                shopDescription = uiModel.value.shop.description,
                shopAddress = uiModel.value.shop.address,
                totalProduct = uiModel.value.shop.totalProduct ?: 0,
                totalOrder = uiModel.value.shop.totalOrder ?: 0
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Add Product Button
            Button(
                onClick = onNavigateToProductList,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.colorSystem_heading_button)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Quản lý sản phẩm",
                    style = CustomTypography.TextSemiBold,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.colorSystem_greyscale_0_white),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    // Show loading indicator when processing
    if (isLoading.value) {
        FullScreenProgressBar()
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ShopProfileSection(
    shop: ShopDomainEntity,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Chỉnh sửa cửa hàng",
                    tint = colorResource(id = R.color.colorSystem_heading_button)
                )
            }

            // Content column centered in the box
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Shop logo/image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.colorSystem_background_level_2))
                        .border(2.dp, colorResource(R.color.colorSystem_stroke), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (shop.avatar.isNotEmpty()) {
                        GlideImage(
                            model = shop.avatar,
                            contentDescription = "Logo cửa hàng",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop,
                            transition = MyCrossFade
                        ) {
                            it.centerCrop()
                        }
                    } else {
                        Text(
                            text = if (shop.name.isNotEmpty()) shop.name.first().toString().uppercase() else "?",
                            style = CustomTypography.TextBold,
                            fontSize = 40.sp,
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Shop name
                Text(
                    text = shop.name,
                    style = CustomTypography.TextBold,
                    fontSize = 22.sp,
                    color = colorResource(id = R.color.colorSystem_heading_button),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
@Composable
private fun ShopInfoSection(
    shopName: String,
    shopDescription: String,
    shopAddress: String,
    totalProduct: Int = 0,
    totalOrder: Int = 0
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Information title
            Text(
                text = "Thông tin cửa hàng",
                style = CustomTypography.TextBold,
                fontSize = 18.sp,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Shop description
            Text(
                text = "Mô tả",
                style = CustomTypography.TextSemiBold,
                fontSize = 16.sp,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )

            Text(
                text = shopDescription.ifEmpty { "Chưa có mô tả" },
                style = CustomTypography.TextRegular,
                fontSize = 14.sp,
                color = colorResource(id = R.color.colorSystem_normal_text),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            HorizontalDivider(
                color = colorResource(id = R.color.colorSystem_text_button),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Shop address
            Text(
                text = "Địa chỉ",
                style = CustomTypography.TextSemiBold,
                fontSize = 16.sp,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )

            Text(
                text = shopAddress.ifEmpty { "Chưa có địa chỉ" },
                style = CustomTypography.TextRegular,
                fontSize = 14.sp,
                color = colorResource(id = R.color.colorSystem_normal_text),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            HorizontalDivider(
                color = colorResource(id = R.color.colorSystem_text_button),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Shop statistics (can be expanded later)
            Text(
                text = "Thống kê",
                style = CustomTypography.TextSemiBold,
                fontSize = 16.sp,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )

            Spacer(modifier = Modifier.height(8.dp))

            MaxWidthRow(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                StatisticItem(
                    title = "Sản phẩm",
                    value = totalProduct.toString(),
                    modifier = Modifier.weight(1f)
                )

                StatisticItem(
                    title = "Đơn hàng",
                    value = totalOrder.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = CustomTypography.TextBold,
            fontSize = 20.sp,
            color = colorResource(id = R.color.colorSystem_heading_button)
        )

        Text(
            text = title,
            style = CustomTypography.TextRegular,
            fontSize = 12.sp,
            color = colorResource(id = R.color.colorSystem_normal_text)
        )
    }
}