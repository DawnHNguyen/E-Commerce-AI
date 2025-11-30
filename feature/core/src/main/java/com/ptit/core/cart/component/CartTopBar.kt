package com.ptit.core.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.component.noRippleClickable
import com.ptit.common.presentation.theme.CustomTypography

@Composable
fun CartTopBar(onBack: () -> Unit) {
    MaxWidthRow(
        modifier = Modifier
            .background(color = colorResource(R.color.colorSystem_heading_button))
            .padding(vertical = 16.dp, horizontal = 20.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ Uncommented: Back button
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.noRippleClickable { onBack() },
            tint = colorResource(R.color.colorSystem_greyscale_0_white)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Giỏ hàng",
            style = CustomTypography.TextBold.copy(
                color = colorResource(R.color.colorSystem_greyscale_0_white),
                fontSize = 20.sp
            )
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}