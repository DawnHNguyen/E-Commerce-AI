package com.ptit.core.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.component.CustomPullToRefreshBox
import com.ptit.domain.entity.cart.PurchaseDomainEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartContent(
    purchases: List<PurchaseDomainEntity>,
    onRefresh: () -> Unit,
    isLoading: Boolean,
    onBack: () -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onDeleteSingleItem: (PurchaseDomainEntity) -> Unit,
    onDeleteMultipleItems: (Set<String>) -> Unit,
    onCheckout: (Set<String>) -> Unit, // Modified to pass IDs only
    onProductClick: (String) -> Unit = {}
) {
    var selectedItemIds by remember { mutableStateOf(setOf<String>()) }
    var isAllSelected by remember(purchases) { mutableStateOf(false) }

    // Calculate total price based on selected items
    val totalPrice = purchases
        .filter { selectedItemIds.contains(it.id) }
        .sumOf { it.price * it.buyCount }

    // Custom checkbox colors
    val customCheckboxColors = CheckboxDefaults.colors(
        checkedColor = colorResource(R.color.colorSystem_heading_button),
        uncheckedColor = colorResource(R.color.colorSystem_greyscale_300),
        checkmarkColor = colorResource(R.color.colorSystem_greyscale_0_white)
    )

    CustomPullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isLoading,
        onRefresh = onRefresh
    ) {
        MaxSizeColumn(
            modifier = Modifier.background(colorResource(R.color.colorSystem_background_level_0))
        ) {
            // Top app bar
            CartTopBar(onBack = onBack)

            if (purchases.isEmpty()) {
                EmptyCartMessage()
            } else {
                // Cart items list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(purchases, key = { it.id }) { purchase ->
                        SwipeToDeleteCartItem(
                            purchase = purchase,
                            isSelected = selectedItemIds.contains(purchase.id),
                            onSelectionChanged = { isSelected ->
                                selectedItemIds = if (isSelected) {
                                    selectedItemIds + purchase.id
                                } else {
                                    selectedItemIds - purchase.id
                                }
                            },
                            checkboxColors = customCheckboxColors,
                            onQuantityUpdate = onUpdateQuantity,
                            onDeleteRequest = { onDeleteSingleItem(purchase) },
                            onProductClick = onProductClick
                        )
                    }
                }
            }

            // Bottom payment bar
            CartBottomBar(
                totalPrice = totalPrice,
                isAllSelected = isAllSelected,
                onSelectAllChanged = { checked ->
                    isAllSelected = checked
                    selectedItemIds = if (checked) {
                        purchases.map { it.id }.toSet()
                    } else {
                        emptySet()
                    }
                },
                hasSelectedItems = selectedItemIds.isNotEmpty(),
                onDeleteSelected = { onDeleteMultipleItems(selectedItemIds) },
                onCheckout = { onCheckout(selectedItemIds) }, // Pass just the IDs
                checkboxColors = customCheckboxColors
            )
        }
    }
}