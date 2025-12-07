package com.ptit.core.shop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ptit.common.presentation.component.FullScreenProgressBar

/**
 * Entry screen for "Tạo cửa hàng" menu
 *
 * Logic:
 * 1. Check if user has shop → Navigate to ShopDetailScreen
 * 2. If no shop, check seller request → Show request status screen
 * 3. If no request → Show CreateSellerRequestScreen
 */
@Composable
fun ShopEntryScreen(
    viewModel: ShopEntryViewModel = hiltViewModel(),
    onNavigateToShopDetail: (shopId: String) -> Unit,
    onNavigateToRequestStatus: () -> Unit,
    onNavigateToCreateRequest: () -> Unit,
    onBack: () -> Unit
) {
    val navigationDestination by viewModel.navigationDestination.collectAsStateWithLifecycle()

    // Handle navigation based on destination
    LaunchedEffect(navigationDestination) {
        when (val destination = navigationDestination) {
            is ShopEntryViewModel.NavigationDestination.ShopDetail -> {
                // User has shop → Navigate to ShopDetailScreen
                onNavigateToShopDetail(destination.shopId)
                viewModel.resetNavigation()
            }
            is ShopEntryViewModel.NavigationDestination.RequestStatus -> {
                // User has seller request → Navigate to request status screen
                onNavigateToRequestStatus()
                viewModel.resetNavigation()
            }
            ShopEntryViewModel.NavigationDestination.CreateRequest -> {
                // User doesn't have request → Navigate to create request screen
                onNavigateToCreateRequest()
                viewModel.resetNavigation()
            }
            null -> {
                // Still loading or idle
            }
        }
    }

    // Show loading while checking status
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        FullScreenProgressBar()
    }
}

