package com.ptit.core.review

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.toPriceFormat
import com.ptit.domain.utils.Resource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun CreateReviewScreen(
    orderId: String,
    productId: String,
    productName: String,
    productImage: String,
    productPrice: Int,
    productSkuValue: String,
    onBack: () -> Unit
) {
    LocalBottomNavigationVisibility.current.value = false

    val viewModel: ReviewViewModel = hiltViewModel()
    val createReviewState by viewModel.createReviewState.collectAsStateWithLifecycle()
    val updateReviewState by viewModel.updateReviewState.collectAsStateWithLifecycle()
    val existingReviews by viewModel.existingReviews.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Load reviews on screen open to populate existingReviews map
    LaunchedEffect(orderId, productId) {
        android.util.Log.d("CreateReviewScreen", "LaunchedEffect: Loading reviews for orderId=$orderId, productId=$productId")
        viewModel.checkReviewExists(orderId, productId)
    }

    // Get existing review if any
    val key = "${orderId}_${productId}"
    val existingReview = existingReviews[key]
    val isEditMode = existingReview != null

    // Initialize with existing data or defaults
    var rating by remember(existingReview) { mutableStateOf(existingReview?.rating ?: 5) }
    var content by remember(existingReview) { mutableStateOf(existingReview?.content ?: "") }
    var isLoading by remember { mutableStateOf(false) }

    // Handle create review state
    LaunchedEffect(createReviewState) {
        when (createReviewState) {
            is Resource.Loading -> {
                isLoading = true
            }
            is Resource.Success -> {
                isLoading = false
                Toast.makeText(context, "Đánh giá thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateReviewState()
                onBack()
            }
            is Resource.Error -> {
                isLoading = false
                val errorResponse = (createReviewState as Resource.Error).error
                val errorMessage = errorResponse.error?.message ?: "Không xác định"
                Toast.makeText(context, "Lỗi: $errorMessage", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateReviewState()
            }
            else -> {}
        }
    }

    // Handle update review state
    LaunchedEffect(updateReviewState) {
        when (updateReviewState) {
            is Resource.Loading -> {
                isLoading = true
            }
            is Resource.Success -> {
                isLoading = false
                Toast.makeText(context, "Cập nhật đánh giá thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateReviewState()
                onBack()
            }
            is Resource.Error -> {
                isLoading = false
                val errorResponse = (updateReviewState as Resource.Error).error
                val errorMessage = errorResponse.error?.message ?: "Không xác định"
                Toast.makeText(context, "Lỗi: $errorMessage", Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateReviewState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isEditMode) "Chỉnh sửa đánh giá" else "Đánh giá sản phẩm",
                            style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(R.color.colorSystem_greyscale_0_white)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button),
                    titleContentColor = colorResource(R.color.colorSystem_greyscale_0_white)
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(R.color.colorSystem_background_level_0))
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Product Info
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorResource(R.color.colorSystem_background_level_2))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlideImage(
                            model = productImage,
                            contentDescription = productName,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = productName,
                                style = CustomTypography.TextSemiBold,
                                color = colorResource(R.color.colorSystem_heading_button)
                            )
                            Text(
                                text = productSkuValue,
                                style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                                color = colorResource(R.color.colorSystem_normal_text)
                            )
                            Text(
                                text = productPrice.toPriceFormat(),
                                style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                                color = colorResource(R.color.colorSystem_heading_button)
                            )
                        }
                    }
                }

                // Rating Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorResource(R.color.colorSystem_background_level_2))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Chất lượng sản phẩm",
                            style = CustomTypography.TextSemiBold,
                            color = colorResource(R.color.colorSystem_heading_button)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Star $i",
                                    tint = if (i <= rating) Color(0xFFFFB800) else Color.Gray,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable { rating = i }
                                )
                            }
                        }
                        Text(
                            text = when (rating) {
                                1 -> "Rất tệ"
                                2 -> "Tệ"
                                3 -> "Bình thường"
                                4 -> "Tốt"
                                5 -> "Rất tốt"
                                else -> ""
                            },
                            style = CustomTypography.TextRegular,
                            color = colorResource(R.color.colorSystem_normal_text)
                        )
                    }
                }

                // Review Content
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorResource(R.color.colorSystem_background_level_2))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Chia sẻ đánh giá của bạn",
                            style = CustomTypography.TextSemiBold,
                            color = colorResource(R.color.colorSystem_heading_button)
                        )
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            placeholder = { Text("Hãy chia sẻ trải nghiệm của bạn về sản phẩm này...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                }

                // Submit Button
                item {
                    FilledButton(
                        text = if (isLoading) {
                            "Đang gửi..."
                        } else {
                            if (isEditMode) "Cập nhật đánh giá" else "Gửi đánh giá"
                        },
                        onClick = {
                            if (content.isBlank()) {
                                Toast.makeText(context, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show()
                                return@FilledButton
                            }

                            android.util.Log.d("CreateReviewScreen", "Submit clicked - isEditMode: $isEditMode, existingReview: ${existingReview?.id}")

                            if (isEditMode && existingReview != null) {
                                // Update existing review
                                android.util.Log.d("CreateReviewScreen", "Calling updateReview with reviewId: ${existingReview.id}")
                                viewModel.updateReview(
                                    reviewId = existingReview.id,
                                    content = content,
                                    rating = rating,
                                    productId = productId,
                                    orderId = orderId,
                                    medias = emptyList()
                                )
                            } else {
                                // Create new review
                                android.util.Log.d("CreateReviewScreen", "Calling createReview")
                                viewModel.createReview(
                                    content = content,
                                    rating = rating,
                                    productId = productId,
                                    orderId = orderId,
                                    medias = emptyList()
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && content.isNotBlank()
                    )
                }
            }

            if (isLoading) {
                FullScreenProgressBar()
            }
        }
    }
}

