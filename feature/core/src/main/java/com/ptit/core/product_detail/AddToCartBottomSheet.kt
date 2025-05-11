package com.ptit.core.product_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.component.BaseBottomSheet
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.product.ProductDomainEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCartBottomSheet(
    product: ProductDomainEntity,
    isShowBottomSheet: Boolean,
    onDismiss: () -> Unit,
    onAddToCart: (Int) -> Unit
) {
    val modalSheetState = rememberModalBottomSheetState()
    var quantity by remember { mutableStateOf("1") }

    // Validate and parse quantity
    val quantityValue = quantity.toIntOrNull()?.coerceIn(1, product.quantity) ?: 1

    // Check if product is in stock
    val isInStock = product.quantity > 0

    BaseBottomSheet(
        modalSheetState = modalSheetState,
        isShowBottomSheet = isShowBottomSheet,
        onDismiss = onDismiss
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.colorSystem_background_level_0)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Product info
                Text(
                    text = product.name,
                    style = CustomTypography.TextSemiBold.merge(
                        color = colorResource(id = R.color.colorSystem_heading_button),
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Inventory status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kho:",
                        style = CustomTypography.TextRegular,
                        color = colorResource(id = R.color.colorSystem_normal_text)
                    )

                    //Spacer(modifier = Modifier.weight(1f))

                    if (isInStock) {
                        Text(
                            text = "    ${product.quantity}",
                            style = CustomTypography.TextMedium,
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    } else {
                        Text(
                            text = "Hết hàng",
                            style = CustomTypography.TextMedium,
                            color = Color.Red
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Số lượng:",
                        style = CustomTypography.TextRegular,
                        color = colorResource(id = R.color.colorSystem_normal_text)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Decrease button
                    IconButton(
                        onClick = {
                            val current = quantity.toIntOrNull() ?: 1
                            if (current > 1) {
                                quantity = (current - 1).toString()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colorResource(id = R.color.colorSystem_background_level_2)),
                        enabled = (quantityValue > 1) && isInStock
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }

                    // Quantity input
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { newValue ->
                            // Only accept numeric values
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                val newQuantity = newValue.toIntOrNull() ?: 0
                                if (newValue.isEmpty() || (newQuantity in 1..product.quantity)) {
                                    quantity = newValue
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(horizontal = 8.dp),
                        textStyle = CustomTypography.TextMedium.merge(
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = isInStock
                    )

                    // Increase button
                    IconButton(
                        onClick = {
                            val current = quantity.toIntOrNull() ?: 1
                            if (current < product.quantity) {
                                quantity = (current + 1).toString()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colorResource(id = R.color.colorSystem_background_level_2)),
                        enabled = (quantityValue < product.quantity) && isInStock
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Add to cart button
                FilledButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Thêm vào giỏ hàng",
                    onClick = {
                        onAddToCart(quantityValue)
                        onDismiss()
                    },
                    enabled = isInStock && quantityValue > 0
                )
            }
        }
    }
}