package com.ptit.core.cart.components

import DialogState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.theme.CustomTypography

@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = CustomTypography.TextBold,
                fontSize = 18.sp,
                color = colorResource(R.color.colorSystem_heading_button)
            )
        },
        text = {
            Text(
                text = message,
                style = CustomTypography.TextRegular,
                color = colorResource(R.color.colorSystem_normal_text),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Yes",
                    style = CustomTypography.TextSemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "No",
                    style = CustomTypography.TextSemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun HandleDialogs(
    dialogState: DialogState,
    onDismiss: () -> Unit,
    onConfirmDelete: (List<String>) -> Unit
) {
    when (val currentDialog = dialogState) {
        is DialogState.DeleteSingleItem -> {
            DeleteConfirmationDialog(
                title = "Xóa sản phẩm",
                message = "Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?",
                onConfirm = { onConfirmDelete(listOf(currentDialog.item.id)) },
                onDismiss = onDismiss
            )
        }
        is DialogState.DeleteMultipleItems -> {
            DeleteConfirmationDialog(
                title = "Xóa sản phẩm",
                message = "Bạn có chắc chắn muốn xóa ${currentDialog.itemIds.size} sản phẩm đã chọn khỏi giỏ hàng?",
                onConfirm = { onConfirmDelete(currentDialog.itemIds.toList()) },
                onDismiss = onDismiss
            )
        }
        DialogState.Hidden -> {}
    }
}
