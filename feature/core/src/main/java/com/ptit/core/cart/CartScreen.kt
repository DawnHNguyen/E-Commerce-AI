package com.ptit.core.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.presentation.cart.CartViewModel

@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel()
) {
    val cartState by viewModel.cartState.collectAsState()

    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("My Cart") }
//            )
//        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = cartState) {
                is CartViewModel.CartState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is CartViewModel.CartState.Success -> {
                    if (state.purchases.isEmpty()) {
                        Text(
                            text = "Your cart is empty",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        CartContent(purchases = state.purchases)
                    }
                }
                is CartViewModel.CartState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${state.message}")
                        Button(
                            onClick = { viewModel.getPurchases() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun CartContent(purchases: List<PurchaseDomainEntity>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(purchases) { purchase ->
            PurchaseItem(purchase = purchase)
        }
    }
}

@Composable
fun PurchaseItem(purchase: PurchaseDomainEntity) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = purchase.product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Quantity: ${purchase.buyCount}")
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Price: $${purchase.price / 100000}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (purchase.priceBeforeDiscount > purchase.price) {
                    Text(
                        text = "$${purchase.priceBeforeDiscount / 100000}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}