package com.ptit.common.presentation.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.theme.CustomTypography

@Composable
fun FilledTextField(
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
) {
    BaseTextField(
        value = value,
        hint = hint,
        onValueChange = onValueChange,
        modifier = modifier,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        isError = isError,
        errorMessage = errorMessage,
        enabled = enabled,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        backgroundColor = Color.Transparent,
        hintTextColor = colorResource(R.color.colorSystem_greyscale_600),
        disableBackgroundColor = Color.White,
        focusStrokeColor = colorResource(id = R.color.colorSystem_text_field),
        unfocusStrokeColor = Color.Transparent,
        disableTextColor = colorResource(R.color.colorSystem_greyscale_1000_black),
    )
}

@Composable
fun FilledTextField(
    value: TextFieldValue,
    hint: String,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
) {
    BaseTextField(
        value = value,
        hint = hint,
        onValueChange = onValueChange,
        modifier = modifier,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        isError = isError,
        errorMessage = errorMessage,
        enabled = enabled,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        backgroundColor = Color.Transparent,
        hintTextColor = colorResource(R.color.colorSystem_greyscale_600),
        disableBackgroundColor = Color.White,
        focusStrokeColor = colorResource(id = R.color.colorSystem_text_field),
        unfocusStrokeColor = Color.Transparent,
        disableTextColor = colorResource(R.color.colorSystem_greyscale_1000_black),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BaseTextField(
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    backgroundColor: Color,
    hintTextColor: Color,
    disableBackgroundColor: Color,
    focusStrokeColor: Color,
    unfocusStrokeColor: Color,
    disableTextColor: Color,
) {
    val colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = colorResource(id = R.color.colorSystem_greyscale_1000_black),
        unfocusedTextColor = colorResource(id = R.color.colorSystem_greyscale_1000_black),
        disabledTextColor = disableTextColor,
        errorTextColor = colorResource(id = R.color.colorSystem_greyscale_1000_black),
        focusedContainerColor = backgroundColor,
        unfocusedContainerColor = backgroundColor,
        disabledContainerColor = disableBackgroundColor,
        errorContainerColor = backgroundColor,
        cursorColor = colorResource(id = R.color.colorSystem_greyscale_1000_black),
        errorCursorColor = colorResource(id = R.color.colorSystem_greyscale_1000_black),
        focusedBorderColor = focusStrokeColor,
        unfocusedBorderColor = unfocusStrokeColor,
        disabledBorderColor = unfocusStrokeColor,
        errorBorderColor = colorResource(id = R.color.colorSystem_tint_red),
        focusedPlaceholderColor = hintTextColor,
        unfocusedPlaceholderColor = hintTextColor,
        disabledPlaceholderColor = hintTextColor,
    )
    val interactionSource = remember { MutableInteractionSource() }
    val focused = interactionSource.collectIsFocusedAsState().value
    val textColor = remember(enabled, isError, focused) {
        when {
            !enabled -> colors.disabledTextColor
            isError -> colors.errorTextColor
            focused -> colors.focusedTextColor
            else -> colors.unfocusedTextColor
        }
    }
    val cursorColor = remember(isError) {
        if (isError) colors.errorCursorColor else colors.cursorColor
    }

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            interactionSource = interactionSource,
            textStyle = CustomTypography.TextField.merge(
                color = textColor
            ),
            cursorBrush = SolidColor(cursorColor),
            enabled = enabled,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            readOnly = readOnly,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
        ) {
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = it,
                singleLine = singleLine,
                enabled = enabled,
                interactionSource = interactionSource,
                contentPadding = OutlinedTextFieldDefaults.contentPadding(
                    top = 8.dp,
                    bottom = 8.dp,
                ),
                colors = colors,
                placeholder = {
                    Text(
                        text = hint,
                        style = CustomTypography.TextField
                    )
                },
                leadingIcon = leadingContent,
                trailingIcon = trailingContent,
                supportingText =
                    if (isError.not()) null
                    else {
                        @Composable {
                            Text(
                                text = errorMessage,
                                style = CustomTypography.TextMedium,
                                fontSize = 12.sp,
                                color = colorResource(R.color.colorSystem_tint_red)
                            )
                        }
                    },
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = enabled,
                        isError = isError,
                        colors = colors,
                        interactionSource = interactionSource,
                        shape = RoundedCornerShape(16.dp),
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BaseTextField(
    value: TextFieldValue,
    hint: String,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    backgroundColor: Color,
    hintTextColor: Color,
    disableBackgroundColor: Color,
    focusStrokeColor: Color,
    unfocusStrokeColor: Color,
    disableTextColor: Color,
) {
    val colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = colorResource(id = R.color.colorSystem_greyscale_1000_black),
        unfocusedTextColor = colorResource(id = R.color.colorSystem_greyscale_1000_black),
        disabledTextColor = disableTextColor,
        errorTextColor = colorResource(id = R.color.colorSystem_greyscale_1000_black),
        focusedContainerColor = backgroundColor,
        unfocusedContainerColor = backgroundColor,
        disabledContainerColor = disableBackgroundColor,
        errorContainerColor = backgroundColor,
        cursorColor = colorResource(id = R.color.colorSystem_greyscale_1000_black),
        errorCursorColor = colorResource(id = R.color.colorSystem_greyscale_1000_black),
        focusedBorderColor = focusStrokeColor,
        unfocusedBorderColor = unfocusStrokeColor,
        disabledBorderColor = unfocusStrokeColor,
        errorBorderColor = colorResource(id = R.color.colorSystem_tint_red),
        focusedPlaceholderColor = hintTextColor,
        unfocusedPlaceholderColor = hintTextColor,
        disabledPlaceholderColor = hintTextColor,
    )
    val interactionSource = remember { MutableInteractionSource() }
    val focused = interactionSource.collectIsFocusedAsState().value
    val textColor = remember(enabled, isError, focused) {
        when {
            !enabled -> colors.disabledTextColor
            isError -> colors.errorTextColor
            focused -> colors.focusedTextColor
            else -> colors.unfocusedTextColor
        }
    }
    val cursorColor = remember(isError) {
        if (isError) colors.errorCursorColor else colors.cursorColor
    }

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            interactionSource = interactionSource,
            textStyle = CustomTypography.TextField.merge(
                color = textColor
            ),
            cursorBrush = SolidColor(cursorColor),
            enabled = enabled,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            readOnly = readOnly,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation
        ) {
            OutlinedTextFieldDefaults.DecorationBox(
                value = value.text,
                visualTransformation = visualTransformation,
                innerTextField = it,
                singleLine = singleLine,
                enabled = enabled,
                interactionSource = interactionSource,
                contentPadding = OutlinedTextFieldDefaults.contentPadding(
                    top = 8.dp,
                    bottom = 8.dp,
                ),
                colors = colors,
                placeholder = {
                    Text(
                        text = hint,
                        style = CustomTypography.TextField
                    )
                },
                leadingIcon = leadingContent,
                trailingIcon = trailingContent,
                supportingText =
                    if (isError.not()) null
                    else {
                        @Composable {
                            Text(
                                text = errorMessage,
                                style = CustomTypography.TextMedium,
                                fontSize = 12.sp,
                                color = colorResource(R.color.colorSystem_tint_red)
                            )
                        }
                    },
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = enabled,
                        isError = isError,
                        colors = colors,
                        interactionSource = interactionSource,
                        shape = RoundedCornerShape(16.dp),
                    )
                },
            )
        }
    }
}
