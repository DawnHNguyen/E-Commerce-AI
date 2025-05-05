package com.ptit.core.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.ptit.common.R
import com.ptit.common.presentation.theme.CustomTypography

@Composable
fun QuantityControl(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(id = R.color.colorSystem_background_level_0))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        IconButton(
            onClick = onDecrease,
            enabled = quantity > 1,
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = colorResource(R.color.colorSystem_background_level_1),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Text(
                text = "-",
                style = CustomTypography.TextSemiBold,
                color = colorResource(R.color.colorSystem_heading_button)
            )
        }

        Text(
            text = quantity.toString(),
            style = CustomTypography.TextSemiBold,
            color = colorResource(R.color.colorSystem_heading_button),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        IconButton(
            onClick = onIncrease,
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = colorResource(R.color.colorSystem_background_level_1),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Text(
                text = "+",
                style = CustomTypography.TextSemiBold,
                color = colorResource(R.color.colorSystem_heading_button)
            )
        }
    }
}