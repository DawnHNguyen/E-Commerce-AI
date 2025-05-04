package com.ptit.common.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeBox
import com.ptit.common.presentation.theme.CustomTypography

@Composable
fun ProductEmptyState(
    message: String,
    buttonText: String,
    onActionClick: () -> Unit
) {
    MaxSizeBox(
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = CustomTypography.TextMedium,
                color = colorResource(id = R.color.colorSystem_normal_text),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.colorSystem_heading_button)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(
                    text = buttonText,
                    style = CustomTypography.TextSemiBold,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.colorSystem_greyscale_0_white),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}