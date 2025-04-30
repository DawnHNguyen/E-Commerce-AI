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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FilledTextField
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.cart.PurchaseDomainEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    selectedItemIds: ArrayList<String>,
    viewModel: CreateOrderViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onPlaceOrder: () -> Unit
) {
    // Pass the selected item IDs to the ViewModel
    LaunchedEffect(selectedItemIds) {
        viewModel.setSelectedItemIds(selectedItemIds)
    }

    val orderState by viewModel.orderState.collectAsState()
    val selectedItems = orderState.selectedItems
    val totalPrice = selectedItems.sumOf { it.price * it.buyCount }

    MaxSizeColumn(
        modifier = Modifier.background(colorResource(R.color.colorSystem_background_level_0))
    ) {
        // Top Bar with centered and enlarged title
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
                OrderItemRow(purchase = purchase)
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
                TotalAmountSection(totalPrice = totalPrice)
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
                onClick = onPlaceOrder,
                modifier = Modifier.fillMaxWidth()
            )
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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun OrderItemRow(purchase: PurchaseDomainEntity) {
    val product = purchase.product
    val quantity = purchase.buyCount

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.colorSystem_background_level_2))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            GlideImage(
                model = product.image,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = CustomTypography.TextRegular,
                    color = colorResource(R.color.colorSystem_normal_text),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Original price with strikethrough
                Text(
                    text = "${product.priceBeforeDiscount}đ",
                    style = CustomTypography.TextRegular.copy(
                        textDecoration = TextDecoration.LineThrough,
                        fontSize = 12.sp
                    ),
                    color = colorResource(R.color.colorSystem_greyscale_600)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${purchase.price}đ",
                    style = CustomTypography.TextSemiBold,
                    color = colorResource(R.color.colorSystem_heading_button)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Số lượng: $quantity",
                    style = CustomTypography.TextRegular,
                    color = colorResource(R.color.colorSystem_normal_text)
                )
            }
        }
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

@Composable
fun TotalAmountSection(totalPrice: Int) {
    val shippingFee = 30000
    val grandTotal = totalPrice + shippingFee

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tổng thanh toán:",
                style = CustomTypography.TextSemiBold,
                color = colorResource(R.color.colorSystem_normal_text)
            )

            Text(
                text = "$grandTotal đ",
                style = CustomTypography.TextSemiBold,
                color = colorResource(R.color.colorSystem_heading_button)
            )
        }
    }
}

