package com.ptit.core.order.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.cart.PurchaseDomainEntity

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SharedOrderItemRow(purchase: PurchaseDomainEntity) {
    val product = purchase.product
    val quantity = purchase.buyCount

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            GlideImage(
                model = product.images.firstOrNull(),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = CustomTypography.TextRegular,
                    color = colorResource(R.color.colorSystem_normal_text),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Original price with strikethrough
                if (product.basePrice > 0) {
                    Text(
                        text = "${product.basePrice}đ",
                        style = CustomTypography.TextRegular.copy(
                            textDecoration = TextDecoration.LineThrough,
                            fontSize = 12.sp
                        ),
                        color = colorResource(R.color.colorSystem_greyscale_600)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${purchase.price}đ",
                    style = CustomTypography.TextSemiBold,
                    color = colorResource(R.color.colorSystem_heading_button)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Số lượng: $quantity",
                    style = CustomTypography.TextRegular,
                    color = colorResource(R.color.colorSystem_normal_text)
                )
            }
        }
    }
}

@Composable
fun SharedTotalAmountSection(subtotal: Int, shippingFee: Int, totalPrice: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tạm tính:",
                style = CustomTypography.TextRegular,
                color = colorResource(R.color.colorSystem_normal_text)
            )

            Text(
                text = "$subtotal đ",
                style = CustomTypography.TextRegular,
                color = colorResource(R.color.colorSystem_normal_text)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Phí vận chuyển:",
                style = CustomTypography.TextRegular,
                color = colorResource(R.color.colorSystem_normal_text)
            )

            Text(
                text = "$shippingFee đ",
                style = CustomTypography.TextRegular,
                color = colorResource(R.color.colorSystem_normal_text)
            )
        }

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = colorResource(R.color.colorSystem_greyscale_400)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tổng thanh toán:",
                style = CustomTypography.TextSemiBold,
                color = colorResource(R.color.colorSystem_normal_text)
            )

            Text(
                text = "$totalPrice đ",
                style = CustomTypography.TextSemiBold,
                color = colorResource(R.color.colorSystem_heading_button)
            )
        }
    }
}
