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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ptit.common.R
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.chat.ChatMessage
import com.ptit.domain.entity.chat.ChatOrderItem
import com.ptit.domain.entity.chat.ChatRole
import com.ptit.domain.entity.chat.UiTag
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
    viewModel: ChatViewModel = hiltViewModel()
) {
    LocalBottomNavigationVisibility.current.value = false
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    // Bottom sheet state for order selection
    val orderSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showOrderSheet by remember { mutableStateOf(false) }
    var currentOrderList by remember { mutableStateOf<List<ChatOrderItem>>(emptyList()) }

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
                        onShowOrderList = { orders ->
                            currentOrderList = orders
                            showOrderSheet = true
                        }
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
            text = "Tôi có thể giúp bạn kiểm tra đơn hàng hoặc quản lý phương thức thanh toán.",
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
    onShowOrderList: (List<ChatOrderItem>) -> Unit
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