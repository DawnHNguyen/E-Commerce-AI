package com.ptit.core.order.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.ptit.common.utils.toPriceFormat
import com.ptit.domain.entity.cart.CartItemDomainEntity
import com.ptit.domain.entity.order.ProductSKUSnapshotDomainEntity


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SharedCartItemRow(cartItem: CartItemDomainEntity) {
    val sku = cartItem.sku
    val product = sku?.product
    val quantity = cartItem.quantity

    SharedOrderItemRowContent(
        name = product?.name ?: "Không rõ sản phẩm",
        image = sku?.image ?: product?.mainImage,
        basePrice = product?.basePrice?.toPriceFormat() ?: 0,
        price = sku?.price ?: 0,
        quantity = quantity
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SharedSnapshotItemRow(
    snapshot: ProductSKUSnapshotDomainEntity,
    onClick: () -> Unit = {}
) {
    SharedOrderItemRowContent(
        name = snapshot.productName,
        image = snapshot.image,
        basePrice = 0,
        price = snapshot.skuPrice,
        quantity = snapshot.quantity,
        onClick = onClick
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun SharedOrderItemRowContent(
    name: String,
    image: String?,
    basePrice: Any,
    price: Int,
    quantity: Int,
    onClick: () -> Unit = {}
) {
    // Tạo biến giá trị đã định dạng
    val formattedPrice = price.toPriceFormat()

    // Xử lý basePrice (virtualPrice):
    val formattedBasePrice = if (basePrice is Int && basePrice > 0) {
        basePrice.toPriceFormat()
    } else if (basePrice is String) {
        basePrice // Nếu nó đã được format từ SharedCartItemRow
    } else {
        null // Nếu là 0 hoặc kiểu khác
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            GlideImage(
                model = image,
                contentDescription = name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = CustomTypography.TextRegular,
                    color = colorResource(R.color.colorSystem_normal_text),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (basePrice != price && basePrice != 0) {
                    Text(
                        text = formattedBasePrice.toString(),
                        style = CustomTypography.TextRegular.copy(
                            textDecoration = TextDecoration.LineThrough,
                            fontSize = 12.sp
                        ),
                        color = colorResource(R.color.colorSystem_greyscale_600)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = formattedPrice,
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
fun SharedTotalAmountSection(subtotal: String, shippingFee: String, totalPrice: String) {
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
                text = subtotal,
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
                text = shippingFee,
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
                text = totalPrice,
                style = CustomTypography.TextSemiBold,
                color = colorResource(R.color.colorSystem_heading_button)
            )
        }
    }
}
