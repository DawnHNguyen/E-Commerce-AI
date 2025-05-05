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
    onSearch: () -> Unit = {}, // Added parameter to match HomeScreen usage
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
                onSearch() // Call the new parameter
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
    onSearch: () -> Unit = {}, // Added parameter to match HomeScreen usage
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
                onSearch() // Call the new parameter
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