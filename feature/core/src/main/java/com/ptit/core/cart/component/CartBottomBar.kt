package com.ptit.core.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.theme.CustomTypography

@Composable
fun CartBottomBar(
    totalPrice: Int,
    isAllSelected: Boolean,
    onSelectAllChanged: (Boolean) -> Unit,
    hasSelectedItems: Boolean,
    onDeleteSelected: () -> Unit,
    onCheckout: () -> Unit,
    checkboxColors: CheckboxColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorResource(R.color.colorSystem_greyscale_0_white))
            .padding(16.dp)
    ) {
        MaxWidthRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Select All and Total
            Column {
                // Select All checkbox
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isAllSelected,
                        onCheckedChange = onSelectAllChanged,
                        colors = checkboxColors
                    )

                    Text(
                        text = "Chọn tất cả",
                        style = CustomTypography.TextMedium,
                        color = colorResource(R.color.colorSystem_normal_text)
                    )
                }

                // Total amount
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = "Tổng tiền: ",
                        style = CustomTypography.TextRegular,
                        color = colorResource(R.color.colorSystem_normal_text)
                    )

                    Text(
                        text = "$totalPrice đ",
                        style = CustomTypography.TextSemiBold.merge(
                            color = colorResource(R.color.colorSystem_heading_button)
                        ),
                        fontSize = 16.sp
                    )
                }
            }

            // Right side - Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Delete button
                Button(
                    onClick = onDeleteSelected,
                    enabled = hasSelectedItems,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasSelectedItems)
                            Color(0xFFE53935) // Enabled - deeper red
                        else
                            Color(0xFFFFCDD2), // Disabled - light red
                        disabledContainerColor = Color(0xFFFFCDD2)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Xóa",
                        style = CustomTypography.TextSemiBold,
                        color = Color.White
                    )
                }

                // Checkout button
                FilledButton(
                    text = "Mua hàng",
                    onClick = onCheckout,
                    enabled = hasSelectedItems
                )
            }
        }
    }
}