package com.ptit.core.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.common.R
import com.ptit.common.presentation.theme.CustomTypography
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityControl(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onQuantityChange: (Int) -> Unit = {}
) {
    var textValue by remember(quantity) { mutableStateOf(quantity.toString()) }
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    // ✅ Tự động cập nhật khi textValue thay đổi (với debounce 800ms)
    LaunchedEffect(textValue) {
        // Đợi 800ms sau khi user ngừng nhập
        delay(800)

        val newQty = textValue.toIntOrNull()
        when {
            // Nếu input rỗng hoặc không hợp lệ → không làm gì
            newQty == null || newQty < 1 -> {
                // Reset về quantity cũ nếu user để trống quá lâu
                if (textValue.isEmpty()) {
                    textValue = quantity.toString()
                }
            }
            // Nếu > 1000 → giới hạn về 1000 và gọi API
            newQty > 1000 -> {
                textValue = "1000"
                if (quantity != 1000) {
                    onQuantityChange(1000)
                }
            }
            // Nếu khác quantity hiện tại → gọi API
            newQty != quantity -> {
                onQuantityChange(newQty)
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .width(96.dp) // Tăng lên 96dp để vừa với TextField 42dp + 2 nút 20dp + padding
            .clip(RoundedCornerShape(8.dp))
            .background(colorResource(id = R.color.colorSystem_background_level_0))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // --- NÚT TRỪ ---
        IconButton(
            onClick = onDecrease,
            enabled = quantity > 1,
            modifier = Modifier
                .size(20.dp) // Giảm từ 24dp xuống 20dp
                .background(
                    color = colorResource(R.color.colorSystem_background_level_1),
                    shape = RoundedCornerShape(6.dp)
                )
        ) {
            Text(
                text = "-",
                style = CustomTypography.TextSemiBold,
                color = colorResource(R.color.colorSystem_heading_button)
            )
        }

        // --- Ô NHẬP TEXTFIELD ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(42.dp) // Tăng lên 42dp để hiển thị đủ 3 chữ số
                .height(22.dp) // Tăng lên 22dp cho thoải mái
        ) {
            CompositionLocalProvider(LocalTextSelectionColors provides TextSelectionColors(
                handleColor = colorResource(R.color.colorSystem_greyscale_1000_black),
                backgroundColor = colorResource(R.color.colorSystem_greyscale_1000_black).copy(alpha = 0.4f)
            )) {
                BasicTextField(
                    value = textValue,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 4)) {
                            textValue = newValue
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = CustomTypography.TextSemiBold.copy(
                        color = colorResource(R.color.colorSystem_greyscale_1000_black),
                        fontSize = 12.sp, // Tăng lên 13sp cho rõ ràng hơn
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp // Điều chỉnh lineHeight cho cân đối
                    ),
                    cursorBrush = SolidColor(colorResource(R.color.colorSystem_greyscale_1000_black)),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            // Chỉ cần đóng bàn phím, logic update đã được xử lý bởi LaunchedEffect
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true,
                    interactionSource = interactionSource,
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.Center,

                        ) {
                            innerTextField()
                        }
                    }
                )
            }
        }

        // --- NÚT CỘNG ---
        IconButton(
            onClick = onIncrease,
            modifier = Modifier
                .size(20.dp) // Giảm từ 24dp xuống 20dp để đồng bộ với nút -
                .background(
                    color = colorResource(R.color.colorSystem_background_level_1),
                    shape = RoundedCornerShape(6.dp)
                )
        ) {
            Text(
                text = "+",
                style = CustomTypography.TextSemiBold,
                color = colorResource(R.color.colorSystem_heading_button)
            )
        }
    }
}