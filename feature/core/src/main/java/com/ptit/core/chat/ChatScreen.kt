package com.ptit.core.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.presentation.component.FilledButton
import com.ptit.domain.entity.chat.ChatCartItem
import com.ptit.domain.entity.chat.ChatCheckoutSummary
import com.ptit.domain.entity.chat.ChatDeliveryAddress
import com.ptit.domain.entity.chat.ChatMessage
import com.ptit.domain.entity.chat.ChatOrderItem
import com.ptit.domain.entity.chat.ChatPaginationInfo
import com.ptit.domain.entity.chat.ChatProductDetail
import com.ptit.domain.entity.chat.ChatProductItem
import com.ptit.domain.entity.chat.ChatRole
import com.ptit.domain.entity.chat.UiTag
import com.ptit.domain.entity.shipping.DistrictEntity
import com.ptit.domain.entity.shipping.ProvinceEntity
import com.ptit.domain.entity.shipping.WardEntity
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddPayment: () -> Unit,
    onNavigateToOrderDetail: (orderId: String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    LocalBottomNavigationVisibility.current.value = false
    val uiState by viewModel.uiState.collectAsState()
    val addressState by viewModel.addressState.collectAsState()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    // Bottom sheet state for order selection
    val orderSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showOrderSheet by remember { mutableStateOf(false) }
    var currentOrderList by remember { mutableStateOf<List<ChatOrderItem>>(emptyList()) }

    // Address input bottom sheet state
    val addressSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddressSheet by remember { mutableStateOf(false) }

    // Initialize with session ID
    LaunchedEffect(sessionId) {
        viewModel.setSessionId(sessionId)
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Handle navigation from AI
    LaunchedEffect(uiState.navigationEvent) {
        uiState.navigationEvent?.let { event ->
            when (event) {
                is NavigationEvent.AddPayment -> onNavigateToAddPayment()
                is NavigationEvent.Checkout -> onNavigateToCheckout()
                is NavigationEvent.OrderDetail -> onNavigateToOrderDetail(event.orderId)
            }
            viewModel.clearNavigationEvent()
        }
    }

    // Check for DisplayOrderList tag in latest message
    LaunchedEffect(uiState.messages) {
        uiState.messages.lastOrNull()?.let { lastMessage ->
            lastMessage.tags.filterIsInstance<UiTag.DisplayOrderList>().firstOrNull()?.let { tag ->
                if (tag.orders.isNotEmpty()) {
                    currentOrderList = tag.orders
                    showOrderSheet = true
                }
            }
        }
    }

    // Order Selection Bottom Sheet
    if (showOrderSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOrderSheet = false },
            sheetState = orderSheetState
        ) {
            OrderSelectionBottomSheet(
                orders = currentOrderList,
                onOrderSelected = { order ->
                    scope.launch {
                        orderSheetState.hide()
                        showOrderSheet = false
                        // Send order ID to chat for checking status
                        viewModel.sendMessage("Kiểm tra đơn hàng ${order.orderCode ?: order.id}")
                    }
                }
            )
        }
    }

    // Address Input Bottom Sheet - matching CreateOrderScreen flow
    if (showAddressSheet) {
        // Initialize address input when sheet opens
        LaunchedEffect(Unit) {
            viewModel.initAddressInput()
        }

        ModalBottomSheet(
            onDismissRequest = {
                showAddressSheet = false
                viewModel.resetAddressState()
            },
            sheetState = addressSheetState
        ) {
            AddressInputBottomSheet(
                addressState = addressState,
                onRecipientNameChange = viewModel::updateRecipientName,
                onPhoneChange = viewModel::updatePhone,
                onProvinceSelect = viewModel::selectProvince,
                onDistrictSelect = viewModel::selectDistrict,
                onWardSelect = viewModel::selectWard,
                onDetailAddressChange = viewModel::updateDetailAddress,
                onSave = {
                    scope.launch {
                        addressSheetState.hide()
                        showAddressSheet = false
                        viewModel.saveAddressAndCheckout()
                    }
                },
                onCancel = {
                    scope.launch {
                        addressSheetState.hide()
                        showAddressSheet = false
                        viewModel.resetAddressState()
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.colorSystem_greyscale_100)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = colorResource(id = R.color.colorSystem_heading_button)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Trợ lý AI",
                                style = CustomTypography.TextSemiBold
                            )
                            Text(
                                text = if (uiState.isLoading) "Đang trả lời..." else "Trực tuyến",
                                style = CustomTypography.TextSmall,
                                color = if (uiState.isLoading)
                                    colorResource(id = R.color.colorSystem_greyscale_500)
                                else
                                    colorResource(id = R.color.colorSystem_success)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                if (uiState.messages.isEmpty() && !uiState.isLoading) {
                    item {
                        WelcomeMessage()
                    }
                }

                items(uiState.messages) { message ->
                    ChatMessageItem(
                        message = message,
                        onQuickReplyClick = { reply ->
                            viewModel.sendMessage(reply)
                        },
                        onNavigateToAddPayment = onNavigateToAddPayment,
                        onShowAddressInput = { showAddressSheet = true },
                        onShowOrderList = { orders ->
                            currentOrderList = orders
                            showOrderSheet = true
                        },
                        onProductClick = { product ->
                            viewModel.sendMessage("Xem chi tiết sản phẩm ${product.id}")
                        },
                        onPaginationClick = { searchQuery: String?, page: Int ->
                            val query = searchQuery?.replace(" ", "_") ?: ""
                            viewModel.sendMessage("Xem trang $page kết quả tìm kiếm $query")
                        },
                        onViewCart = onNavigateToCart,
                        onConfirmOrder = {
                            viewModel.sendMessage("Xác nhận đặt hàng")
                        },
                        onNavigateToOrderDetail = onNavigateToOrderDetail
                    )
                }

                if (uiState.isLoading) {
                    item {
                        TypingIndicator()
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // Input area
            ChatInputArea(
                value = inputText,
                onValueChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                isLoading = uiState.isLoading
            )
        }
    }
}

@Composable
private fun WelcomeMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colorResource(id = R.color.colorSystem_greyscale_100)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = colorResource(id = R.color.colorSystem_heading_button)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Xin chào!",
            style = CustomTypography.HeadingH5
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tôi có thể giúp bạn tìm kiếm sản phẩm, quản lý giỏ hàng, kiểm tra đơn hàng và quản lý thanh toán.",
            style = CustomTypography.TextRegular,
            color = colorResource(id = R.color.colorSystem_greyscale_500),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    onQuickReplyClick: (String) -> Unit,
    onNavigateToAddPayment: () -> Unit,
    onShowAddressInput: () -> Unit,
    onShowOrderList: (List<ChatOrderItem>) -> Unit,
    onProductClick: (ChatProductItem) -> Unit,
    onPaginationClick: (String?, Int) -> Unit, // searchQuery, page
    onViewCart: () -> Unit,
    onConfirmOrder: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit
) {
    val isUser = message.role == ChatRole.USER
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = screenWidth * 0.8f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser)
                        colorResource(id = R.color.colorSystem_heading_button)
                    else
                        colorResource(id = R.color.colorSystem_greyscale_100)
                )
                .padding(12.dp)
        ) {
            if (isUser) {
                Text(
                    text = cleanMessageContent(message.content),
                    style = CustomTypography.TextRegular,
                    color = Color.White
                )
            } else {
                MarkdownText(
                    text = cleanMessageContent(message.content),
                    color = Color.Black
                )
            }
        }

        // Quick Replies
        message.tags.filterIsInstance<UiTag.QuickReplies>().firstOrNull()?.let { tag ->
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tag.options.forEach { option ->
                    QuickReplyChip(
                        text = option,
                        onClick = { onQuickReplyClick(option) }
                    )
                }
            }
        }

        // Navigate to screen
        message.tags.filterIsInstance<UiTag.NavigateToScreen>().forEach { tag ->
            if (tag.screen == "add_payment") {
                Spacer(modifier = Modifier.height(8.dp))
                ActionButton(
                    text = "Thêm phương thức thanh toán",
                    onClick = onNavigateToAddPayment
                )
            }
        }

        // Display order list button
        message.tags.filterIsInstance<UiTag.DisplayOrderList>().firstOrNull()?.let { tag ->
            if (tag.orders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ActionButton(
                    text = "Xem danh sách đơn hàng (${tag.orders.size})",
                    onClick = { onShowOrderList(tag.orders) }
                )
            }
        }

        // Display product list
        message.tags.filterIsInstance<UiTag.DisplayProductList>().firstOrNull()?.let { tag ->
            if (tag.products.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ProductListSection(
                    products = tag.products,
                    pagination = tag.pagination,
                    onProductClick = onProductClick,
                    onPaginationClick = onPaginationClick
                )
            }
        }

        // Display product detail
        message.tags.filterIsInstance<UiTag.DisplayProductDetail>().firstOrNull()?.let { tag ->
            Spacer(modifier = Modifier.height(8.dp))
            ProductDetailCard(
                product = tag.product,
                onAddToCart = { skuId, quantity ->
                    onQuickReplyClick("Thêm vào giỏ hàng SKU $skuId số lượng $quantity")
                }
            )
        }

        // Display cart
        message.tags.filterIsInstance<UiTag.DisplayCart>().firstOrNull()?.let { tag ->
            Spacer(modifier = Modifier.height(8.dp))
            CartSection(
                items = tag.items,
                totalAmount = tag.totalAmount,
                totalItems = tag.totalItems,
                onRemoveItem = { cartItemId ->
                    onQuickReplyClick("Xóa sản phẩm $cartItemId khỏi giỏ hàng")
                },
                onViewCart = onViewCart
            )
        }

        // Display checkout summary
        message.tags.filterIsInstance<UiTag.DisplayCheckoutSummary>().firstOrNull()?.let { tag ->
            Spacer(modifier = Modifier.height(8.dp))
            CheckoutSummaryCard(
                summary = tag.summary,
                onConfirmOrder = onConfirmOrder,
                onNavigateToAddPayment = onNavigateToAddPayment,
                onShowAddressInput = onShowAddressInput
            )
        }

        // Handle order created - navigate to payment
        message.tags.filterIsInstance<UiTag.OrderCreated>().firstOrNull()?.let { tag ->
            Spacer(modifier = Modifier.height(8.dp))
            OrderCreatedCard(
                orderId = tag.orderId,
                orderCode = tag.orderCode,
                onPayNow = { onNavigateToOrderDetail(tag.orderId) }
            )
        }
    }
}

