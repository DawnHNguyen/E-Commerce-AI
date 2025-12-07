package com.ptit.core.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FilledTextField
import com.ptit.common.presentation.component.NeutralButton
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.product.CategoryDomainEntity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    categories: List<CategoryDomainEntity>,
    selectedCategoryIds: List<String>,
    minPrice: Int?,
    maxPrice: Int?,
    selectedSortBy: String,
    selectedOrderBy: String,
    onDismiss: () -> Unit,
    onApply: (
        categoryIds: List<String>,
        minPrice: Int?,
        maxPrice: Int?,
        sortBy: String,
        orderBy: String
    ) -> Unit
) {
    var tempCategoryIds by remember { mutableStateOf(selectedCategoryIds) }
    var tempMinPrice by remember { mutableStateOf(minPrice?.toString() ?: "") }
    var tempMaxPrice by remember { mutableStateOf(maxPrice?.toString() ?: "") }
    var tempSortBy by remember { mutableStateOf(selectedSortBy) }
    var tempOrderBy by remember { mutableStateOf(selectedOrderBy) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colorResource(R.color.colorSystem_background_level_0),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bộ lọc",
                    style = CustomTypography.TextBold.copy(fontSize = 22.sp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colorResource(R.color.colorSystem_heading_button)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = colorResource(R.color.colorSystem_greyscale_200)
            )

            // Content
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Categories Section
                item {
                    FilterSection(title = "Danh mục") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.chunked(3).forEach { rowCategories ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowCategories.forEach { category ->
                                        FilterChipItem(
                                            label = category.name,
                                            isSelected = tempCategoryIds.contains(category.id),
                                            onClick = {
                                                tempCategoryIds = if (tempCategoryIds.contains(category.id)) {
                                                    tempCategoryIds - category.id
                                                } else {
                                                    tempCategoryIds + category.id
                                                }
                                            },
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Price Range Section
                item {
                    FilterSection(title = "Khoảng giá") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilledTextField(
                                value = tempMinPrice,
                                onValueChange = { tempMinPrice = it },
                                hint = "Tối thiểu",
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "-",
                                style = CustomTypography.TextMedium.copy(fontSize = 16.sp),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            FilledTextField(
                                value = tempMaxPrice,
                                onValueChange = { tempMaxPrice = it },
                                hint = "Tối đa",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Sort Section
                item {
                    FilterSection(title = "Sắp xếp theo") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SortOption(
                                label = "Mới nhất",
                                isSelected = tempSortBy == "createdAt" && tempOrderBy == "desc",
                                onClick = {
                                    tempSortBy = "createdAt"
                                    tempOrderBy = "desc"
                                }
                            )
                            SortOption(
                                label = "Cũ nhất",
                                isSelected = tempSortBy == "createdAt" && tempOrderBy == "asc",
                                onClick = {
                                    tempSortBy = "createdAt"
                                    tempOrderBy = "asc"
                                }
                            )
                            SortOption(
                                label = "Giá thấp đến cao",
                                isSelected = tempSortBy == "price" && tempOrderBy == "asc",
                                onClick = {
                                    tempSortBy = "price"
                                    tempOrderBy = "asc"
                                }
                            )
                            SortOption(
                                label = "Giá cao đến thấp",
                                isSelected = tempSortBy == "price" && tempOrderBy == "desc",
                                onClick = {
                                    tempSortBy = "price"
                                    tempOrderBy = "desc"
                                }
                            )
                            SortOption(
                                label = "Giảm giá nhiều nhất",
                                isSelected = tempSortBy == "sale",
                                onClick = {
                                    tempSortBy = "sale"
                                    tempOrderBy = "desc"
                                }
                            )
                        }
                    }
                }

                // Add bottom padding
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // Bottom Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.colorSystem_background_level_0))
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeutralButton(
                    text = "Đặt lại",
                    onClick = {
                        tempCategoryIds = emptyList()
                        tempMinPrice = ""
                        tempMaxPrice = ""
                        tempSortBy = "createdAt"
                        tempOrderBy = "desc"
                        onApply(emptyList(), null, null, "createdAt", "desc")
                    },
                    modifier = Modifier.weight(1f)
                )
                FilledButton(
                    text = "Áp dụng",
                    onClick = {
                        val min = tempMinPrice.toIntOrNull()
                        val max = tempMaxPrice.toIntOrNull()
                        onApply(tempCategoryIds, min, max, tempSortBy, tempOrderBy)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = CustomTypography.TextSemiBold.copy(fontSize = 16.sp),
            color = colorResource(R.color.colorSystem_heading_button)
        )
        content()
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) colorResource(R.color.colorSystem_heading_button)
                else colorResource(R.color.colorSystem_background_level_0)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) colorResource(R.color.colorSystem_heading_button)
                else colorResource(R.color.colorSystem_greyscale_200),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colorResource(R.color.colorSystem_background_level_0),
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                style = CustomTypography.TextMedium.copy(
                    color = if (isSelected) colorResource(R.color.colorSystem_background_level_0)
                    else colorResource(R.color.colorSystem_heading_button),
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
private fun SortOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) colorResource(R.color.colorSystem_heading_button).copy(alpha = 0.1f)
                else colorResource(R.color.colorSystem_background_level_0)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) colorResource(R.color.colorSystem_heading_button)
                else colorResource(R.color.colorSystem_greyscale_200),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = CustomTypography.TextMedium.copy(
                color = if (isSelected) colorResource(R.color.colorSystem_heading_button)
                else colorResource(R.color.colorSystem_normal_text),
                fontSize = 15.sp
            )
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colorResource(R.color.colorSystem_heading_button),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

