package com.ptit.core.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.core.order.components.SharedOrderItemRow
import com.ptit.core.order.components.SharedTotalAmountSection
import com.ptit.domain.entity.order.OrderDomainEntity
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    selectedItemIds: List<String>,
    viewModel: CreateOrderViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOrderCreated: (OrderDomainEntity) -> Unit
) {
    // Initialize the viewModel with the selected item IDs
    LaunchedEffect(selectedItemIds) {
        println("CreateOrderScreen received ${selectedItemIds.size} item IDs: $selectedItemIds")
        viewModel.setSelectedItemIds(selectedItemIds)
    }

    val orderState by viewModel.orderState.collectAsState()
    val selectedItems = orderState.selectedItems

    // Calculate pricing info
    val subtotal = selectedItems.sumOf { it.price * it.buyCount }
    val shippingFee = 30000
    val totalPrice = subtotal + shippingFee

    // Show success or error messages
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.orderEvents.collectLatest { event ->
            when (event) {
                is OrderEvent.ShowError -> {
                    println("Error event: ${event.message}")
                    snackbarHostState.showSnackbar(event.message)
                }
                is OrderEvent.OrderCreated -> {
                    println("Order created: ${event.orderResponse}")
                    snackbarHostState.showSnackbar("Đặt hàng thành công!")
                    //onOrderCreated(event.orderResponse)
                }
            }
        }
    }

    // Show loading or error indicators
    if (orderState.isLoading) {
        FullScreenProgressBar()
        return
    }

    if (orderState.error != null) {
        // Show error message
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: ${orderState.error}", color = MaterialTheme.colorScheme.error)
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
                    containerColor = colorResource(id = R.color.colorSystem_heading_button),
                    titleContentColor = colorResource(id = R.color.colorSystem_greyscale_0_white)
                )
            )
        }
    ) { paddingValues ->
        MaxSizeColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(R.color.colorSystem_background_level_0))
        ) {
            // Debug indicator for empty items
            if (selectedItems.isEmpty()) {
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
                        "Loading items... IDs received: ${selectedItemIds.size}",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Shipping Information
                item {
                    ShippingInformationSection(
                        name = orderState.name,
                        phone = orderState.phone,
                        address = orderState.address,
                        onNameChange = viewModel::updateName,
                        onPhoneChange = viewModel::updatePhone,
                        onAddressChange = viewModel::updateAddress
                    )
                }

                // Order Details
                item {
                    Text(
                        text = "Chi tiết đơn hàng",
                        style = CustomTypography.TextBold,
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                }

                // Order Items
                items(selectedItems) { purchase ->
                    SharedOrderItemRow(purchase = purchase)
                }

                // Note Section
                item {
                    OrderNoteSection(
                        note = orderState.note,
                        onNoteChange = viewModel::updateNote
                    )
                }

                // Total amount
                item {
                    SharedTotalAmountSection(subtotal = subtotal, shippingFee = shippingFee, totalPrice = totalPrice)
                }

                // Spacer at the bottom for better layout
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }

            // Bottom Bar with Place Order Button
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
                    enabled = !orderState.isLoading && selectedItems.isNotEmpty()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippingInformationSection(
    name: TextFieldValue,
    phone: TextFieldValue,
    address: TextFieldValue,
    onNameChange: (TextFieldValue) -> Unit,
    onPhoneChange: (TextFieldValue) -> Unit,
    onAddressChange: (TextFieldValue) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Thông tin vận chuyển",
            style = CustomTypography.TextBold,
            color = colorResource(R.color.colorSystem_heading_button)
        )

        // Sử dụng FilledTextField thay vì EditableTextField
        FilledTextField(
            value = name,
            hint = "Họ tên đầy đủ",
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = colorResource(R.color.colorSystem_heading_button)
                )
            }
        )

        FilledTextField(
            value = phone,
            hint = "Số điện thoại",
            onValueChange = onPhoneChange,
            modifier = Modifier.fillMaxWidth(),
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = colorResource(R.color.colorSystem_heading_button)
                )
            }
        )

        FilledTextField(
            value = address,
            hint = "Địa chỉ",
            onValueChange = onAddressChange,
            modifier = Modifier.fillMaxWidth(),
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = colorResource(R.color.colorSystem_heading_button)
                )
            }
        )

        Text(
            text = "Phí vận chuyển: 30.000 đ",
            style = CustomTypography.TextRegular,
            color = colorResource(R.color.colorSystem_normal_text),
            modifier = Modifier.align(Alignment.End)
        )
    }
}

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
            hint = "Ghi chú cho shop",
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
    }
}