@Composable
private fun QuickReplyChip(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = text,
            style = CustomTypography.TextRegular,
            color = colorResource(id = R.color.colorSystem_heading_button),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.colorSystem_heading_button)
        )
    ) {
        Text(
            text = text,
            style = CustomTypography.TextSemiBold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(id = R.color.colorSystem_greyscale_100))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = colorResource(id = R.color.colorSystem_heading_button)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Đang trả lời...",
            style = CustomTypography.TextRegular,
            color = colorResource(id = R.color.colorSystem_greyscale_600)
        )
    }
}

@Composable
private fun ChatInputArea(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = "Nhập tin nhắn...",
                    style = CustomTypography.TextRegular
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.colorSystem_heading_button),
                unfocusedBorderColor = colorResource(id = R.color.colorSystem_greyscale_300)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            maxLines = 4,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSend,
            enabled = value.isNotBlank() && !isLoading,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (value.isNotBlank() && !isLoading)
                        colorResource(id = R.color.colorSystem_heading_button)
                    else
                        colorResource(id = R.color.colorSystem_greyscale_300)
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Gửi",
                tint = Color.White
            )
        }
    }
}

private fun cleanMessageContent(content: String): String {
    // Remove UI tags from displayed content
    return content
        .replace(Regex("""\[DISPLAY_RECENT_ORDERS\]"""), "")
        .replace(Regex("""\[DISPLAY_PAYMENT_METHODS\]"""), "")
        .replace(Regex("""\[QUICK_REPLIES:[^\]]+\]"""), "")
        .replace(Regex("""\[NAVIGATE_TO:[^\]]+\]"""), "")
        .replace(Regex("""FUNCTION:[^:\s]+:?[^\s]*"""), "")
        .trim()
}

