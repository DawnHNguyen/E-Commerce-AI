package com.ptit.common.presentation.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.theme.CustomTypography

@Composable
fun BaseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    hint: String = "",
    errorMessage: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = colorResource(id = R.color.colorSystem_text_field),
            errorContainerColor = Color.Transparent,

            focusedBorderColor = colorResource(id = R.color.colorSystem_text_field),
            unfocusedBorderColor = Color.Transparent,
            errorBorderColor = colorResource(R.color.colorSystem_tint_red),

            unfocusedTextColor = colorResource(R.color.colorSystem_greyscale_1000_black),
            focusedTextColor = colorResource(id = R.color.colorSystem_greyscale_1000_black),
            errorTextColor = colorResource(R.color.colorSystem_greyscale_1000_black),

            focusedPlaceholderColor = colorResource(R.color.colorSystem_greyscale_600),
            unfocusedPlaceholderColor = colorResource(R.color.colorSystem_greyscale_600),
            errorPlaceholderColor = colorResource(R.color.colorSystem_greyscale_600),

            cursorColor = colorResource(R.color.colorSystem_greyscale_1000_black),
            errorCursorColor = colorResource(R.color.colorSystem_greyscale_1000_black),
        ),
        enabled = enabled,
        isError = isError,
        readOnly = readOnly,
        textStyle = CustomTypography.TextField,
        placeholder = {
            Text(
                text = hint,
                style = CustomTypography.TextField
            )
        },
        supportingText = {
            if (isError) {
                Text(
                    text = errorMessage,
                    style = CustomTypography.TextMedium,
                    fontSize = 12.sp,
                    color = colorResource(R.color.colorSystem_tint_red)
                )
            }
        },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        interactionSource = interactionSource,
    )
}