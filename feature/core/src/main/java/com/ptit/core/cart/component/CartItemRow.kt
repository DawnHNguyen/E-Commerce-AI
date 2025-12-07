package com.ptit.core.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.component.noRippleClickable
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.toPriceFormat
import com.ptit.domain.entity.cart.CartItemDomainEntity

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CartItemRow(
    purchase: CartItemDomainEntity,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    checkboxColors: CheckboxColors,
    onQuantityUpdate: (String, String, Int) -> Unit,
    onProductClick: (String) -> Unit = {}
) {
    val sku = purchase.sku
    val product = sku?.product
    val quantity = purchase.quantity

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged,
                colors = checkboxColors
            )

            Spacer(modifier = Modifier.width(8.dp))

            GlideImage(
                model = sku?.image ?: product?.mainImage.orEmpty(),
                contentDescription = product?.name.orEmpty(),
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .noRippleClickable { product?.id?.let(onProductClick) },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product?.name ?: "Unknown product",
                    style = CustomTypography.TextRegular.merge(
                        color = colorResource(R.color.colorSystem_normal_text)
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                val basePrice = product?.virtualPrice ?: 0
                val currentPrice = sku?.price ?: product?.basePrice ?: 0

                if (basePrice > currentPrice) {
                    Text(
                        text = basePrice.toPriceFormat(),
                        style = CustomTypography.TextRegular.copy(
                            color = colorResource(R.color.colorSystem_greyscale_300)
                        ),
                        textDecoration = TextDecoration.LineThrough
                    )
                }

                Text(
                    text = currentPrice.toPriceFormat(),
                    style = CustomTypography.TextSemiBold.merge(
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                )
            }

            QuantityControl(
                quantity = quantity,
                onIncrease = {
                    if (quantity < 1000) sku?.id?.let { onQuantityUpdate(purchase.id, it, quantity + 1) }
                },
                onDecrease = {
                    if (quantity > 1) sku?.id?.let { onQuantityUpdate(purchase.id, it, quantity - 1) }
                },
                onQuantityChange = { newQty ->
                    sku?.id?.let { onQuantityUpdate(purchase.id, it, newQty) }
                }
            )
        }
    }
}