@Composable
private fun OrderSelectionBottomSheet(
    orders: List<ChatOrderItem>,
    onOrderSelected: (ChatOrderItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        // Header
        Text(
            text = "Chọn đơn hàng",
            style = CustomTypography.HeadingH5,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        HorizontalDivider()

        // Order list
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(orders) { order ->
                OrderListItem(
                    order = order,
                    onClick = { onOrderSelected(order) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = colorResource(id = R.color.colorSystem_greyscale_200)
                )
            }
        }
    }
}

@Composable
private fun OrderListItem(
    order: ChatOrderItem,
    onClick: () -> Unit
) {
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Order icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colorResource(id = R.color.colorSystem_greyscale_100)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                tint = colorResource(id = R.color.colorSystem_heading_button),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Order info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = order.orderCode ?: "Đơn hàng #${order.id.take(8)}",
                style = CustomTypography.TextSemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrderStatusChip(status = order.status)

                Text(
                    text = "${order.itemCount} sản phẩm",
                    style = CustomTypography.TextSmall,
                    color = colorResource(id = R.color.colorSystem_greyscale_500)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = currencyFormat.format(order.totalAmount),
                style = CustomTypography.TextMedium,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )
        }
    }
}

@Composable
private fun OrderStatusChip(status: String) {
    val (backgroundColor, textColor, displayText) = when (status.lowercase()) {
        "pending" -> Triple(
            colorResource(id = R.color.colorSystem_tint_yellow),
            Color.Black,
            "Chờ xử lý"
        )
        "confirmed" -> Triple(
            colorResource(id = R.color.colorSystem_normal_button),
            Color.White,
            "Đã xác nhận"
        )
        "shipping" -> Triple(
            colorResource(id = R.color.colorSystem_text_button),
            Color.White,
            "Đang giao"
        )
        "delivered" -> Triple(
            colorResource(id = R.color.colorSystem_success),
            Color.White,
            "Đã giao"
        )
        "cancelled" -> Triple(
            colorResource(id = R.color.colorSystem_tint_red),
            Color.White,
            "Đã hủy"
        )
        else -> Triple(
            colorResource(id = R.color.colorSystem_greyscale_300),
            Color.Black,
            status
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = displayText,
            style = CustomTypography.TextSmall,
            color = textColor
        )
    }
}

