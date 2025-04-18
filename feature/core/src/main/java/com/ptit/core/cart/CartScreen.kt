package com.ptit.core.cart

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.ptit.common.presentation.MaxSizeBox

@Composable
fun CartScreen() {
    MaxSizeBox(
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Cart Screen",
        )
    }

}