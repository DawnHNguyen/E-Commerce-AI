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
import com.ptit.domain.entity.cart.PurchaseDomainEntity

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CartItemRow(
    purchase: PurchaseDomainEntity,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    checkboxColors: CheckboxColors,
    onQuantityUpdate: (String, Int) -> Unit,
    onProductClick: (String) -> Unit = {} // Add parameter for product click navigation
) {
    val product = purchase.product
    val quantity = purchase.buyCount

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectionChanged(it) },
                colors = checkboxColors
            )

            Spacer(modifier = Modifier.width(8.dp))

            GlideImage(
                model = product.image,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .noRippleClickable { onProductClick(product.id) },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = CustomTypography.TextRegular.merge(
                        color = colorResource(R.color.colorSystem_normal_text)
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${product.priceBeforeDiscount} đ",
                    style = CustomTypography.TextRegular.copy(
                        color = colorResource(R.color.colorSystem_greyscale_300)
                    ),
                    textDecoration = TextDecoration.LineThrough
                )

                Text(
                    text = "${purchase.price}đ",
                    style = CustomTypography.TextSemiBold.merge(
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                )
            }

            QuantityControl(
                quantity = quantity,
                onIncrease = { 
		    if (quantity < product.quantity){
		        onQuantityUpdate(product.id, quantity + 1) 
		    }
		},
                onDecrease = {
                    if (quantity > 1) {
                        onQuantityUpdate(product.id, quantity - 1)
                    }
                }
            )
        }
    }
}