/**
 * Simple Markdown text renderer supporting:
 * - **bold** or __bold__
 * - *italic* or _italic_
 * - Bullet points (- or *)
 * - Numbered lists (1. 2. etc)
 */
@Composable
private fun MarkdownText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val annotatedString = remember(text) {
        parseMarkdown(text, color)
    }

    Text(
        text = annotatedString,
        style = CustomTypography.TextRegular,
        modifier = modifier
    )
}

private fun parseMarkdown(text: String, defaultColor: Color) = buildAnnotatedString {
    val lines = text.split("\n")

    lines.forEachIndexed { lineIndex, line ->
        val trimmedLine = line.trim()

        // Check for bullet points
        val isBullet = trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") ||
                trimmedLine.startsWith("• ")
        // Check for numbered list
        val isNumberedList = trimmedLine.matches(Regex("^\\d+\\.\\s.*"))

        if (isBullet) {
            append("  • ")
            parseInlineMarkdown(trimmedLine.substring(2), defaultColor)
        } else if (isNumberedList) {
            val numberEnd = trimmedLine.indexOf(". ")
            val number = trimmedLine.substring(0, numberEnd + 1)
            append("  $number ")
            parseInlineMarkdown(trimmedLine.substring(numberEnd + 2), defaultColor)
        } else {
            parseInlineMarkdown(line, defaultColor)
        }

        // Add newline except for last line
        if (lineIndex < lines.size - 1) {
            append("\n")
        }
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.parseInlineMarkdown(
    text: String,
    defaultColor: Color
) {
    var currentIndex = 0
    val boldRegex = Regex("""\*\*(.+?)\*\*|__(.+?)__""")
    val italicRegex = Regex("""\*(.+?)\*|_(.+?)_""")

    // Find all bold matches first
    val boldMatches = boldRegex.findAll(text).toList()
    val italicMatches = italicRegex.findAll(text).filter { italicMatch ->
        // Filter out italic matches that are part of bold matches
        boldMatches.none { boldMatch ->
            italicMatch.range.first >= boldMatch.range.first &&
                    italicMatch.range.last <= boldMatch.range.last
        }
    }.toList()

    // Combine and sort all matches
    data class MarkdownMatch(val range: IntRange, val content: String, val isBold: Boolean)

    val allMatches = mutableListOf<MarkdownMatch>()
    boldMatches.forEach { match ->
        val content = match.groupValues[1].ifEmpty { match.groupValues[2] }
        allMatches.add(MarkdownMatch(match.range, content, true))
    }
    italicMatches.forEach { match ->
        val content = match.groupValues[1].ifEmpty { match.groupValues[2] }
        allMatches.add(MarkdownMatch(match.range, content, false))
    }
    allMatches.sortBy { it.range.first }

    // Process text with matches
    for (match in allMatches) {
        // Add text before match
        if (currentIndex < match.range.first) {
            append(text.substring(currentIndex, match.range.first))
        }

        // Add styled text
        if (match.isBold) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.content)
            }
        } else {
            withStyle(SpanStyle(fontWeight = FontWeight.Light)) {
                append(match.content)
            }
        }

        currentIndex = match.range.last + 1
    }

    // Add remaining text
    if (currentIndex < text.length) {
        append(text.substring(currentIndex))
    }
}

