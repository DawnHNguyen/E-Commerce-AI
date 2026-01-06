package com.ptit.core.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.ptit.core.order.components.*
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
                    val firstOrderId = event.response.orders.firstOrNull()?.id
                    if (firstOrderId != null) {
                        snackbarHostState.showSnackbar("Đặt hàng thành công!")
                        onOrderCreated(firstOrderId)
                    } else {
                        snackbarHostState.showSnackbar("Thành công nhưng không có OrderID")
                    }
                }
            }
        }
    }

    if (orderState.isLoading || userProfileState is Resource.Loading) {
        FullScreenProgressBar()
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tạo đơn hàng", style = CustomTypography.TextBold.copy(fontSize = 20.sp), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = colorResource(R.color.colorSystem_greyscale_0_white)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(R.color.colorSystem_heading_button), titleContentColor = colorResource(R.color.colorSystem_greyscale_0_white))
            )
        }
    ) { paddingValues ->
        MaxSizeColumn(modifier = Modifier.padding(paddingValues).background(colorResource(R.color.colorSystem_background_level_0))) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { CustomerInformationSection(orderState.name, orderState.email, orderState.phone) }

                item {
                    ShippingAddressSection(
                        orderState.name, orderState.phone, viewModel::updateName, viewModel::updatePhone,
                        addressState.provinces, addressState.districts, addressState.wards,
                        addressState.selectedProvince, addressState.selectedDistrict, addressState.selectedWard,
                        addressState.provincesLoading, addressState.districtsLoading, addressState.wardsLoading,
                        viewModel::selectProvince, viewModel::selectDistrict, viewModel::selectWard,
                        orderState.address, viewModel::updateAddress
                    )
                }

                // Shop Items (Chỉ hiển thị sản phẩm, không voucher shop)
                orderState.selectedShops.forEach { shop ->
                    item {
                        ShopOrderSection(
                            shopName = shop.shopName ?: "Cửa hàng",
                            cartItems = shop.cartItems
                        )
                    }
                }

                // Platform Voucher Row
                item {
                    PlatformVoucherRow(
                        selectedVoucher = orderState.selectedPlatformVoucher,
                        onClick = {
                            viewModel.loadAvailableVouchers() // Tải danh sách
                            showVoucherBottomSheet = true // Mở sheet
                        }
                    )
                }

                item { OrderNoteSection(orderState.note, viewModel::updateNote) }

                // Tính toán
                val subtotal = orderState.selectedShops.sumOf { shop -> shop.cartItems.sumOf { (it.sku?.price ?: 0) * it.quantity } }
                val shippingFee = orderState.calculatedShippingFee.toInt()

                val discount = if (orderState.selectedPlatformVoucher != null) {
                    viewModel.calculateDiscountValue(orderState.selectedPlatformVoucher!!, subtotal.toDouble()).toInt()
                } else 0

                val finalTotal = (subtotal + shippingFee - discount).coerceAtLeast(0)

                item {
                    SharedTotalAmountSection(
                        subtotal = subtotal.toPriceFormat(),
                        shippingFee = shippingFee.toPriceFormat(),
                        totalPrice = finalTotal.toPriceFormat(),
                        discount = if (discount > 0) discount.toPriceFormat() else null
                    )
                }
                item { Spacer(modifier = Modifier.height(60.dp)) }
            }

            Box(modifier = Modifier.fillMaxWidth().background(colorResource(R.color.colorSystem_greyscale_0_white)).padding(16.dp)) {
                FilledButton(
                    text = "Đặt hàng",
                    onClick = { viewModel.createOrder() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = orderState.selectedShops.isNotEmpty()
                )
            }
        }
    }

    VoucherBottomSheet(
        isVisible = showVoucherBottomSheet,
        onDismiss = { showVoucherBottomSheet = false },

        // ⚠️ QUAN TRỌNG: Truyền thẳng list từ ViewModel, không lọc nữa
        availableVouchers = orderState.allAvailableVouchers,

        selectedVoucher = orderState.selectedPlatformVoucher,
        isLoading = orderState.isLoadingVouchers,
        voucherError = orderState.voucherError,
        onApplyCode = { code -> viewModel.applyVoucherCode(code) },
        onSelectVoucher = { voucher ->
            viewModel.selectPlatformVoucher(voucher)
            showVoucherBottomSheet = false
        },
        onRemoveVoucher = { viewModel.removePlatformVoucher() }
    )
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