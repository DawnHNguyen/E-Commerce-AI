package com.ptit.common.presentation.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.ptit.common.R
import com.ptit.common.presentation.theme.CustomTypography

@Composable
fun NeutralButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    BaseButton(
        modifier = modifier,
        enabled = enabled,
        text = text,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.colorSystem_greyscale_200),
            disabledContainerColor = colorResource(id = R.color.colorSystem_greyscale_200).copy(0.5f),
            contentColor = colorResource(id = R.color.colorSystem_text_button),
            disabledContentColor = colorResource(id = R.color.colorSystem_text_button),
        ),
        onClick = onClick
    )
}

@Composable
fun FilledButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    BaseButton(
        modifier = modifier,
        enabled = enabled,
        text = text,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.colorSystem_heading_button),
            disabledContainerColor = colorResource(id = R.color.colorSystem_heading_button).copy(0.5f),
            contentColor = colorResource(id = R.color.colorSystem_greyscale_0_white),
            disabledContentColor = colorResource(id = R.color.colorSystem_greyscale_0_white),
        ),
        onClick = onClick
    )
}

@Composable
private fun BaseButton(
    modifier: Modifier = Modifier,
    colors: ButtonColors,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(size = 50.dp),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
        colors = colors,
    ) {
        Text(
            text = text,
            style = CustomTypography.Button
        )
    }
}