package com.ptit.core.review

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.review.ReviewDomainEntity
import com.ptit.domain.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllReviewsScreen(
    productId: String,
    onBack: () -> Unit
) {
    LocalBottomNavigationVisibility.current.value = false

    val reviewViewModel: ReviewViewModel = hiltViewModel()
    val reviewsState by reviewViewModel.reviewsState.collectAsStateWithLifecycle()

    var selectedRatingFilter by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(productId) {
        reviewViewModel.getReviews(productId, page = 1, limit = 100)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Đánh giá sản phẩm",
                        style = CustomTypography.TextBold.copy(fontSize = 20.sp)
                    )
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
        when (val state = reviewsState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    FullScreenProgressBar()
                }
            }
            is Resource.Success -> {
                val allReviews = state.data.data
                val filteredReviews = if (selectedRatingFilter != null) {
                    allReviews.filter { it.rating == selectedRatingFilter }
                } else {
                    allReviews
                }

                AllReviewsContent(
                    reviews = allReviews,
                    filteredReviews = filteredReviews,
                    selectedRatingFilter = selectedRatingFilter,
                    onRatingFilterChanged = { selectedRatingFilter = it },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Không thể tải đánh giá",
                            style = CustomTypography.TextMedium,
                            color = colorResource(R.color.colorSystem_normal_text)
                        )
                        TextButton(onClick = {
                            reviewViewModel.getReviews(productId, page = 1, limit = 100)
                        }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AllReviewsContent(
    reviews: List<ReviewDomainEntity>,
    filteredReviews: List<ReviewDomainEntity>,
    selectedRatingFilter: Int?,
    onRatingFilterChanged: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate statistics
    val avgRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average()
    } else {
        0.0
    }

    val ratingCounts = (1..5).map { rating ->
        rating to reviews.count { it.rating == rating }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.colorSystem_background_level_0))
    ) {
        // Rating Summary Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.colorSystem_background_level_2)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left: Average Rating
                Column(
                    modifier = Modifier.weight(0.4f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "%.1f".format(avgRating),
                        style = CustomTypography.TextBold.copy(fontSize = 48.sp),
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < avgRating.toInt())
                                    Icons.Filled.Star
                                else
                                    Icons.Outlined.StarOutline,
                                contentDescription = null,
                                tint = if (index < avgRating.toInt())
                                    Color(0xFFFFB800)
                                else
                                    colorResource(R.color.colorSystem_stroke),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = "${reviews.size} đánh giá",
                        style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                        color = colorResource(R.color.colorSystem_normal_text)
                    )
                }

                // Right: Rating Distribution
                Column(
                    modifier = Modifier.weight(0.6f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ratingCounts.reversed().forEach { (rating, count) ->
                        RatingFilterRow(
                            rating = rating,
                            count = count,
                            totalCount = reviews.size,
                            isSelected = selectedRatingFilter == rating,
                            onClick = {
                                onRatingFilterChanged(if (selectedRatingFilter == rating) null else rating)
                            }
                        )
                    }
                }
            }
        }

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedRatingFilter == null,
                onClick = { onRatingFilterChanged(null) },
                label = {
                    Text(
                        "Tất cả (${reviews.size})",
                        style = CustomTypography.TextMedium.copy(fontSize = 14.sp)
                    )
                }
            )

            (5 downTo 1).forEach { rating ->
                val count = reviews.count { it.rating == rating }
                if (count > 0) {
                    FilterChip(
                        selected = selectedRatingFilter == rating,
                        onClick = { onRatingFilterChanged(rating) },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "$rating",
                                    style = CustomTypography.TextMedium.copy(fontSize = 14.sp)
                                )
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "($count)",
                                    style = CustomTypography.TextMedium.copy(fontSize = 14.sp)
                                )
                            }
                        }
                    )
                }
            }
        }

        // Reviews List
        if (filteredReviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedRatingFilter != null) {
                        "Không có đánh giá $selectedRatingFilter sao"
                    } else {
                        "Chưa có đánh giá nào"
                    },
                    style = CustomTypography.TextRegular,
                    color = colorResource(R.color.colorSystem_normal_text),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredReviews.size) { index ->
                    ReviewCard(review = filteredReviews[index])
                }
            }
        }
    }
}

@Composable
fun RatingFilterRow(
    rating: Int,
    count: Int,
    totalCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected)
                    colorResource(R.color.colorSystem_heading_button).copy(alpha = 0.1f)
                else
                    Color.Transparent
            )
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rating number with star
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rating",
                style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFB800),
                modifier = Modifier.size(14.dp)
            )
        }

        // Progress bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colorResource(R.color.colorSystem_stroke))
        ) {
            val percentage = if (totalCount > 0) count.toFloat() / totalCount else 0f
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage)
                    .background(Color(0xFFFFB800))
            )
        }

        // Count
        Text(
            text = "($count)",
            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
            color = colorResource(R.color.colorSystem_normal_text),
            modifier = Modifier.widthIn(min = 32.dp)
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ReviewCard(review: ReviewDomainEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.colorSystem_background_level_2)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // User info and rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar
                GlideImage(
                    model = review.user?.avatar ?: "",
                    contentDescription = review.user?.name,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorResource(R.color.colorSystem_stroke)),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.user?.name ?: "Anonymous",
                        style = CustomTypography.TextSemiBold.copy(fontSize = 14.sp),
                        color = colorResource(R.color.colorSystem_heading_button)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < review.rating)
                                    Icons.Filled.Star
                                else
                                    Icons.Outlined.StarOutline,
                                contentDescription = null,
                                tint = if (index < review.rating)
                                    Color(0xFFFFB800)
                                else
                                    colorResource(R.color.colorSystem_stroke),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Review content
            Text(
                text = review.content,
                style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                color = colorResource(R.color.colorSystem_normal_text),
                lineHeight = 20.sp
            )

            // Review images (if any)
            review.medias?.let { medias ->
                if (medias.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        medias.take(3).forEach { media ->
                            GlideImage(
                                model = media.url,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // Review date
            Text(
                text = formatReviewDate(review.createdAt),
                style = CustomTypography.TextRegular.copy(fontSize = 12.sp),
                color = colorResource(R.color.colorSystem_greyscale_400)
            )
        }
    }
}

fun formatReviewDate(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

