package com.ptit.core.product_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.component.BaseBottomSheet
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.core.cart.components.QuantityControl
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.entity.product.SKUDomainEntity
import com.ptit.domain.entity.product.VariantDomainEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCartBottomSheet(
    product: ProductDomainEntity,
    selectedVariants: Map<String, String>,
    selectedSKU: SKUDomainEntity?,
    onVariantSelected: (String, String) -> Unit,
    isShowBottomSheet: Boolean,
    onDismiss: () -> Unit,
    onAddToCart: (String, Int) -> Unit
) {
    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var quantity by remember { mutableIntStateOf(1) }

    val hasSelectedAllVariants = selectedVariants.size == product.variants.size
    val isInStock = selectedSKU != null && selectedSKU.stock > 0
    val maxStock = selectedSKU?.stock ?: 1

    // Reset quantity mỗi khi mở bottom sheet
    LaunchedEffect(isShowBottomSheet) {
        if (isShowBottomSheet) {
            quantity = 1
        }
    }

    BaseBottomSheet(
        modalSheetState = modalSheetState,
        isShowBottomSheet = isShowBottomSheet,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.colorSystem_background_level_0))
                .padding(16.dp)
        ) {
            // Tên sản phẩm
            Text(
                text = product.name,
                style = CustomTypography.TextSemiBold.merge(
                    color = colorResource(id = R.color.colorSystem_heading_button),
                    fontSize = 18.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Hiển thị giá
            val displayPrice = selectedSKU?.price ?: product.basePrice
            Text(
                text = "${displayPrice}đ",
                style = CustomTypography.TextSemiBold.merge(
                    color = colorResource(id = R.color.colorSystem_heading_button),
                    fontSize = 20.sp
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Variant selectors
            product.variants.forEach { variant ->
                VariantSelectorInBottomSheet(
                    variant = variant,
                    selectedOption = selectedVariants[variant.name],
                    onOptionSelected = { option ->
                        onVariantSelected(variant.name, option)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Hiển thị tồn kho
            if (hasSelectedAllVariants) {
                Text(
                    text = if (isInStock) "Kho: ${selectedSKU.stock}" else "Hết hàng",
                    style = CustomTypography.TextRegular.merge(
                        color = if (isInStock)
                            colorResource(id = R.color.colorSystem_normal_text)
                        else
                            MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Chọn số lượng
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Số lượng",
                    style = CustomTypography.TextMedium.merge(
                        color = colorResource(id = R.color.colorSystem_heading_button),
                        fontSize = 16.sp
                    )
                )

                QuantityControl(
                    quantity = quantity,
                    onIncrease = {
                        if (quantity < maxStock) quantity++
                    },
                    onDecrease = {
                        if (quantity > 1) quantity--
                    },
                    onQuantityChange = { newQty ->
                        // Giới hạn quantity trong khoảng 1 đến maxStock
                        quantity = when {
                            newQty < 1 -> 1
                            newQty > maxStock -> maxStock
                            else -> newQty
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút thêm vào giỏ
            FilledButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Thêm vào giỏ hàng",
                enabled = hasSelectedAllVariants && isInStock,
                onClick = {
                    selectedSKU?.let {
                        onAddToCart(it.value, quantity)
                        onDismiss()
                    }
                }
            )
        }
    }
}

@Composable
fun VariantSelectorInBottomSheet(
    variant: VariantDomainEntity,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit
) {
    Column {
        Text(
            text = variant.name,
            style = CustomTypography.TextMedium.merge(
                color = colorResource(id = R.color.colorSystem_heading_button),
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(variant.options) { option ->
                VariantOption(
                    option = option,
                    isSelected = option == selectedOption,
                    onClick = { onOptionSelected(option) }
                )
            }
        }
    }
}
