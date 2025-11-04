package com.ptit.core.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FilledTextField
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.core.order.components.SharedCartItemRow
import com.ptit.core.order.components.SharedTotalAmountSection
import com.ptit.domain.utils.Resource
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    selectedItemIds: List<String>,
    viewModel: CreateOrderViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOrderCreated: (String) -> Unit
) {
    LocalBottomNavigationVisibility.current.value = false

    val orderState by viewModel.orderState.collectAsState()
    val userProfileState by viewModel.userProfileState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 🔹 Khi nhận danh sách cartItemIds từ CartScreen hoặc BuyNow flow
    LaunchedEffect(selectedItemIds) {
        viewModel.setSelectedItemIds(selectedItemIds)
    }

    // 🔹 Lắng nghe sự kiện (hiển thị toast/snackbar)
    LaunchedEffect(Unit) {
        viewModel.orderEvents.collectLatest { event ->
            when (event) {
                is CreateOrderViewModel.OrderEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is CreateOrderViewModel.OrderEvent.OrderCreated -> {
                    val firstOrderId = event.response.orders.firstOrNull()?.id ?: ""
                    snackbarHostState.showSnackbar("Đặt hàng thành công!")
                    onOrderCreated(firstOrderId)
                }
            }
        }
    }

    // 🔹 Hiển thị loading overlay
    if (orderState.isLoading || userProfileState is Resource.Loading) {
        FullScreenProgressBar()
        return
    }

    // 🔹 Hiển thị lỗi toàn cục
    if (orderState.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Lỗi: ${orderState.error}",
                color = MaterialTheme.colorScheme.error,
                style = CustomTypography.TextMedium
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Tạo đơn hàng",
                            style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(R.color.colorSystem_greyscale_0_white)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button),
                    titleContentColor = colorResource(R.color.colorSystem_greyscale_0_white)
                )
            )
        }
    ) { paddingValues ->
        MaxSizeColumn(
            modifier = Modifier
                .padding(paddingValues)
                .background(colorResource(R.color.colorSystem_background_level_0))
        ) {
            val selectedShops = orderState.selectedShops

            if (selectedShops.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        "Chưa có sản phẩm nào. IDs nhận: ${selectedItemIds.joinToString()}",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---------------------------
                // 🧍‍♀️ Thông tin người nhận
                // ---------------------------
                item {
                    ShippingInformationSection(
                        name = orderState.name,
                        phone = orderState.phone,
                        address = orderState.address,
                        onNameChange = viewModel::updateName,
                        onPhoneChange = viewModel::updatePhone,
                        onAddressChange = viewModel::updateAddress,
                        isUserInfoLoaded = orderState.isUserInfoLoaded
                    )
                }

                // ---------------------------
                // 🏪 Danh sách sản phẩm theo từng shop
                // ---------------------------
                selectedShops.forEach { shop ->
                    item {
                        ShopOrderSection(shop.shopName ?: "Cửa hàng", shop.cartItems)
                    }
                }

                // ---------------------------
                // 📝 Ghi chú
                // ---------------------------
                item {
                    OrderNoteSection(
                        note = orderState.note,
                        onNoteChange = viewModel::updateNote
                    )
                }

                // ---------------------------
                // 💰 Tổng tiền
                // ---------------------------
                val subtotal = selectedShops.sumOf { shop ->
                    shop.cartItems.sumOf { (it.sku?.price ?: 0) * it.quantity }
                }
                val shippingFee = 30000
                val total = subtotal + shippingFee

                item {
                    SharedTotalAmountSection(
                        subtotal = subtotal,
                        shippingFee = shippingFee,
                        totalPrice = total
                    )
                }

                item { Spacer(modifier = Modifier.height(60.dp)) }
            }

            // ---------------------------
            // 🧾 Nút “Đặt hàng”
            // ---------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.colorSystem_greyscale_0_white))
                    .padding(16.dp)
            ) {
                FilledButton(
                    text = "Đặt hàng",
                    onClick = { viewModel.createOrder() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !orderState.isLoading && orderState.selectedShops.isNotEmpty()
                )
            }
        }
    }
}

/**
 * 🏪 Hiển thị danh sách sản phẩm trong 1 shop
 */
@Composable
fun ShopOrderSection(shopName: String, cartItems: List<com.ptit.domain.entity.cart.CartItemDomainEntity>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(12.dp)
    ) {
        Text(
            text = shopName,
            style = CustomTypography.TextSemiBold,
            color = colorResource(R.color.colorSystem_heading_button)
        )
        Spacer(modifier = Modifier.height(8.dp))

        cartItems.forEach { item ->
            SharedCartItemRow(cartItem = item)
        }
    }
}

/**
 * 📦 Thông tin vận chuyển (người nhận)
 */
@Composable
fun ShippingInformationSection(
    name: TextFieldValue,
    phone: TextFieldValue,
    address: TextFieldValue,
    onNameChange: (TextFieldValue) -> Unit,
    onPhoneChange: (TextFieldValue) -> Unit,
    onAddressChange: (TextFieldValue) -> Unit,
    isUserInfoLoaded: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Thông tin vận chuyển",
            style = CustomTypography.TextBold,
            color = colorResource(R.color.colorSystem_heading_button)
        )

        FilledTextField(
            value = name,
            hint = "Họ tên đầy đủ",
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth()
        )

        FilledTextField(
            value = phone,
            hint = "Số điện thoại",
            onValueChange = onPhoneChange,
            modifier = Modifier.fillMaxWidth()
        )

        FilledTextField(
            value = address,
            hint = "Địa chỉ giao hàng",
            onValueChange = onAddressChange,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Phí vận chuyển: 30.000 đ",
            style = CustomTypography.TextRegular,
            color = colorResource(R.color.colorSystem_normal_text),
            modifier = Modifier.align(Alignment.End)
        )
    }
}

/**
 * 📝 Ghi chú đơn hàng
 */
@Composable
fun OrderNoteSection(
    note: TextFieldValue,
    onNoteChange: (TextFieldValue) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(16.dp)
    ) {
        FilledTextField(
            value = note,
            hint = "Ghi chú cho shop (nếu có)",
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
    }
}