// ==================== PRODUCT COMPONENTS ====================

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ProductListSection(
    products: List<ChatProductItem>,
    pagination: ChatPaginationInfo,
    onProductClick: (ChatProductItem) -> Unit,
    onPaginationClick: (String?, Int) -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Product horizontal list
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    currencyFormat = currencyFormat,
                    onClick = { onProductClick(product) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Pagination controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button
            OutlinedButton(
                onClick = { onPaginationClick(pagination.searchQuery, pagination.currentPage - 1) },
                enabled = pagination.hasPreviousPage,
                modifier = Modifier.size(40.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Trang trước"
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Trang ${pagination.currentPage}/${pagination.totalPages}",
                style = CustomTypography.TextMedium
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Next button
            OutlinedButton(
                onClick = { onPaginationClick(pagination.searchQuery, pagination.currentPage + 1) },
                enabled = pagination.hasNextPage,
                modifier = Modifier.size(40.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Trang sau"
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ProductCard(
    product: ChatProductItem,
    currencyFormat: NumberFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Product image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(colorResource(id = R.color.colorSystem_greyscale_100))
            ) {
                if (product.mainImage.isNotEmpty()) {
                    GlideImage(
                        model = product.mainImage,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Discount badge
                if (product.hasDiscount) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colorResource(id = R.color.colorSystem_tint_red))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "-${product.discountPercent}%",
                            style = CustomTypography.TextSmall,
                            color = Color.White
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = product.name,
                    style = CustomTypography.TextMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Price
                Text(
                    text = currencyFormat.format(product.basePrice),
                    style = CustomTypography.TextSemiBold,
                    color = colorResource(id = R.color.colorSystem_tint_red)
                )

                if (product.hasDiscount && product.virtualPrice != null) {
                    Text(
                        text = currencyFormat.format(product.virtualPrice),
                        style = CustomTypography.TextSmall.copy(textDecoration = TextDecoration.LineThrough),
                        color = colorResource(id = R.color.colorSystem_greyscale_400)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Rating and sold
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = colorResource(id = R.color.colorSystem_tint_yellow)
                    )
                    Text(
                        text = " ${product.rating}",
                        style = CustomTypography.TextSmall,
                        color = colorResource(id = R.color.colorSystem_greyscale_500)
                    )
                    Text(
                        text = " | Đã bán ${product.sold}",
                        style = CustomTypography.TextSmall,
                        color = colorResource(id = R.color.colorSystem_greyscale_500)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ProductDetailCard(
    product: ChatProductDetail,
    onAddToCart: (skuId: String, quantity: Int) -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }
    var selectedSkuIndex by remember { mutableStateOf(0) }
    var quantity by remember { mutableStateOf(1) }

    val selectedSku = product.skus.getOrNull(selectedSkuIndex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Product images
            if (product.images.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(product.images) { imageUrl ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colorResource(id = R.color.colorSystem_greyscale_100))
                        ) {
                            GlideImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Product name
            Text(
                text = product.name,
                style = CustomTypography.HeadingH5,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currencyFormat.format(selectedSku?.price ?: product.basePrice),
                    style = CustomTypography.HeadingH5,
                    color = colorResource(id = R.color.colorSystem_tint_red)
                )
                if (product.hasDiscount && product.virtualPrice != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currencyFormat.format(product.virtualPrice),
                        style = CustomTypography.TextMedium.copy(textDecoration = TextDecoration.LineThrough),
                        color = colorResource(id = R.color.colorSystem_greyscale_400)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "-${product.discountPercent}%",
                        style = CustomTypography.TextSmall,
                        color = colorResource(id = R.color.colorSystem_tint_red)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rating, sold, shop
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = colorResource(id = R.color.colorSystem_tint_yellow)
                )
                Text(
                    text = " ${product.rating}/5",
                    style = CustomTypography.TextRegular
                )
                Text(
                    text = " | Đã bán ${product.sold}",
                    style = CustomTypography.TextRegular,
                    color = colorResource(id = R.color.colorSystem_greyscale_500)
                )
            }

            product.shopName?.let { shopName ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Shop: $shopName",
                    style = CustomTypography.TextSmall,
                    color = colorResource(id = R.color.colorSystem_greyscale_500)
                )
            }

            // Description
            if (product.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.description,
                    style = CustomTypography.TextRegular,
                    color = colorResource(id = R.color.colorSystem_greyscale_600),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // SKU selection
            if (product.skus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Phân loại:",
                    style = CustomTypography.TextSemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(product.skus.size) { index ->
                        val sku = product.skus[index]
                        val isSelected = index == selectedSkuIndex
                        Card(
                            modifier = Modifier.clickable { selectedSkuIndex = index },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    colorResource(id = R.color.colorSystem_heading_button)
                                else
                                    colorResource(id = R.color.colorSystem_greyscale_100)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = sku.value,
                                    style = CustomTypography.TextSmall,
                                    color = if (isSelected) Color.White else Color.Black
                                )
                                Text(
                                    text = currencyFormat.format(sku.price),
                                    style = CustomTypography.TextSmall,
                                    color = if (isSelected) Color.White else colorResource(id = R.color.colorSystem_tint_red)
                                )
                                Text(
                                    text = "Còn ${sku.stock}",
                                    style = CustomTypography.TextSmall,
                                    color = if (isSelected) Color.White.copy(alpha = 0.7f) else colorResource(id = R.color.colorSystem_greyscale_500)
                                )
                            }
                        }
                    }
                }
            }

            // Quantity selector
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Số lượng:",
                    style = CustomTypography.TextSemiBold
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = { if (quantity > 1) quantity-- },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.colorSystem_greyscale_100))
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Giảm",
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = quantity.toString(),
                    style = CustomTypography.TextMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(
                    onClick = {
                        val maxStock = selectedSku?.stock ?: 99
                        if (quantity < maxStock) quantity++
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.colorSystem_greyscale_100))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tăng",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Add to cart button
            Spacer(modifier = Modifier.height(12.dp))
            ActionButton(
                text = "Thêm vào giỏ hàng",
                onClick = {
                    selectedSku?.let { sku ->
                        onAddToCart(sku.id, quantity)
                    }
                }
            )
        }
    }
}

// ==================== CART COMPONENTS ====================

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun CartSection(
    items: List<ChatCartItem>,
    totalAmount: Int,
    totalItems: Int,
    onRemoveItem: (String) -> Unit,
    onViewCart: () -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = colorResource(id = R.color.colorSystem_heading_button)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Giỏ hàng ($totalItems)",
                        style = CustomTypography.TextSemiBold
                    )
                }
                Text(
                    text = currencyFormat.format(totalAmount),
                    style = CustomTypography.TextSemiBold,
                    color = colorResource(id = R.color.colorSystem_tint_red)
                )
            }

            if (items.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Giỏ hàng của bạn đang trống",
                    style = CustomTypography.TextRegular,
                    color = colorResource(id = R.color.colorSystem_greyscale_500),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                // Cart items (show max 3)
                items.take(3).forEach { item ->
                    CartItemRow(
                        item = item,
                        currencyFormat = currencyFormat,
                        onRemove = { onRemoveItem(item.cartItemId) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (items.size > 3) {
                    Text(
                        text = "và ${items.size - 3} sản phẩm khác...",
                        style = CustomTypography.TextSmall,
                        color = colorResource(id = R.color.colorSystem_greyscale_500)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // View cart button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onViewCart) {
                        Text(
                            text = "Xem giỏ hàng đầy đủ",
                            style = CustomTypography.TextMedium,
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun CartItemRow(
    item: ChatCartItem,
    currencyFormat: NumberFormat,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colorResource(id = R.color.colorSystem_greyscale_100))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product image
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colorResource(id = R.color.colorSystem_greyscale_100))
        ) {
            if (item.image.isNotEmpty()) {
                GlideImage(
                    model = item.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Product info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productName,
                style = CustomTypography.TextMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            item.skuValue?.let { skuValue ->
                Text(
                    text = skuValue,
                    style = CustomTypography.TextSmall,
                    color = colorResource(id = R.color.colorSystem_greyscale_500)
                )
            }
            Text(
                text = "${item.quantity} x ${currencyFormat.format(item.price)}",
                style = CustomTypography.TextSmall,
                color = colorResource(id = R.color.colorSystem_greyscale_600)
            )
        }

        // Total and remove button
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = currencyFormat.format(item.price * item.quantity),
                style = CustomTypography.TextSemiBold,
                color = colorResource(id = R.color.colorSystem_tint_red)
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa",
                    modifier = Modifier.size(16.dp),
                    tint = colorResource(id = R.color.colorSystem_greyscale_400)
                )
            }
        }
    }
}

// ==================== CHECKOUT COMPONENTS ====================

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun CheckoutSummaryCard(
    summary: ChatCheckoutSummary,
    onConfirmOrder: () -> Unit,
    onNavigateToAddPayment: () -> Unit,
    onShowAddressInput: () -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = colorResource(id = R.color.colorSystem_heading_button)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Xác nhận đơn hàng",
                    style = CustomTypography.TextBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Items summary
            Text(
                text = "Sản phẩm (${summary.items.size})",
                style = CustomTypography.TextSemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            summary.items.take(3).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.productName,
                            style = CustomTypography.TextMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "x${item.quantity}",
                            style = CustomTypography.TextSmall,
                            color = colorResource(id = R.color.colorSystem_greyscale_500)
                        )
                    }
                    Text(
                        text = currencyFormat.format(item.price * item.quantity),
                        style = CustomTypography.TextMedium
                    )
                }
            }

            if (summary.items.size > 3) {
                Text(
                    text = "và ${summary.items.size - 3} sản phẩm khác...",
                    style = CustomTypography.TextSmall,
                    color = colorResource(id = R.color.colorSystem_greyscale_500),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Delivery address
            Text(
                text = "Địa chỉ giao hàng",
                style = CustomTypography.TextSemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))

            val address = summary.address
            if (address != null) {
                Text(
                    text = address.recipientName,
                    style = CustomTypography.TextMedium
                )
                Text(
                    text = address.phone,
                    style = CustomTypography.TextSmall,
                    color = colorResource(id = R.color.colorSystem_greyscale_600)
                )
                Text(
                    text = address.fullAddress,
                    style = CustomTypography.TextSmall,
                    color = colorResource(id = R.color.colorSystem_greyscale_600)
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onShowAddressInput),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.colorSystem_tint_red).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = colorResource(id = R.color.colorSystem_tint_red)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thêm địa chỉ giao hàng",
                            style = CustomTypography.TextMedium,
                            color = colorResource(id = R.color.colorSystem_tint_red)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Price breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Tạm tính:", style = CustomTypography.TextRegular)
                Text(text = currencyFormat.format(summary.subtotal), style = CustomTypography.TextMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Phí vận chuyển:", style = CustomTypography.TextRegular)
                Text(text = currencyFormat.format(summary.shippingFee), style = CustomTypography.TextMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Tổng cộng:", style = CustomTypography.TextBold)
                Text(
                    text = currencyFormat.format(summary.total),
                    style = CustomTypography.TextBold,
                    color = colorResource(id = R.color.colorSystem_tint_red)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            if (!summary.hasPaymentMethod) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToAddPayment),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.colorSystem_greyscale_100)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = colorResource(id = R.color.colorSystem_heading_button)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thêm phương thức thanh toán",
                            style = CustomTypography.TextMedium,
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Confirm button
            val canConfirm = summary.address != null && summary.hasPaymentMethod
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = canConfirm, onClick = onConfirmOrder),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (canConfirm)
                        colorResource(id = R.color.colorSystem_heading_button)
                    else
                        colorResource(id = R.color.colorSystem_greyscale_300)
                )
            ) {
                Text(
                    text = "Xác nhận đặt hàng",
                    style = CustomTypography.TextBold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            if (!canConfirm) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (summary.address == null) "Vui lòng thêm địa chỉ giao hàng"
                           else "Vui lòng thêm phương thức thanh toán",
                    style = CustomTypography.TextSmall,
                    color = colorResource(id = R.color.colorSystem_greyscale_500),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun OrderCreatedCard(
    orderId: String,
    orderCode: String?,
    onPayNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.colorSystem_tint_green).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = colorResource(id = R.color.colorSystem_success)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Đơn hàng đã được tạo!",
                style = CustomTypography.TextBold,
                color = colorResource(id = R.color.colorSystem_success)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Mã đơn: ${orderCode ?: orderId}",
                style = CustomTypography.TextMedium,
                color = colorResource(id = R.color.colorSystem_greyscale_600)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPayNow),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(id = R.color.colorSystem_heading_button)
                )
            ) {
                Text(
                    text = "Thanh toán ngay",
                    style = CustomTypography.TextBold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

/**
 * Address Input Bottom Sheet matching CreateOrderScreen flow
 * - Auto-filled name and phone from user profile
 * - Province/District/Ward dropdowns with API calls
 * - Detail address text field
 * - Shipping fee calculation when address is complete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressInputBottomSheet(
    addressState: AddressInputState,
    onRecipientNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onProvinceSelect: (ProvinceEntity?) -> Unit,
    onDistrictSelect: (DistrictEntity?) -> Unit,
    onWardSelect: (WardEntity?) -> Unit,
    onDetailAddressChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Địa chỉ giao hàng",
            style = CustomTypography.TextBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Recipient Name and Phone in a row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = addressState.recipientName,
                onValueChange = onRecipientNameChange,
                label = { Text("Tên người nhận") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.colorSystem_heading_button),
                    focusedLabelColor = colorResource(id = R.color.colorSystem_heading_button)
                )
            )

            OutlinedTextField(
                value = addressState.phone,
                onValueChange = onPhoneChange,
                label = { Text("Số điện thoại") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.colorSystem_heading_button),
                    focusedLabelColor = colorResource(id = R.color.colorSystem_heading_button)
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Province Dropdown
        AddressDropdown(
            label = "Tỉnh/Thành phố",
            items = addressState.provinces,
            selectedItem = addressState.selectedProvince,
            isLoading = addressState.provincesLoading,
            enabled = !addressState.provincesLoading,
            onItemSelected = onProvinceSelect,
            itemNameSelector = { it.name }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // District Dropdown
        AddressDropdown(
            label = "Quận/Huyện",
            items = addressState.districts,
            selectedItem = addressState.selectedDistrict,
            isLoading = addressState.districtsLoading,
            enabled = addressState.selectedProvince != null && !addressState.districtsLoading,
            onItemSelected = onDistrictSelect,
            itemNameSelector = { it.name }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Ward Dropdown
        AddressDropdown(
            label = "Phường/Xã",
            items = addressState.wards,
            selectedItem = addressState.selectedWard,
            isLoading = addressState.wardsLoading,
            enabled = addressState.selectedDistrict != null && !addressState.wardsLoading,
            onItemSelected = onWardSelect,
            itemNameSelector = { it.name }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Detail Address
        OutlinedTextField(
            value = addressState.detailAddress,
            onValueChange = onDetailAddressChange,
            label = { Text("Địa chỉ cụ thể (số nhà, tên đường...)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.colorSystem_heading_button),
                focusedLabelColor = colorResource(id = R.color.colorSystem_heading_button)
            )
        )

        // Show shipping fee when address is complete
        if (addressState.isAddressComplete) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Phí vận chuyển:",
                    style = CustomTypography.TextMedium
                )
                if (addressState.isCalculatingShippingFee) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (addressState.shippingFee > 0)
                            currencyFormat.format(addressState.shippingFee)
                        else "Đang tính...",
                        style = CustomTypography.TextSemiBold,
                        color = colorResource(id = R.color.colorSystem_heading_button)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Hủy")
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = addressState.isFormValid, onClick = onSave),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (addressState.isFormValid)
                        colorResource(id = R.color.colorSystem_heading_button)
                    else
                        colorResource(id = R.color.colorSystem_greyscale_300)
                )
            ) {
                Text(
                    text = "Lưu địa chỉ",
                    style = CustomTypography.TextSemiBold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Dropdown component for Province/District/Ward selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> AddressDropdown(
    label: String,
    items: List<T>,
    selectedItem: T?,
    isLoading: Boolean,
    enabled: Boolean,
    onItemSelected: (T?) -> Unit,
    itemNameSelector: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    // Update search text when selected item changes
    LaunchedEffect(selectedItem) {
        searchText = selectedItem?.let(itemNameSelector) ?: ""
    }

    // Filter items based on search
    val filteredItems = remember(items, searchText) {
        items.filter { itemNameSelector(it).contains(searchText, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filteredItems.isNotEmpty(),
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = isLoading,
            enabled = enabled,
            value = searchText,
            onValueChange = { newValue ->
                searchText = newValue
                if (newValue.isNotEmpty() && !expanded) {
                    expanded = true
                }
                if (selectedItem != null && itemNameSelector(selectedItem) != newValue) {
                    onItemSelected(null)
                }
            },
            label = { Text(label) },
            placeholder = { Text(if (isLoading) "Đang tải..." else "Chọn $label") },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.colorSystem_heading_button),
                focusedLabelColor = colorResource(id = R.color.colorSystem_heading_button)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded && filteredItems.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filteredItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemNameSelector(item)) },
                    onClick = {
                        onItemSelected(item)
                        searchText = itemNameSelector(item)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}