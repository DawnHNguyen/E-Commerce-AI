package com.ptit.core.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeBox
import com.ptit.common.presentation.theme.CustomTypography

@Composable
fun EmptyCartMessage() {
    MaxSizeBox(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
            .background(color = colorResource(R.color.colorSystem_background_level_0))
            .padding(16.dp)
    ) {
        Text(
            text = "Giỏ hàng của bạn đang trống",
            style = CustomTypography.TextMedium.merge(
                color = colorResource(R.color.colorSystem_normal_text)
            )
        )
    }
}