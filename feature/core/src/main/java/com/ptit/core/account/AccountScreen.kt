package com.ptit.core.account

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.ptit.common.presentation.MaxSizeBox

@Composable
fun AccountScreen() {
    MaxSizeBox(
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Profile Screen",
        )
    }
}