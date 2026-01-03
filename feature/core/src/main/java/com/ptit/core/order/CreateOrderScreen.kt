package com.ptit.core.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
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
                        snackbarHostState.showSnackbar("Đặt hàng thành công nhưng không lấy được ID đơn hàng")
                    }
                }
            }
        }
    }

    if (orderState.isLoading || userProfileState is Resource.Loading) {
        FullScreenProgressBar()
        return
    }

    // Logic lọc Voucher hiển thị trong BottomSheet
    val displayedVouchers = remember(orderState.currentSelectingShopId, orderState.allAvailableVouchers) {
        if (orderState.currentSelectingShopId == null) {
            // Context null -> Chỉ hiện Platform Voucher
            orderState.allAvailableVouchers.filter { it.isPlatform }
        } else {
            // Context có ID -> Chỉ hiện Voucher của Shop đó
            orderState.allAvailableVouchers.filter {
                !it.isPlatform && it.shopId == orderState.currentSelectingShopId
            }
        }
    }

    // Voucher đang được chọn trong context hiện tại (để highlight)
    val currentSelectedVoucherInSheet = if (orderState.currentSelectingShopId == null) {
        orderState.selectedPlatformVoucher
    } else {
        orderState.selectedShopVouchers[orderState.currentSelectingShopId]
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Thông tin khách hàng
                item {
                    CustomerInformationSection(
                        name = orderState.name,
                        email = orderState.email,
                        phone = orderState.phone
                    )
                }

                // 2. Thông tin nhận hàng
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

                // 3. Danh sách Shop và Voucher từng Shop
                orderState.selectedShops.forEach { shop ->
                    item {
                        ShopOrderSection(
                            shopName = shop.shopName ?: "Cửa hàng",
                            cartItems = shop.cartItems,
                            // Pass voucher đã chọn của shop này
                            selectedShopVoucher = orderState.selectedShopVouchers[shop.shopId],
                            onSelectVoucherClick = {
                                viewModel.openVoucherSheet(shop.shopId)
                                showVoucherBottomSheet = true
                            }
                        )
                    }
                }

                // 4. Voucher Sàn (Platform)
                item {
                    PlatformVoucherRow(
                        selectedVoucher = orderState.selectedPlatformVoucher,
                        onClick = {
                            viewModel.openVoucherSheet(null) // null = Platform
                            showVoucherBottomSheet = true
                        }
                    )
                }

                // 5. Ghi chú (Đã fix lỗi Unresolved reference)
                item {
                    OrderNoteSection(
                        note = orderState.note,
                        onNoteChange = viewModel::updateNote
                    )
                }

                // 6. Tính toán tổng tiền
                // Tinh subtotal (tiền hàng) -> Ép kiểu về Int
                val subtotal = orderState.selectedShops.sumOf { shop ->
                    shop.cartItems.sumOf { (it.sku?.price ?: 0) * it.quantity }
                }

                // Phí ship -> Ép kiểu về Int
                val shippingFee = orderState.calculatedShippingFee.toInt()

                // Tính tổng giảm giá Shop
                var totalShopDiscount = 0.0
                orderState.selectedShopVouchers.forEach { (shopId, voucher) ->
                    val shopTotal = orderState.selectedShops.find { it.shopId == shopId }
                        ?.cartItems?.sumOf { (it.sku?.price ?: 0) * it.quantity }?.toDouble() ?: 0.0
                    totalShopDiscount += viewModel.calculateDiscountValue(voucher, shopTotal)
                }

                // Tính giảm giá Platform
                val platformDiscount = if (orderState.selectedPlatformVoucher != null) {
                    viewModel.calculateDiscountValue(orderState.selectedPlatformVoucher!!, subtotal.toDouble())
                } else 0.0

                val totalDiscount = (totalShopDiscount + platformDiscount).toInt()
                val finalTotal = (subtotal + shippingFee - totalDiscount).coerceAtLeast(0)

                item {
                    // Đã fix lỗi receiver type mismatch bằng cách ép kiểu .toInt() ở trên
                    SharedTotalAmountSection(
                        subtotal = subtotal.toPriceFormat(),
                        shippingFee = shippingFee.toPriceFormat(),
                        totalPrice = finalTotal.toPriceFormat(),
                        discount = if (totalDiscount > 0) totalDiscount.toPriceFormat() else null
                    )
                }

                item { Spacer(modifier = Modifier.height(60.dp)) }
            }

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

    VoucherBottomSheet(
        isVisible = showVoucherBottomSheet,
        onDismiss = { showVoucherBottomSheet = false },
        availableVouchers = displayedVouchers,
        selectedVoucher = currentSelectedVoucherInSheet,
        isLoading = orderState.isLoadingVouchers,
        voucherError = orderState.voucherError,
        onApplyCode = { code -> viewModel.applyVoucherCode(code) },
        onSelectVoucher = { voucher ->
            viewModel.selectVoucher(voucher)
            showVoucherBottomSheet = false
        },
        onRemoveVoucher = { viewModel.removeVoucher() }
    )
}

// Composable này bị thiếu ở code cũ, gây lỗi Unresolved reference
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