package com.ptit.common.presentation.component

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.ptit.common.R

@Composable
fun CustomSearchBar(
    modifier: Modifier = Modifier,
    hint: String,
    value: String,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit,
    onClickSearch: (String) -> Unit = {},
) {
    FilledTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        hint = hint,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onClickSearch(value)
            }
        ),
        singleLine = true,
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "",
                tint = colorResource(R.color.colorSystem_greyscale_500)
            )
        },
        trailingContent = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "",
                        tint = colorResource(id = R.color.colorSystem_greyscale_1000_black)
                    )
                }
            }
        }
    )
}

@Composable
fun CustomSearchBar(
    modifier: Modifier = Modifier,
    hint: String,
    value: TextFieldValue,
    enabled: Boolean = true,
    onValueChange: (TextFieldValue) -> Unit,
    onClickSearch: (String) -> Unit = {},
) {
    FilledTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        hint = hint,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onClickSearch(value.text)
            }
        ),
        singleLine = true,
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "",
                tint = colorResource(R.color.colorSystem_greyscale_500)
            )
        },
        trailingContent = {
            if (value.text.isNotEmpty()) {
                IconButton(onClick = { onValueChange(TextFieldValue()) }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "",
                        tint = colorResource(id = R.color.colorSystem_greyscale_1000_black)
                    )
                }
            }
        }
    )
}
