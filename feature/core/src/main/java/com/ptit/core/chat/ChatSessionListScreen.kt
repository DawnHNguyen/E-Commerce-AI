package com.ptit.core.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ptit.common.R
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.chat.ChatSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSessionListScreen(
    onNavigateToChat: (sessionId: String) -> Unit,
    viewModel: ChatSessionListViewModel = hiltViewModel()
) {
    LocalBottomNavigationVisibility.current.value = true
    val uiState by viewModel.uiState.collectAsState()

    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<ChatSession?>(null) }

    // Delete confirmation dialog
    if (showDeleteDialog && sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                sessionToDelete = null
            },
            title = {
                Text(
                    text = "Xóa cuộc trò chuyện",
                    style = CustomTypography.TextSemiBold
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn xóa cuộc trò chuyện này? Hành động này không thể hoàn tác.",
                    style = CustomTypography.TextRegular
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sessionToDelete?.let { viewModel.deleteSession(it.id) }
                        showDeleteDialog = false
                        sessionToDelete = null
                    }
                ) {
                    Text(
                        text = "Xóa",
                        color = colorResource(id = R.color.colorSystem_tint_red)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        sessionToDelete = null
                    }
                ) {
                    Text(
                        text = "Hủy",
                        color = colorResource(id = R.color.colorSystem_greyscale_600)
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trợ lý AI",
                        style = CustomTypography.HeadingH5
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.createNewSession { sessionId ->
                        onNavigateToChat(sessionId)
                    }
                },
                containerColor = colorResource(id = R.color.colorSystem_heading_button),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tạo cuộc trò chuyện mới"
                )
            }
        }
    ) { paddingValues ->
        if (uiState.sessions.isEmpty()) {
            EmptySessionsView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onCreateSession = {
                    viewModel.createNewSession { sessionId ->
                        onNavigateToChat(sessionId)
                    }
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(uiState.sessions) { session ->
                    ChatSessionItem(
                        session = session,
                        onClick = { onNavigateToChat(session.id) },
                        onDelete = {
                            sessionToDelete = session
                            showDeleteDialog = true
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun EmptySessionsView(
    modifier: Modifier = Modifier,
    onCreateSession: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(colorResource(id = R.color.colorSystem_greyscale_100)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = colorResource(id = R.color.colorSystem_greyscale_400)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Chưa có cuộc trò chuyện nào",
            style = CustomTypography.HeadingH5,
            color = colorResource(id = R.color.colorSystem_greyscale_700)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bắt đầu trò chuyện với trợ lý AI để được hỗ trợ về đơn hàng và thanh toán",
            style = CustomTypography.TextRegular,
            color = colorResource(id = R.color.colorSystem_greyscale_500),
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        com.ptit.common.presentation.component.FilledButton(
            text = "Bắt đầu trò chuyện",
            onClick = onCreateSession
        )
    }
}

@Composable
private fun ChatSessionItem(
    session: ChatSession,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.colorSystem_greyscale_100)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = colorResource(id = R.color.colorSystem_heading_button)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = session.title,
                    style = CustomTypography.TextSemiBold,
                    color = colorResource(id = R.color.colorSystem_greyscale_900),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (session.lastMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = session.lastMessage,
                        style = CustomTypography.TextRegular,
                        color = colorResource(id = R.color.colorSystem_greyscale_500),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTimestamp(session.updatedAt),
                    style = CustomTypography.TextSmall,
                    color = colorResource(id = R.color.colorSystem_greyscale_400)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = colorResource(id = R.color.colorSystem_greyscale_400)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Vừa xong"
        diff < 3_600_000 -> "${diff / 60_000} phút trước"
        diff < 86_400_000 -> "${diff / 3_600_000} giờ trước"
        diff < 604_800_000 -> "${diff / 86_400_000} ngày trước"
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}