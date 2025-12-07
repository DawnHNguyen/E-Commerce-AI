package com.ptit.core.order

// 🔴 XÓA: import android.widget.Toast
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
// 🔴 XÓA: import androidx.compose.ui.platform.LocalContext
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
import com.ptit.core.order.components.VoucherSelectorRow
// 🔴 XÓA: import com.ptit.core.purchase.PurchaseBottomSheet
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
    // 🔴 XÓA: navigateToPaymentMethod
) {
    LocalBottomNavigationVisibility.current.value = false
    // 🔴 XÓA: context

    val orderState by viewModel.orderState.collectAsState()
    val userProfileState by viewModel.userProfileState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val addressState by viewModel.addressState.collectAsState()
    var showVoucherBottomSheet by remember { mutableStateOf(false) }

    // 🔴 XÓA: showPurchaseBottomSheet

    // ... (LaunchedEffects không đổi) ...
    LaunchedEffect(selectedItemIds, groupedCartItems) {
        viewModel.setSelectedItems(selectedItemIds, groupedCartItems)
    }

    LaunchedEffect(Unit) {
        viewModel.orderEvents.collectLatest { event ->
            when (event) {
                is CreateOrderViewModel.OrderEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is CreateOrderViewModel.OrderEvent.OrderCreated -> {
                    // Response sau khi tạo đơn có thể không chứa items
                    // Chỉ cần lấy orderId để navigate
                    val firstOrderId = event.response.orders.firstOrNull()?.id
                    if (firstOrderId != null) {
                        snackbarHostState.showSnackbar("Đặt hàng thành công!")
                        onOrderCreated(firstOrderId)
                    } else {
                        snackbarHostState.showSnackbar("Đặt hàng thành công nhưng không lấy được ID đơn hàng")
                    }
                }
            }
        }
    }

    // ... (Loading và Error UI không đổi) ...
    if (orderState.isLoading || userProfileState is Resource.Loading) {
        FullScreenProgressBar()
        return
    }

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
                    Text(
                        "Tạo đơn hàng",
                        style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
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

                // 🔴 XÓA: PaymentMethodSelectorSection

                // ---------------------------
                // 1. 🧍‍♀️ Thông tin khách hàng
                // ---------------------------
                item {
                    CustomerInformationSection(
                        name = orderState.name,
                        email = orderState.email,
                        phone = orderState.phone
                    )
                }

                // ---------------------------
                // 2. 📦 Thông tin nhận hàng
                // ---------------------------
                item {
                    ShippingAddressSection(
                        receiverName = orderState.name,
                        receiverPhone = orderState.phone,
                        onNameChange = viewModel::updateName,
                        onPhoneChange = viewModel::updatePhone,
                        provinces = addressState.provinces,
                        districts = addressState.districts,
                        wards = addressState.wards,
                        selectedProvince = addressState.selectedProvince,
                        selectedDistrict = addressState.selectedDistrict,
                        selectedWard = addressState.selectedWard,
                        provincesLoading = addressState.provincesLoading,
                        districtsLoading = addressState.districtsLoading,
                        wardsLoading = addressState.wardsLoading,
                        onSelectProvince = viewModel::selectProvince,
                        onSelectDistrict = viewModel::selectDistrict,
                        onSelectWard = viewModel::selectWard,
                        detailAddress = orderState.address,
                        onDetailAddressChange = viewModel::updateAddress
                    )
                }

                // ... (Các sections khác giữ nguyên) ...
                selectedShops.forEach { shop ->
                    item {
                        ShopOrderSection(shop.shopName ?: "Cửa hàng", shop.cartItems)
                    }
                }

                item {
                    OrderNoteSection(
                        note = orderState.note,
                        onNoteChange = viewModel::updateNote
                    )
                }

                // ✅ Calculate subtotal from selected products
                val subtotal = selectedShops.sumOf { shop ->
                    shop.cartItems.sumOf { (it.sku?.price ?: 0) * it.quantity }
                }

                // ✅ Use calculated shipping fee from orderState (default 0đ)
                val shippingFee = orderState.calculatedShippingFee.toInt()
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

            // ---------------------------
            // 🧾 Nút “Đặt hàng” (Quay lại logic cũ)
            // ---------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.colorSystem_greyscale_0_white))
                    .padding(16.dp)
            ) {
                FilledButton(
                    // 🔴 SỬA: Đổi text
                    text = "Đặt hàng",
                    onClick = {
                        // 🔴 SỬA: Gọi thẳng createOrder
                        viewModel.createOrder()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = orderState.selectedShops.isNotEmpty()
                )
            }
        }
    }

    // ... (VoucherBottomSheet giữ nguyên) ...
    VoucherBottomSheet(
        isVisible = showVoucherBottomSheet,
        onDismiss = { showVoucherBottomSheet = false },
        onConfirm = {
            showVoucherBottomSheet = false
        },
        isVoucherListLoaded = true,
        onVoucherCodeApply = { code ->
            // TODO
        }
    )

    // 🔴 XÓA: PurchaseBottomSheet
}

// 🔴 XÓA: Composable PaymentMethodSelectorSection

// ... (ShopOrderSection và OrderNoteSection giữ nguyên) ...
@Composable
fun ShopOrderSection(shopName: String, cartItems: List<com.ptit.domain.entity.cart.CartItemDomainEntity>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🏪",
                style = CustomTypography.TextSemiBold.copy(fontSize = 18.sp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = shopName,
                style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                color = colorResource(R.color.colorSystem_heading_button)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (cartItems.isEmpty()) {
            Text(
                text = "Không có sản phẩm",
                style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                color = colorResource(R.color.colorSystem_text_button)
            )
        } else {
            cartItems.forEach { item ->
                SharedCartItemRow(cartItem = item)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun OrderNoteSection(
    note: TextFieldValue,
    onNoteChange: (TextFieldValue) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(16.dp)
    ) {
        Text(
            text = "📝 Ghi chú đơn hàng",
            style = CustomTypography.TextBold.copy(fontSize = 16.sp),
            color = colorResource(R.color.colorSystem_heading_button)
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilledTextField(
            value = note,
            hint = "Nhập ghi chú cho shop (nếu có)...",
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
    }
}