package com.ptit.common.presentation.component

import androidx.compose.foundation.background
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeBox

@Composable
fun FullScreenProgressBar() {
    MaxSizeBox(
        modifier = Modifier
            .consumeTaps()
            .background(Color.Gray.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = colorResource(id = R.color.colorSystem_heading_button)
        )
    }
}