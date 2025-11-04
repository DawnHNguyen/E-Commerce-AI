package com.ptit.core.cart.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.ptit.common.R
import com.ptit.common.presentation.component.SwipeRevealContainer
import com.ptit.common.presentation.component.noRippleClickable
import com.ptit.common.presentation.component.rememberSwipeRevealState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.cart.CartItemDomainEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeToDeleteCartItem(
    purchase: CartItemDomainEntity,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    checkboxColors: CheckboxColors,
    onQuantityUpdate: (String, String, Int) -> Unit,
    onDeleteRequest: () -> Unit,
    onProductClick: (String) -> Unit = {}
) {
    val density = LocalDensity.current
    val deleteButtonWidth = 80.dp
    val deleteButtonWidthPx = with(density) { deleteButtonWidth.toPx() }

    val state = rememberSwipeRevealState(revealedWidth = deleteButtonWidthPx)
    val coroutineScope = rememberCoroutineScope()

    SwipeRevealContainer(
        state = state,
        revealedWidth = deleteButtonWidthPx,
        revealContent = {
            Box(
                modifier = Modifier
                    .width(deleteButtonWidth)
                    .fillMaxHeight()
                    .background(colorResource(R.color.colorSystem_background_level_1)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Xóa",
                    color = Color.Red,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFE8E8))
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                        .noRippleClickable {
                            onDeleteRequest()
                            coroutineScope.launch {
                                state.animateTo(0)
                            }
                        },
                    style = CustomTypography.TextSemiBold
                )
            }
        },
        mainContent = {
            CartItemRow(
                purchase = purchase,
                isSelected = isSelected,
                onSelectionChanged = onSelectionChanged,
                checkboxColors = checkboxColors,
                onQuantityUpdate = onQuantityUpdate,
                onProductClick = onProductClick
            )
        }
    )
}
