package com.ptit.core.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.ptit.common.utils.toPriceFormat
import com.ptit.core.order.components.CustomerInformationSection
import com.ptit.core.order.components.SharedCartItemRow
import com.ptit.core.order.components.SharedTotalAmountSection
import com.ptit.core.order.components.ShippingAddressSection
import com.ptit.core.order.components.VoucherBottomSheet
import com.ptit.domain.entity.cart.CartItemDetailDomainEntity
import com.ptit.domain.utils.Resource
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    selectedItemIds: List<String>,
    groupedCartItems: List<CartItemDetailDomainEntity>,
    viewModel: CreateOrderViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOrderCreated: (String) -> Unit
) {
    LocalBottomNavigationVisibility.current.value = false

    val orderState by viewModel.orderState.collectAsState()
    val userProfileState by viewModel.userProfileState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val addressState by viewModel.addressState.collectAsState()
    var showVoucherBottomSheet by remember { mutableStateOf(false) }

    // 🔹 Khi nhận danh sách cartItemIds từ CartScreen hoặc BuyNow flow
    LaunchedEffect(selectedItemIds, groupedCartItems) { // 🔴 SỬA: Thêm groupedCartItems vào key
        // 🔴 SỬA: Gọi hàm mới, lọc dữ liệu local
        viewModel.setSelectedItems(selectedItemIds, groupedCartItems)
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
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                // 0. 🏷️ HÀNG VOUCHER
                // ---------------------------
                item {
                    VoucherSelectorRow(
                        onClick = { showVoucherBottomSheet = true }
                    )
                }
                // ---------------------------
                // 1. 🧍‍♀️ Thông tin khách hàng (Customer Information)
                // ---------------------------
                item {
                    // 🔴 THAY THẾ: Sử dụng component CustomerInformationSection mới
                    CustomerInformationSection(
                        name = orderState.name,
                        email = orderState.email,
                        phone = orderState.phone
                    )
                }

                // ---------------------------
                // 2. 📦 Thông tin nhận hàng (Shipping Address - GHN)
                // ---------------------------
                item {
                    // 🔴 THAY THẾ: Sử dụng component ShippingAddressSection mới
                    ShippingAddressSection(
                        // Thông tin người nhận
                        receiverName = orderState.name,
                        receiverPhone = orderState.phone,
                        onNameChange = viewModel::updateName,
                        onPhoneChange = viewModel::updatePhone,

                        // Dữ liệu GHN
                        provinces = addressState.provinces,
                        districts = addressState.districts,
                        wards = addressState.wards,
                        selectedProvince = addressState.selectedProvince,
                        selectedDistrict = addressState.selectedDistrict,
                        selectedWard = addressState.selectedWard,

                        // 🔴 THÊM: Loading states (Dựa trên ViewModel đã sửa)
                        provincesLoading = addressState.provincesLoading,
                        districtsLoading = addressState.districtsLoading,
                        wardsLoading = addressState.wardsLoading,

                        onSelectProvince = viewModel::selectProvince,
                        onSelectDistrict = viewModel::selectDistrict,
                        onSelectWard = viewModel::selectWard,

                        // Địa chỉ cụ thể
                        detailAddress = orderState.address,
                        onDetailAddressChange = viewModel::updateAddress
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
                        subtotal = subtotal.toPriceFormat(),
                        shippingFee = shippingFee.toPriceFormat(),
                        totalPrice = total.toPriceFormat()
                    )
                }

                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
            VoucherBottomSheet(
                isVisible = showVoucherBottomSheet,
                onDismiss = { showVoucherBottomSheet = false },
                onConfirm = {
                    // TODO: Xử lý logic xác nhận voucher và áp dụng giảm giá
                    showVoucherBottomSheet = false
                },
                // Giả lập trạng thái tải thành công để hiển thị UI list
                isVoucherListLoaded = true,
                onVoucherCodeApply = { code ->
                    // TODO: Gửi mã code lên ViewModel để kiểm tra và áp dụng
                }
            )

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
                    enabled = orderState.selectedShops.isNotEmpty()
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
