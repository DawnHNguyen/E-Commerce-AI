package com.ptit.core.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ptit.common.R
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.core.seller_request.SellerRequestViewModel
import com.ptit.domain.utils.Resource

/**
 * Loading screen that checks seller request status and redirects accordingly:
 * - null -> RequestStatus (with NoShopCard)
 * - PENDING/REJECTED -> RequestStatus (with request details)
 * - APPROVED -> ShopDetail
 */
@Composable
fun ShopLoadingScreen(
    viewModel: SellerRequestViewModel = hiltViewModel(),
    onNavigateToNoShop: () -> Unit, // Thay cho onNavigateToRequestStatus (nếu null)
    onNavigateToRequestStatus: () -> Unit,
    onNavigateToShopDetail: () -> Unit,
    onBack: () -> Unit
) {
    LocalBottomNavigationVisibility.current.value = false

    val myRequestState by viewModel.myRequestState.collectAsStateWithLifecycle()

    // Auto-fetch when screen loads
    LaunchedEffect(Unit) {
        viewModel.fetchMySellerRequest()
    }

    // Handle navigation based on request state
    LaunchedEffect(myRequestState) {
        when (val state = myRequestState) {
            is Resource.Success -> {
                val request = state.data
                if (request == null) {
                    // No request -> Show RequestStatus with NoShopCard
                    onNavigateToNoShop()
                } else if (request.status.name == "APPROVED") {
                    // Approved -> Go to ShopDetail
                    onNavigateToShopDetail()
                } else {
                    // PENDING or REJECTED -> Show RequestStatus
                    onNavigateToRequestStatus()
                }
            }
            is Resource.Error -> {
                // On error, navigate to RequestStatus (will show error there)
                onNavigateToNoShop()
            }
            else -> {
                // Loading or Idle - stay on loading screen
            }
        }
    }

    // Loading UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.colorSystem_background_level_0)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Đang tải thông tin cửa hàng...",
                style = CustomTypography.TextMedium.copy(fontSize = 16.sp),
                color = colorResource(R.color.colorSystem_greyscale_600),
                textAlign = TextAlign.Center
            )
        }
    }
}

