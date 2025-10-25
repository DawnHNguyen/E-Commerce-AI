package com.ptit.core.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeBox
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MaxWidthColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.MyCrossFade
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
internal fun ProductItem(
    product: ProductDomainEntity,
    onClickProduct: () -> Unit,
) {
    val ratingAnnotatedString = remember(product.rating) {
        buildAnnotatedString {
            appendInlineContent(id = "ratingIcon")
            append(product.rating.toString())
        }
    }
    val ratingInlineContentMap = mapOf(
        "ratingIcon" to InlineTextContent(
            Placeholder(12.sp, 12.sp, PlaceholderVerticalAlign.TextCenter)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "",
                tint = colorResource(R.color.colorSystem_tint_yellow)
            )
        }
    )

    MaxSizeBox(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color = colorResource(R.color.colorSystem_background_level_2))
            .clickable(onClick = onClickProduct)
    ) {
        MaxSizeColumn {
            GlideImage(
                model = product.images[0],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.FillBounds,
                transition = MyCrossFade,
            ) {
                it.centerCrop()
            }

            MaxWidthColumn(
                modifier = Modifier
                    .padding(
                        vertical = 8.dp,
                        horizontal = 12.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = product.name,
                    style = CustomTypography.TextRegular.merge(
                        color = colorResource(R.color.colorSystem_normal_text),
                        fontSize = 12.sp
                    ),
                    minLines = 2,
                    maxLines = 2
                )

                Text(
                    text = "${product.basePrice}đ",
                    style = CustomTypography.TextSemiBold.merge(
                        color = colorResource(R.color.colorSystem_heading_button),
                        fontSize = 14.sp
                    )
                )

                Spacer(Modifier.height(8.dp))

                MaxWidthRow(
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = ratingAnnotatedString,
                        inlineContent = ratingInlineContentMap,
                        style = CustomTypography.TextRegular.merge(
                            color = colorResource(R.color.colorSystem_normal_text),
                            fontSize = 12.sp
                        )
                    )

                    Text(
                        text = "${product.sold} đã bán",
                        style = CustomTypography.TextRegular.merge(
                            color = colorResource(R.color.colorSystem_normal_text),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        if (product.hasDiscount)
            Text(
                text = "-${product.discountPercent}%",
                style = CustomTypography.TextMedium.merge(
                    color = colorResource(R.color.colorSystem_heading_button),
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .padding(4.dp)
                    .background(
                        color = colorResource(R.color.colorSystem_stroke).copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(
                        vertical = 8.dp,
                        horizontal = 4.dp
                    )
                    .align(Alignment.TopEnd)
            )
    }
}

@Composable
internal fun CategoryItem(
    category: CategoryDomainEntity,
    onClick: () -> Unit = {}
) {
    Text(
        text = category.name,
        style = CustomTypography.TextMedium.merge(
            color = colorResource(R.color.colorSystem_normal_text),
            fontSize = 14.sp
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
internal fun FilterOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit = {}
) {
    Text(
        text = text,
        style = if (isSelected) CustomTypography.TextSemiBold else CustomTypography.TextRegular,
        color = if (isSelected) colorResource(R.color.colorSystem_heading_button) 
                else colorResource(R.color.colorSystem_normal_text),
        fontSize = 14.sp,
        modifier = Modifier.clickable(onClick = onClick)
    )
}